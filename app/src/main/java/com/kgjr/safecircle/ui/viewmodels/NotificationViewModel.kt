package com.kgjr.safecircle.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
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

    private val _groupList = MutableStateFlow<List<GroupMember>>(emptyList())
    val groupList: StateFlow<List<GroupMember>> = _groupList

    private val _currentGroupId = MutableStateFlow<String?>(null)
    val currentGroupId : StateFlow<String?> = _currentGroupId

    private val _isLoading = MutableStateFlow<Set<String>>(emptySet())
    val isLoading : StateFlow<Set<String>> = _isLoading

    init {
        loadFromSharedPrefs()
    }

    private fun loadFromSharedPrefs() {
        _user.value = sharedPref.getUser()
        _group.value = sharedPref.getGroup()
        _groupList.value = sharedPref.getGroupList()
        _currentGroupId.value = sharedPref.getGroupId()
    }

    fun getUserData(groupId: String){
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child("")
    }

}