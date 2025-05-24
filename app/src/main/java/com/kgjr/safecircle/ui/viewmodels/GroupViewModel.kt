package com.kgjr.safecircle.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor() : ViewModel() {

    private val _groupKeys = MutableStateFlow<List<String>?>(null)
    val groupKeys: StateFlow<List<String>?> = _groupKeys

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadUserGroups(userId: String) {
        viewModelScope.launch {
            val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
            _isLoading.value = true
            dbRef.get().addOnSuccessListener { snapshot ->
                val keys = snapshot.children.mapNotNull { it.key }
                Log.d("GroupViewModel", "Loaded groups for user $userId: $keys")
                _groupKeys.value = if (keys.isEmpty()) null else keys
                _isLoading.value = false

            }.addOnFailureListener { exception ->
                Log.e("GroupViewModel", "Error loading groups for user $userId", exception)
                _groupKeys.value = null
                _isLoading.value = false
            }
        }
    }

    fun createGroupForUser(groupName: String, adminUserId: String, eMail: String, userName: String, userProfileImageUrl: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Groups")

        fun generateShortKey(length: Int = 6): String {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            return (1..length).map { chars.random() }.joinToString("")
        }

        fun attemptCreateUniqueGroup(retryCount: Int = 0, maxRetries: Int = 5) {
            if (retryCount >= maxRetries) {
                Log.e("GroupCreation", "Failed to create a unique group ID after $maxRetries attempts.")
                return
            }

            val groupId = generateShortKey(6)
            _isLoading.value = true
            dbRef.child(groupId).get().addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    // Proceed with creating group
                    viewModelScope.launch {
                        val userDataMap = mapOf(
                            "name" to userName,
                            "profileImageUrl" to userProfileImageUrl
                        )
                        val adminMap = mapOf(adminUserId to userDataMap)
                        val usersMap = mapOf(adminUserId to userDataMap)

                        val groupData = mapOf(
                            "name" to groupName,
                            "createdOn" to System.currentTimeMillis(),
                            "admin" to adminMap,
                            "users" to usersMap
                        )


                        dbRef.child(groupId).setValue(groupData)
                            .addOnSuccessListener {
                                _isLoading.value = false
                                addToUser(
                                    userId = adminUserId,
                                    userName = userName,
                                    eMail = eMail,
                                    groupId = groupId,
                                    groupName = groupName,
                                    userProfileImageUrl = userProfileImageUrl
                                )
                            }
                            .addOnFailureListener { exception ->
                                exception.printStackTrace()
                                _isLoading.value = false
                            }
                    }
                } else {
                    _isLoading.value = false
                    attemptCreateUniqueGroup(retryCount + 1, maxRetries)
                }
            }.addOnFailureListener {
                _isLoading.value = false
                Log.e("GroupCreation", "Error checking group ID existence", it)
            }
        }
        attemptCreateUniqueGroup()
    }

    fun addToUser(userId: String, userName: String, eMail: String, groupId: String, groupName: String, userProfileImageUrl: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

        viewModelScope.launch {
            _isLoading.value = true

            userRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val currentMap = currentData.value as? Map<String, Any?> ?: emptyMap()

                    val currentGroups = (currentMap["groups"] as? Map<String, String>)?.toMutableMap() ?: mutableMapOf()

                    // Only add if groupId is not already present
                    if (!currentGroups.containsKey(groupId)) {
                        currentGroups[groupId] = groupName

                        val updatedUserData = mutableMapOf<String, Any?>(
                            "name" to (currentMap["name"] ?: userName),
                            "email" to (currentMap["email"] ?: eMail),
                            "createdOn" to (currentMap["createdOn"] ?: System.currentTimeMillis()),
                            "groups" to currentGroups,
                            "profileImage" to (currentMap["profileImage"] ?: userProfileImageUrl)
                        )

                        currentData.value = updatedUserData
                    }

                    return Transaction.success(currentData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    _isLoading.value = false
                    error?.toException()?.printStackTrace()
                }
            })
        }
    }

}