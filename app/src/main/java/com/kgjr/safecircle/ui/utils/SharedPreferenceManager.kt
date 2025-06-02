package com.kgjr.safecircle.ui.utils

import android.content.Context
import android.location.Location
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kgjr.safecircle.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.core.content.edit

class SharedPreferenceManager(context: Context) {

    private val sharedPreferences = context.applicationContext.getSharedPreferences("Safe_Circle_Shared_Preferences", Context.MODE_PRIVATE)
    private val gson = Gson()

    // ---- User ----
    fun saveUser(user: User?) {
        val json = gson.toJson(user)
        sharedPreferences.edit { putString("user", json) }
    }

    fun getUser(): User? {
        val json = sharedPreferences.getString("user", null)
        val user = json?.let { gson.fromJson(it, User::class.java) }
        return user
    }

    // ---- Group ----
    fun saveGroup(group: Group?) {
        val json = gson.toJson(group)
        sharedPreferences.edit { putString("group", json) }
    }

    fun getGroup(): Group? {
        val json = sharedPreferences.getString("group", null)
        val group = json?.let { gson.fromJson(it, Group::class.java) }
        return group
    }

    // ---- User Groups ----
    fun saveGroupList(userGroups: List<GroupMember>) {
        val json = gson.toJson(userGroups)
        sharedPreferences.edit { putString("user_groups", json) }
    }

    fun getGroupList(): List<GroupMember> {
        val json = sharedPreferences.getString("user_groups", null)
        val userGroups = if (json != null) {
            val type = object : TypeToken<List<GroupMember>>() {}.type
            gson.fromJson<List<GroupMember>>(json, type)
        } else {
            emptyList()
        }
        return userGroups
    }

    // ---- Current Group Id ----
    fun saveGroupId(groupId: String) {
        sharedPreferences.edit { putString("group_id", groupId) }
    }

    fun getGroupId(): String? {
        return sharedPreferences.getString("group_id", null)
    }


    // ---- Last Location ----
    fun saveLastLocation(lat: Double, lng: Double) {
        sharedPreferences.edit {
            putString("latitude", lat.toString())
            putString("longitude", lng.toString())
        }
    }

    fun getLastLocation(): Location? {
        val lat = sharedPreferences.getString("latitude", null)?.toDoubleOrNull()
        val lng = sharedPreferences.getString("longitude", null)?.toDoubleOrNull()

        return if (lat != null && lng != null) {
            Location("SharedPreference").apply {
                latitude = lat
                longitude = lng
            }
        } else {
            null
        }
    }
    // ---- Last Activity Status ----
    fun saveLastActivityStatus(status: String) {
        sharedPreferences.edit {
            putString("last_activity_status", status)
        }
    }

    fun getLastActivityStatus(): String? {
        return sharedPreferences.getString("last_activity_status", null)
    }

    // ---- Last Activity Timestamp ----
    fun saveLastActivityTimestamp(timestamp: Long) {
        sharedPreferences.edit {
            putLong("last_activity_timestamp", timestamp)
        }
    }

    fun getLastActivityTimestamp(): Long {
        return sharedPreferences.getLong("last_activity_timestamp", 0L)
    }

    // ---- Clear all SharedPreferences data ----//
    fun clearSharedPreference() {
        /**
        * @Mark: Always call this when we log out of the application.
        * */
        sharedPreferences.edit { clear() }
    }
}