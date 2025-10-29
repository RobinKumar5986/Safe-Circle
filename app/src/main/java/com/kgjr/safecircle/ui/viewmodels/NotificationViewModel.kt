package com.kgjr.safecircle.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.models.Group
import com.kgjr.safecircle.models.GroupMember
import com.kgjr.safecircle.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor() : ViewModel() {
    private val sharedPref = MainApplication.getSharedPreferenceManager()


    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _group = MutableStateFlow<Group?>(null)
    val group: StateFlow<Group?> = _group

    private val _availableGroups = MutableStateFlow<Map<String, Group>>(emptyMap())
    val availableGroups: StateFlow<Map<String, Group>> = _availableGroups

    private val _groupList = MutableStateFlow<List<GroupMember>>(emptyList())
    val groupList: StateFlow<List<GroupMember>> = _groupList

    private val _currentGroupId = MutableStateFlow<String?>(null)
    val currentGroupId : StateFlow<String?> = _currentGroupId

    private val _isLoading = MutableStateFlow<Set<String>>(emptySet())
    val isLoading : StateFlow<Set<String>> = _isLoading

    private val _error = MutableStateFlow<Set<String>>(emptySet())
    val error: StateFlow<Set<String>> = _error

    init {
        loadFromSharedPrefs()
    }

    private fun loadFromSharedPrefs() {
        _user.value = sharedPref.getUser()
        _group.value = sharedPref.getGroup()
        _groupList.value = sharedPref.getGroupList()
        _currentGroupId.value = sharedPref.getGroupId()
    }

    fun getGroupUsers(groupId: String) {
        _isLoading.value = _isLoading.value + groupId
        _error.value = _error.value - groupId

        val dbRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupId)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val groupData = snapshot.getValue(Group::class.java)
                if (groupData != null) {
                    _group.value = groupData
                    _availableGroups.value = _availableGroups.value + (groupId to groupData)
                }
                _isLoading.value = _isLoading.value - groupId
            }

            override fun onCancelled(errorMsg: DatabaseError) {
                _isLoading.value = _isLoading.value - groupId
                _error.value = _error.value + groupId
            }

            override fun equals(other: Any?): Boolean {
                return super.equals(other)
            }
        })
    }

    fun getAllFcmTokenForTheUsers(
        userIds: Set<String>,
        onComplete: () -> Unit
    ) {
        if(userIds.isEmpty()){
            sharedPref.saveFcmTokens(emptyMap())
            onComplete()
            return
        }
        val fcmTokenMap = mutableMapOf<String, String>()
        val dbRef = FirebaseDatabase.getInstance().getReference("FcmTokens/Users")

        var completedRequests = 0

        for (userId in userIds) {
            dbRef.child(userId).child("fcmToken").get()
                .addOnSuccessListener { snapshot ->
                    val token = snapshot.getValue(String::class.java)
                    if (token != null) {
                        fcmTokenMap[userId] = token
                    }
                    completedRequests++
                    if (completedRequests == userIds.size) {
                        sharedPref.saveFcmTokens(fcmTokenMap)
                        onComplete()
                    }
                }
                .addOnFailureListener {
                    completedRequests++
                    if (completedRequests == userIds.size) {
                        onComplete()
                    }
                }
        }
    }

}