package com.kgjr.safecircle.ui.utils

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kgjr.safecircle.models.*
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
        return sharedPreferences.getString("last_activity_status", "N.A")
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


    // ---- Last Location When Address is Fetch ----
    fun saveLastTimeForAddressApi(time: Long) {
        sharedPreferences.edit{
            putLong("last_time_for_getting_location", time)
        }
    }

    fun getLastTimeForAddressApi(): Long{
        return sharedPreferences.getLong("last_time_for_getting_location", 0L)
    }

    // ---- Last Location Address from Nominatim ---
    fun saveLocationActualAddressForApi(address: String){
        sharedPreferences.edit{
            putString("last_location_address_from_nominatim", address)
        }
    }

    fun getActualAddressForApi(): String? {
        return sharedPreferences.getString("last_location_address_from_nominatim", null)
    }

    // ---- Last Location When API is Called ----
    /**
     * @param lat The latitude to save.
     * @param lng The longitude to save.
     */
    fun saveLastLocationLatLngApi(lat: Double, lng: Double) {
        sharedPreferences.edit {
            putString("nominatim_latitude", lat.toString())
            putString("nominatim_longitude", lng.toString())
        }
    }

    /**
     * @return A Location object if a previously saved location is found, otherwise null.
     */
    fun getLastLocationLatLngApi(): Location? {
        val lat = sharedPreferences.getString("nominatim_latitude", null)?.toDoubleOrNull()
        val lng = sharedPreferences.getString("nominatim_longitude", null)?.toDoubleOrNull()

        return if (lat != null && lng != null) {
            Location("NominatimSharedPreference").apply {
                latitude = lat
                longitude = lng
            }
        } else {
            null
        }
    }

    // --- Save Looper Enabled State ---
    fun saveLooperEnabled(isEnabled: Boolean) {
        sharedPreferences.edit {
            putBoolean("is_looper_enabled", isEnabled)
        }
    }

    // --- Get Looper Enabled State, default to false ---
    fun getIsLooperEnabled(): Boolean {
        return sharedPreferences.getBoolean("is_looper_enabled", false)
    }

    // --- Save Update Location API is Called or Not---
    fun saveIsUpdateLocationApiCalled(isEnabled: Boolean) {
        sharedPreferences.edit {
            putBoolean("is_update_location_api", isEnabled)
        }
    }

    // --- Get Update Location API is Called or Not ---
    fun getIsUpdateLocationApiCalled(): Boolean {
        return sharedPreferences.getBoolean("is_update_location_api", false)
    }

    // --- Save Update Location API is Called or Not For Looper ---
    fun saveIsUpdateLocationApiCalledLooper(isEnabled: Boolean) {
        sharedPreferences.edit {
            putBoolean("is_update_location_api_looper", isEnabled)
        }
    }

    // --- Get Update Location API is Called or Not For Looper ---
    fun getIsUpdateLocationApiCalledLooper(): Boolean {
        return sharedPreferences.getBoolean("is_update_location_api_looper", false)
    }


    // --- Save Place Check-ins ---
    fun savePlaceCheckins(placeList: List<SavedPlace>) {
        val json = gson.toJson(placeList)
        sharedPreferences.edit { putString("place_checkins", json) }
    }


    fun getPlaceCheckins(): List<SavedPlace> {
        val json = sharedPreferences.getString("place_checkins", null)
        return if (json != null) {
            val type = object : TypeToken<List<SavedPlace>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    // --- Is Place CheckIn Api Is Called ---
    fun getIsPlaceCheckInCalled(): Boolean {
        return sharedPreferences.getBoolean("is_place_checking_in_called", false)
    }

    // --- Save Update Location API is Called or Not---
    fun saveIsPlaceCheckInCalled(isEnabled: Boolean) {
        sharedPreferences.edit {
            putBoolean("is_place_checking_in_called", isEnabled)
        }
    }

    // --- Save selected Map Type Id---
    fun saveMapTypeId(mapType: Int) {
        sharedPreferences.edit {
            putInt("selected_map_type_id", mapType)
        }
    }

    // --- Get selected Map Type Id---
    fun getMapTypeId(): Int {
        return sharedPreferences.getInt("selected_map_type_id", 1)
    }


    // --- Save/Append location data to the local list ---
    fun archiveLocationLocal(data: Map<String, Any>) {
        val currentList = getArchivedLocations().toMutableList()
        currentList.add(data)
        val json = gson.toJson(currentList)
        sharedPreferences.edit { putString("archived_locations_local", json) }
        Log.d("FirebaseV2", "Archived: $data")
    }

    // --- Retrieve the list ---
    fun getArchivedLocations(): List<Map<String, Any>> {
        val json = sharedPreferences.getString("archived_locations_local", null) ?: return emptyList()
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            Log.e("SharedPreferences", "Failed to parse archived data", e)
            emptyList()
        }
    }
    // ---Update the archived locations ----
    fun updateArchivedLocations(list: List<Map<String, Any>>) {
        val json = Gson().toJson(list)
        sharedPreferences.edit { putString("archived_locations_local", json) }
    }

    //---- Cleat all the location archived----
    fun clearArchivedLocations() {
        sharedPreferences.edit { remove("archived_locations_local") }
    }

    // --- Save FCM Token---
    fun saveIsFCMTokenCalled(isFcmToken: Boolean) {
        sharedPreferences.edit {
            putBoolean("is_fcm_token_saved", isFcmToken)
        }
    }
    // --- Is FCM Token Saved ---
    fun getIsFCMTokenSaved(): Boolean {
        return sharedPreferences.getBoolean("is_fcm_token_saved", false)
    }

    // --- Save FCM Token String ---
    fun saveFCMTokenString(token: String) {
        sharedPreferences.edit {
            putString("fcm_token", token)
        }
    }

    // --- Get FCM Token String ---
    fun getFCMTokenString(): String? {
        return sharedPreferences.getString("fcm_token", null)
    }

    // --- set notification setup tour ---
    fun setNotificationSetupTour(isNotificationTour: Boolean) {
        sharedPreferences.edit {
            putBoolean("is_notification_tour_done", isNotificationTour)
        }
    }

    // ---get FCM Token Saved ---
    fun getNotificationSetupTour(): Boolean {
        return sharedPreferences.getBoolean("is_notification_tour_done", false)
    }

    // ---- Clear all SharedPreferences data ----//
    fun clearSharedPreference() {
        /**
        * @Mark: Always call this when we logout of the application.
        * */
        sharedPreferences.edit { clear() }
    }
}