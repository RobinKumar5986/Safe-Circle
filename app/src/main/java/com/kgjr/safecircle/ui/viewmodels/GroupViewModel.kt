package com.kgjr.safecircle.ui.viewmodels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.models.ArchiveLocationData
import com.kgjr.safecircle.models.Group
import com.kgjr.safecircle.models.GroupDataWithLocation
import com.kgjr.safecircle.models.GroupMember
import com.kgjr.safecircle.models.SavedPlace
import com.kgjr.safecircle.models.SavedPlaceData
import com.kgjr.safecircle.models.User
import com.kgjr.safecircle.models.UserLastLocation
import com.kgjr.safecircle.ui.utils.SharedPreferenceManager
import com.kgjr.safecircle.ui.utils.helpers.HelperFunctions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor() : ViewModel() {

    private val sharedPref = MainApplication.getSharedPreferenceManager()

    private val _groupKeys = MutableStateFlow<List<String>?>(null)
    val groupKeys: StateFlow<List<String>?> = _groupKeys

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLoadingForArchive = MutableStateFlow(false)
    val isLoadingForArchive: StateFlow<Boolean> = _isLoadingForArchive



    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _group = MutableStateFlow<Group?>(null)
    val group: StateFlow<Group?> = _group

    private val _groupList = MutableStateFlow<List<GroupMember>>(emptyList())
    val groupList: StateFlow<List<GroupMember>> = _groupList

    private val _currentGroupId = MutableStateFlow<String?>(null)
    val currentGroupId : StateFlow<String?> = _currentGroupId

    private val _userCurrentLocations = MutableStateFlow<MutableMap< String , UserLastLocation>>(mutableMapOf())
    val userCurrentLocations: StateFlow<MutableMap<String , UserLastLocation>> = _userCurrentLocations

    private val _groupWithLocation = MutableStateFlow<List<GroupDataWithLocation>>(emptyList())
    val groupWithLocation: StateFlow<List<GroupDataWithLocation>> = _groupWithLocation

    private val listeners = mutableMapOf<String, ValueEventListener>()

    private val _archiveLocationList = MutableStateFlow<List<List<ArchiveLocationData>>>(emptyList())
    val archiveLocationList: StateFlow<List<List<ArchiveLocationData>>> = _archiveLocationList


    init {
        loadFromSharedPrefs()
    }

    private fun loadFromSharedPrefs() {
        _user.value = sharedPref.getUser()
        _group.value = sharedPref.getGroup()
        _groupList.value = sharedPref.getGroupList()
        _currentGroupId.value = sharedPref.getGroupId()
    }

    fun setError(msg: String) {
        _error.value = msg
    }

    fun clearError() {
        _error.value = null
    }
    fun loadUserData(userId: String) {
        viewModelScope.launch {
            val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
            if(_group.value == null) {
                _isLoading.value = true
            }

            dbRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        Log.d("GroupViewModel", "User Data: $user")
                        _user.value = user
                        sharedPref.saveUser(user)
                        val groupList = user.groups.entries.map { (key, value) ->
                            GroupMember(id = key, name = value)
                        }
                        val sortedGroupList = groupList.sortedBy { it.name }
                        _groupList.value = sortedGroupList
                        sharedPref.saveGroupList(sortedGroupList)
                        if (sortedGroupList.isNotEmpty()) {
                            loadGroupById(sortedGroupList[0].id){}
                        }
                    } else {
                        Log.w("GroupViewModel", "User data is null")
                        _user.value = null
                    }
                } else {
                    Log.w("GroupViewModel", "User snapshot does not exist")
                }
                _isLoading.value = false
            }.addOnFailureListener { exception ->
                setError(exception.message.toString())
                Log.e("GroupViewModel", "Error loading user data for $userId", exception)
                _isLoading.value = false
            }
        }
    }

    fun loadGroupById(groupId: String, onSuccess:() -> Unit) {
        viewModelScope.launch {
            val dbRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupId)
//            _isLoading.value = true
            _currentGroupId.value = groupId

            dbRef.get().addOnSuccessListener { snapshot ->
                Log.d("GroupViewModel" , "SnapShot: \n$snapshot ")
                if (snapshot.exists()) {
                    val group = snapshot.getValue(Group::class.java)
                    _group.value = group

                    /**
                     *@author Robin Kumar
                     * @see : we are calling the function to listen for current group location update
                     * but first clear all teh current listeners
                     * */
                    _userCurrentLocations.value = mutableMapOf()
                    _groupWithLocation.value = emptyList()
                    sharedPref.saveGroupId(groupId)
                    fetchGroupUsersWithLocation()

                } else {
                    Log.w("GroupViewModel", "Group not found for ID: $groupId")
                    _group.value = null
                }
                _isLoading.value = false
                onSuccess()
            }.addOnFailureListener { exception ->
                setError(exception.message.toString())
                Log.e("GroupViewModel", "Error loading group data for $groupId", exception)
                _group.value = null
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
                                setError(exception.message.toString())
                                exception.printStackTrace()
                                _isLoading.value = false
                            }
                    }
                } else {
                    _isLoading.value = false
                    attemptCreateUniqueGroup(retryCount + 1, maxRetries)
                }
            }.addOnFailureListener { exception ->
                setError(exception.message.toString())
                _isLoading.value = false
                exception.printStackTrace()
                Log.e("GroupCreation", "Error checking group ID existence", exception)
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
                    loadUserData(userId)
                    return Transaction.success(currentData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    _isLoading.value = false
                    error?.toException()?.printStackTrace()
                }
            })
        }
    }

    fun joinNewCircle(groupId: String, userId: String, userName: String, userEmail: String, userProfileUrl: String) {
        if (_user.value != null && _user.value!!.groups.containsKey(groupId)) {
            _error.value = "You are already part of this group"
            return
        }

        val database = FirebaseDatabase.getInstance()
        val groupRef = database.getReference("Groups").child(groupId)

        // 1. Check if group exists
        groupRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                _error.value = "Group does not exist"
                return@addOnSuccessListener
            }

            val group = snapshot.getValue(Group::class.java)

            val groupName = group?.name ?: ""
            // 2. Add user to the group’s user list
            val userDataMap = mapOf(
                "name" to userName,
                "profileImageUrl" to userProfileUrl
            )

            database.getReference("Groups/$groupId/users/$userId")
                .setValue(userDataMap)
                .addOnSuccessListener {
                    Log.d("JoinCircle", "User added to group successfully")
                }
                .addOnFailureListener { e ->
                    setError(e.message.toString())
                    e.printStackTrace()
                    _error.value = "Failed to add user to group: ${e.message}"
                }

            // 3. Ensure the user exists and has the group added
            addToUser(userId, userName, userEmail, groupId, groupName, userProfileUrl)



        }.addOnFailureListener { e ->
            setError(e.message.toString())
            e.printStackTrace()
            _error.value = "Group not found: ${e.message}"
        }
    }

    fun fetchGroupUsersWithLocation() {
        val groupValue = _group.value ?: return
        val usersMap = groupValue.users ?: return
        val dbRef =
            FirebaseDatabase.getInstance().getReference("Location").child("LastRecordedLocation")
        for ((userId, userData) in usersMap) {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val locationData = if (snapshot.exists()) {
                        snapshot.getValue(UserLastLocation::class.java)
                    } else {
                        null
                    }
                    Log.d("Success", snapshot.toString())
                    if (locationData != null) {
                        val groupWithLocationItem = GroupDataWithLocation(
                            id = userId,
                            userData = userData,
                            locationData = locationData
                        )
                        _userCurrentLocations.value[userId] = locationData
                        _userCurrentLocations.value = _userCurrentLocations.value

                        val currentList = _groupWithLocation.value.toMutableList()
                        val existingIndex = currentList.indexOfFirst { it.id == userId }

                        if (existingIndex != -1) {
                            currentList[existingIndex] = groupWithLocationItem
                        } else {
                            currentList.add(groupWithLocationItem)
                        }

                        _groupWithLocation.value = currentList.toList()

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Failure", "Failed the fetch the location details from the db")
                }
            }
            listeners[userId] = listener
            dbRef.child(userId).addValueEventListener(listener)
        }
    }

    fun fetchLocationsFromArchive(userId: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Location")
            .child("LocationArchive")
            .child(userId)

        val calendar = Calendar.getInstance()
        val allDaysData = mutableListOf<Pair<String, List<ArchiveLocationData>>>()

        var pendingCalls = 5
        _isLoadingForArchive.value = true

        repeat(5) {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val formattedDate = String.format("%04d-%02d-%02d", year, month, day)

            val dayRef = dbRef.child(year.toString())
                .child(month.toString())
                .child(day.toString())

            dayRef.get().addOnSuccessListener { snapshot ->
                val dayList = mutableListOf<ArchiveLocationData>()
                for (childSnapshot in snapshot.children) {
                    val dataMap = childSnapshot.value as? Map<*, *> ?: continue

                    val data = ArchiveLocationData(
                        activity = dataMap["activity"] as? String,
                        address = dataMap["address"] as? String,
                        battery = (dataMap["battery"] as? Long)?.toInt(),
                        latitude = dataMap["latitude"] as? Double,
                        longitude = dataMap["longitude"] as? Double,
                        timeStamp = dataMap["timeStamp"] as? Long
                    )

                    dayList.add(data)
                }
                val sortedDayList = dayList.sortedBy { it.timeStamp } //sort the data by time...
                if (sortedDayList.isNotEmpty()) {
                    allDaysData.add(Pair(formattedDate, sortedDayList))
                }

                pendingCalls--
                if (pendingCalls == 0) {
                    _archiveLocationList.value = HelperFunctions.filterArchiveLocationData(locationData = allDaysData)
                    _isLoadingForArchive.value = false
                }

            }.addOnFailureListener {
                pendingCalls--
                if (pendingCalls == 0) {


                    _archiveLocationList.value = HelperFunctions.filterArchiveLocationData(locationData = allDaysData)
                    _isLoadingForArchive.value = false
                }
            }

            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }
    }

    fun stopListening() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Location").child("LastRecordedLocation")
        listeners.forEach { (userId, listener) ->
            dbRef.child(userId).removeEventListener(listener)
        }
        listeners.clear()
    }

    fun savePlace(
        placeData: SavedPlaceData,
        userId: String,
        context: Context
    ) {
        _isLoading.value = true
        val dbRef = FirebaseDatabase.getInstance().getReference("PlaceCheckins").child(userId)
        val newRef = dbRef.push()
        val newId = newRef.key ?: return

        val savedPlace = SavedPlace(
            id = newId,
            placeName = placeData.placeName,
            lat = placeData.lat,
            lng = placeData.lng,
            radiusInFeet = placeData.radiusInFeet
        )

        newRef.setValue(savedPlace)
            .addOnSuccessListener {
                _isLoading.value = false
                val sharedPrefManager = SharedPreferenceManager(context)
                val updatedList = sharedPrefManager.getPlaceCheckins() + savedPlace
                sharedPrefManager.savePlaceCheckins(updatedList)
                sharedPrefManager.saveIsPlaceCheckInCalled(true)
                Toast.makeText(context, "Place saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                _isLoading.value = false
                Toast.makeText(context, "Failed to save place", Toast.LENGTH_SHORT).show()
            }
    }

    fun updatePlaceCheckingIn(
        placeId: String,
        updatedPlaceData: SavedPlaceData,
        userId: String,
        context: Context
    ) {
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("PlaceCheckins")
            .child(userId)
            .child(placeId)

        _isLoading.value = true

        // Include the ID in the data being saved
        val updatedPlace = SavedPlace(
            id = placeId,
            placeName = updatedPlaceData.placeName,
            lat = updatedPlaceData.lat,
            lng = updatedPlaceData.lng,
            radiusInFeet = updatedPlaceData.radiusInFeet
        )

        dbRef.setValue(updatedPlace)
            .addOnSuccessListener {
                _isLoading.value = false

                val sharedPrefManager = SharedPreferenceManager(context)
                val existingList = sharedPrefManager.getPlaceCheckins()

                val updatedList = existingList.map {
                    if (it.id == placeId) updatedPlace else it
                }

                sharedPrefManager.savePlaceCheckins(updatedList)

                Toast.makeText(context, "Place updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                _isLoading.value = false
                Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
            }
    }


    fun deletePlaceCheckingIn(
        placeId: String,
        userId: String,
        context: Context
    ) {
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("PlaceCheckins")
            .child(userId)
            .child(placeId)

        _isLoading.value = true
        dbRef.removeValue()
            .addOnSuccessListener {
                val sharedPrefManager = SharedPreferenceManager(context)
                val existingList = sharedPrefManager.getPlaceCheckins()
                val updatedList = existingList.filterNot { it.id == placeId }
                sharedPrefManager.savePlaceCheckins(updatedList)
                _isLoading.value = false
                Toast.makeText(context, "Place deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                _isLoading.value = false
                Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
            }
    }

    fun getAllPlaceCheckins(
        userId: String,
        context: Context
    ) {
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("PlaceCheckins")
            .child(userId)

        dbRef.get()
            .addOnSuccessListener { dataSnapshot ->
                val placeList = mutableListOf<SavedPlace>()
                for (child in dataSnapshot.children) {
                    sharedPref.saveIsPlaceCheckInCalled(true)
                    val place = child.getValue(SavedPlace::class.java)
                    if (place != null) {
                        placeList.add(place)
                    }
                }
                val sharedPrefManager = SharedPreferenceManager(context)
                sharedPrefManager.savePlaceCheckins(placeList)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
    }

    fun saveFCMTokenForTheDevice(
        fcmToken: String,
        userId: String,
        profileUrl: String,
        userName: String
    ) {
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("FcmTokens")
            .child("Users")
            .child(userId)

        val map = mapOf(
            "fcmToken" to fcmToken,
            "profileUrl" to profileUrl,
            "userName" to userName,
            "timestamp" to System.currentTimeMillis()
        )

        dbRef.setValue(map)
            .addOnSuccessListener {
                sharedPref.saveFCMTokenString(fcmToken)
                sharedPref.saveIsFCMTokenCalled(true)
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Failed to save FCM token: ${e.message}", e)
            }
    }
}