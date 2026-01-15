package com.kgjr.safecircle.ui.utils

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.models.LocationForApiTest.XyzLocationResponse
import com.kgjr.safecircle.models.NominatimResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar
import java.util.Locale

object BackgroundApiManagerUtil {

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun archiveLocationData(
        context: Context,
        userId: String,
        activityType: String?,
        batteryPercentage: Int,
        address: String,
        timeStamp: Long = System.currentTimeMillis(),
        sharedPreferenceManager: SharedPreferenceManager,
        notificationService: NotificationService
    ) {
//        LocationUtils.getCurrentLocation(context) { location ->
//            if (location != null) {
//                val lat = location.latitude
//                val lng = location.longitude
//
//                val calendar = Calendar.getInstance()
//                val year = calendar.get(Calendar.YEAR)
//                val month = calendar.get(Calendar.MONTH) + 1
//                val day = calendar.get(Calendar.DAY_OF_MONTH)
//
//                val locRef =
//                    FirebaseDatabase.getInstance().getReference("Location").child("LocationArchive")
//                        .child(userId)
//                        .child(year.toString())
//                        .child(month.toString())
//                        .child(day.toString())
//
//                val dataToAppendInTheList =
//                    if(activityType == null) {
//                        mapOf(
//                            "latitude" to lat,
//                            "longitude" to lng,
//                            "timeStamp" to timeStamp,
//                            "activity" to sharedPreferenceManager.getLastActivityStatus(),
//                            "battery" to batteryPercentage,
//                            "address" to address
//                        )
//                    }else{
//                        mapOf(
//                            "latitude" to lat,
//                            "longitude" to lng,
//                            "timeStamp" to timeStamp,
//                            "activity" to activityType,
//                            "battery" to batteryPercentage,
//                            "address" to address
//                        )
//                    }
//                locRef.push().setValue(dataToAppendInTheList)
//                    .addOnSuccessListener {
//                        Log.d("Firebase", "Location data archived successfully.")
//                        notificationService.cancelUpdateLocationNotification()
//                        notificationService.cancelWorkerNotification()
//                        if(activityType != null){
//                            sharedPreferenceManager.saveLastActivityStatus(activityType)
//                        }
//                        sharedPreferenceManager.saveLastLocation(lat = lat, lng = lng)
//                    }
//                    .addOnFailureListener { exception ->
//                        Log.e("Firebase", "Failed to archive location data: ${exception.message}")
//                    }
//            } else {
//                Log.w("Location", "Location is null. Data not archived.")
//            }
//        }
    }



    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun saveLastRecordedLocation(
        context: Context,
        userId: String,
        activityType: String?,
        batteryPercentage: Int,
        address: String,
        timeStamp: Long = System.currentTimeMillis(),
        sharedPreferenceManager: SharedPreferenceManager
    ) {
//        LocationUtils.getCurrentLocation(context) { location ->
//            if (location != null) {
//                val lat = location.latitude
//                val lng = location.longitude
//
//                val locRef = FirebaseDatabase.getInstance().getReference("Location")
//                    .child("LastRecordedLocation")
//                    .child(userId)
//
//                val dataToSet =
//                    if(activityType == null) {
//                        mapOf(
//                            "latitude" to lat,
//                            "longitude" to lng,
//                            "timeStamp" to timeStamp,
//                            "activity" to sharedPreferenceManager.getLastActivityStatus(),
//                            "battery" to batteryPercentage,
//                            "address" to address
//                        )
//                    }else{
//                        mapOf(
//                            "latitude" to lat,
//                            "longitude" to lng,
//                            "timeStamp" to timeStamp,
//                            "activity" to activityType,
//                            "battery" to batteryPercentage,
//                            "address" to address
//                        )
//                    }
//                locRef.setValue(dataToSet)
//                    .addOnSuccessListener {
//                        Log.d("Firebase", "Last recorded location saved successfully.")
//                    }
//                    .addOnFailureListener { exception ->
//                        Log.e(
//                            "Firebase",
//                            "Failed to save last recorded location: ${exception.message}"
//                        )
//                    }
//            } else {
//                Log.w("Location", "Location is null. Last recorded location not saved.")
//            }
//        }
    }

    fun archiveLocationDataV2(
        userId: String,
        activityType: String?,
        batteryPercentage: Int,
        address: String,
        timeStamp: Long = System.currentTimeMillis(),
        sharedPreferenceManager: SharedPreferenceManager,
        notificationService: NotificationService,
        currentLocation: Location?,
        onCompletion: () -> Unit
    ) {
        Log.d("FirebaseV2", "In Location Archive...")
        currentLocation?.let { location ->
            val lat = location.latitude
            val lng = location.longitude

            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val locRef =
                FirebaseDatabase.getInstance()
                    .getReference("Location")
                    .child("LocationArchive")
                    .child(userId)
                    .child(year.toString())
                    .child(month.toString())
                    .child(day.toString())

            val dataToAppendInTheList =
                if (activityType == null) {
                    mapOf(
                        "latitude" to lat,
                        "longitude" to lng,
                        "timeStamp" to timeStamp,
                        "activity" to sharedPreferenceManager.getLastActivityStatus(),
                        "battery" to batteryPercentage,
                        "address" to address
                    )
                } else {
                    mapOf(
                        "latitude" to lat,
                        "longitude" to lng,
                        "timeStamp" to timeStamp,
                        "activity" to activityType,
                        "battery" to batteryPercentage,
                        "address" to address
                    )
                }

            val handler = Handler(Looper.getMainLooper())
            var completed = false // Flag to ensure onCompletion is called only once

            val timeoutRunnable = Runnable {
                if (!completed) {
                    notificationService.cancelUpdateLocationNotification()
                    notificationService.cancelWorkerNotification()
                    Log.e("FirebaseV2", "Firebase operation timed out after 15 seconds.")
                    if (activityType != null) {
                        sharedPreferenceManager.saveLastActivityStatus(activityType)
                    }
                    sharedPreferenceManager.saveLastLocation(lat = lat, lng = lng)
                    archiveLocationLocal( dataToAppendInTheList as Map<String, Any>,sharedPreferenceManager)
                    onCompletion()
                    completed = true
                }
            }

            // Post the timeout runnable with a delay of 15 seconds
            handler.postDelayed(timeoutRunnable, 15000)

            locRef.push().setValue(dataToAppendInTheList)
                .addOnSuccessListener {
                    if (!completed) {
                        Log.d("FirebaseV2", "Location data archived successfully.")
                        notificationService.cancelUpdateLocationNotification()
                        notificationService.cancelWorkerNotification()
                        if (activityType != null) {
                            sharedPreferenceManager.saveLastActivityStatus(activityType)
                        }
                        sharedPreferenceManager.saveLastLocation(lat = lat, lng = lng)
                        onCompletion()
                        completed = true
                        handler.removeCallbacks(timeoutRunnable)
                    }
                }
                .addOnFailureListener { exception ->
                    if (!completed) {
                        notificationService.cancelUpdateLocationNotification()
                        notificationService.cancelWorkerNotification()
                        Log.e("FirebaseV2", "Failed to archive location data: ${exception.message}")
                        if (activityType != null) {
                            sharedPreferenceManager.saveLastActivityStatus(activityType)
                        }
                        sharedPreferenceManager.saveLastLocation(lat = lat, lng = lng)
                        onCompletion()
                        archiveLocationLocal( dataToAppendInTheList as Map<String, Any>,sharedPreferenceManager)
                        completed = true
                        handler.removeCallbacks(timeoutRunnable)
                    }
                }
        }
    }

    fun saveLastRecordedLocationV2(
        userId: String,
        activityType: String?,
        batteryPercentage: Int,
        address: String,
        timeStamp: Long = System.currentTimeMillis(),
        sharedPreferenceManager: SharedPreferenceManager,
        currentLocation: Location?,
        onCompletion: () -> Unit
    ) {
        currentLocation?.let { location ->
            val lat = location.latitude
            val lng = location.longitude

            ///as soon as new location come try to send the notification to all.
            sendNotificationForPlaceChecking(lat = lat, lng = lng)

            val locRef = FirebaseDatabase.getInstance().getReference("Location")
                .child("LastRecordedLocation")
                .child(userId)

            val dataToSet =
                if (activityType == null) {
                    mapOf(
                        "latitude" to lat,
                        "longitude" to lng,
                        "timeStamp" to timeStamp,
                        "activity" to sharedPreferenceManager.getLastActivityStatus(),
                        "battery" to batteryPercentage,
                        "address" to address
                    )
                } else {
                    mapOf(
                        "latitude" to lat,
                        "longitude" to lng,
                        "timeStamp" to timeStamp,
                        "activity" to activityType,
                        "battery" to batteryPercentage,
                        "address" to address
                    )
                }

            val handler = Handler(Looper.getMainLooper())
            var completed = false // Flag to ensure onCompletion is called only once

            val timeoutRunnable = Runnable {
                if (!completed) {
                    Log.e("FirebaseV2", "Firebase operation for last recorded location timed out after 30 seconds.")
                    onCompletion()
                    completed = true
                }
            }

            // Post the timeout runnable with a delay of 15 seconds
            handler.postDelayed(timeoutRunnable, 15000) // 30000 milliseconds = 30 seconds

            locRef.setValue(dataToSet)
                .addOnSuccessListener {

                    if (!completed) {
                        Log.d("FirebaseV2", "Last recorded location saved successfully.")
                        onCompletion()
                        completed = true
                        handler.removeCallbacks(timeoutRunnable)
                    }
                }
                .addOnFailureListener { exception ->
                    if (!completed) {
                        Log.e(
                            "FirebaseV2",
                            "Failed to save last recorded location: ${exception.message}"
                        )
                        onCompletion()
                        completed = true
                        handler.removeCallbacks(timeoutRunnable)
                    }
                }
        }
    }

    fun getAndLogAddressFromLatLngNormApi(
        lat: Double,
        lng: Double,
        onSuccess: (NominatimResponse?) -> Unit,
    ) {
        val client = OkHttpClient()
        val url = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lng&format=json"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent",
                MainApplication.getGoogleAuthUiClient().getSignedInUser()?.email.toString()
            )
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        Log.d("NominatimAPI", "Raw API Response: $responseBody")

                        val gson = Gson()
                        val nominatimResponse =
                            gson.fromJson(responseBody, NominatimResponse::class.java)

                        Log.d(
                            "NominatimAPI",
                            "Parsed Display Name: ${nominatimResponse.displayName}"
                        )
                        Log.d(
                            "NominatimAPI",
                            "Parsed Country: ${nominatimResponse.address.country}"
                        )
                        Log.d("NominatimAPI", "Parsed State: ${nominatimResponse.address.state}")
                        Log.d(
                            "NominatimAPI",
                            "Parsed Village: ${nominatimResponse.address.village}"
                        )
                        Log.d(
                            "NominatimAPI",
                            "Full Parsed Object: $nominatimResponse"
                        )
                        onSuccess(nominatimResponse)
                    } else {
                        Log.e("NominatimAPI", "Empty response body.")
                        onSuccess(null)
                    }
                } else {
                    Log.e(
                        "NominatimAPI",
                        "API call failed with code: ${response.code}, message: ${response.message}"
                    )
                    onSuccess(null)
                }
            } catch (e: IOException) {
                Log.e("NominatimAPI", "Network error: ${e.message}")
                onSuccess(null)
            } catch (e: Exception) {
                Log.e("NominatimAPI", "Error parsing response or unexpected data: ${e.message}")
                e.printStackTrace()
                onSuccess(null)
            }
        }
    }


    fun getAndLogAddressFromLatLngXYZApi(
        lat: Double,
        lng: Double,
        onSuccess: (XyzLocationResponse?) -> Unit,
    ) {
        val client = OkHttpClient()
        val url = "https://geocode.xyz/$lat,$lng?geoit=json"

        val request = Request.Builder()
            .url(url)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        Log.d("XYZSimpleAddress", "Raw API Response: $responseBody")

                        val gson = Gson()
                        val xyzApiResponse =
                            gson.fromJson(responseBody, XyzLocationResponse::class.java)

                        Log.d(
                            "XYZSimpleAddress",
                            "Parsed Display Name: ${xyzApiResponse.standard?.addressT}"
                        )
                        Log.d(
                            "XYZSimpleAddress",
                            "Parsed Country: ${xyzApiResponse.standard?.countryname}"
                        )
                        onSuccess(xyzApiResponse)
                    } else {
                        Log.e("XYZSimpleAddress", "Empty response body.")
                        onSuccess(null)
                    }
                } else {
                    Log.e(
                        "XYZSimpleAddress",
                        "API call failed with code: ${response.code}, message: ${response.message}"
                    )
                    onSuccess(null)
                }
            } catch (e: IOException) {
                Log.e("XYZSimpleAddress", "Network error: ${e.message}")
                onSuccess(null)
            } catch (e: Exception) {
                Log.e("XYZSimpleAddress", "Error parsing response or unexpected data: ${e.message}")
                e.printStackTrace()
                onSuccess(null)
            }
        }
    }

    fun archiveLocationLocal(data: Map<String, Any>,sharedPreferenceManager: SharedPreferenceManager,) {
        Log.d("FirebaseV2", "data: $data")
        sharedPreferenceManager.archiveLocationLocal(data)
    }

    fun uploadAllPendingData() {
        val userId = MainApplication.getGoogleAuthUiClient().getSignedInUser()?.userId ?: return
        val sharedPref = MainApplication.getSharedPreferenceManager()
        val allData = sharedPref.getArchivedLocations().toMutableList()

        if (allData.isEmpty()) {
            Log.d("FirebaseV2", "No pending location data to upload.")
            return
        }

        // Make a copy of the original list to iterate over safely
        val dataToUpload = allData.toList()

        for (data in dataToUpload) {
            val lat = data["latitude"]
            val lng = data["longitude"]
            val timeStamp = (data["timeStamp"] as? Number)?.toLong() ?: continue
            ///try sending the notification for the late
            if(lat != null && lng != null) {
                sendNotificationForPlaceChecking(lat = lat as Double, lng = lng as Double)
            }

            val activity = data["activity"]
            val battery = data["battery"]
            val address = data["address"]

            val calendar = Calendar.getInstance().apply { timeInMillis = timeStamp }
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val locRef = FirebaseDatabase.getInstance()
                .getReference("Location")
                .child("LocationArchive")
                .child(userId)
                .child(year.toString())
                .child(month.toString())
                .child(day.toString())

            val uploadData = mapOf(
                "latitude" to lat,
                "longitude" to lng,
                "timeStamp" to timeStamp,
                "activity" to activity,
                "battery" to battery,
                "address" to address
            )
            locRef.push().setValue(uploadData)
                .addOnSuccessListener {

                    Log.d("FirebaseV2", "Uploaded pending data: $uploadData")
                    allData.remove(data)
                    sharedPref.updateArchivedLocations(allData)
                    if (allData.isEmpty()) {
                        sharedPref.clearArchivedLocations()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseV2", "Failed to upload pending data: ${e.message}")
                }
        }
    }

    fun sendNotificationForPlaceChecking(lat: Double, lng: Double, isPendingNotification: Boolean = false,time: Long? = null ) {
        val sharedPreferenceManager = MainApplication.getSharedPreferenceManager()

        val lastTriggerTime = sharedPreferenceManager.getLastPlaceCheckTriggerTime()
        val currentTime = System.currentTimeMillis()
        val tenMinutesInMillis = 10 * 60 * 1000 // minimum 10 min gap b.w the notification

        if (currentTime - lastTriggerTime < tenMinutesInMillis) {
            return
        }
        sharedPreferenceManager.saveLastPlaceCheckTriggerTime(currentTime)

        val placeCheckins = sharedPreferenceManager.getPlaceCheckins()
        val userIdsForNotification = sharedPreferenceManager.getUserIdsForNotification()
        val user = MainApplication.getGoogleAuthUiClient().getSignedInUser()
        val userName = user?.userName ?: "Someone"
        val profileImageUrl = user?.profileUrl ?: ""
        val lastSavedLocation = sharedPreferenceManager.getLastLocation()

        if (placeCheckins.isEmpty() || userIdsForNotification.isEmpty()) return

        val dbRef = FirebaseDatabase.getInstance().getReference("FcmTokens/Users")
        val client = OkHttpClient()
        val apiUrl = "https://sendcustomnotification-yshvdyz2ka-uc.a.run.app"

        CoroutineScope(Dispatchers.IO).launch {
            for (place in placeCheckins) {
                val distance = FloatArray(1)
                Location.distanceBetween(lat, lng, place.lat, place.lng, distance)
                val isInside = distance[0] <= (place.radiusInFeet * 0.3048)  //converting the distance into meters from feats

                val wasInside = lastSavedLocation?.let {
                    val prevDistance = FloatArray(1)
                    Location.distanceBetween(it.latitude, it.longitude, place.lat, place.lng, prevDistance)
                    prevDistance[0] <= (place.radiusInFeet * 0.3048)
                } ?: false

                val movementType = when {
                    !wasInside && isInside -> "entered"
                    wasInside && !isInside -> "left"
                    else -> null
                } ?: continue

                for (userId in userIdsForNotification) {
                    dbRef.child(userId).child("fcmToken").get()
                        .addOnSuccessListener { snapshot ->
                            val token = snapshot.getValue(String::class.java)
                            if (token != null) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val json = JSONObject().apply {
                                        put("token", token)
                                        var finalMessage = ""
                                        if (isPendingNotification) {
                                            val eventTime = time ?: System.currentTimeMillis()
                                            val calendar = Calendar.getInstance().apply { timeInMillis = eventTime }
                                            val now = Calendar.getInstance()

                                            val datePrefix = when {
                                                // Check if same day
                                                calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                                                        calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) -> "Earlier today"

                                                // Check if yesterday
                                                calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                                                        now.get(Calendar.DAY_OF_YEAR) - calendar.get(Calendar.DAY_OF_YEAR) == 1 -> "Yesterday"

                                                // Older dates
                                                else -> java.text.SimpleDateFormat("MMM dd", Locale.getDefault()).format(eventTime)
                                            }

                                            val formattedTime = java.text.SimpleDateFormat("hh:mm a", Locale.getDefault()).format(eventTime)
                                            finalMessage = "$datePrefix at $formattedTime, $userName $movementType ${place.placeName}!"

                                        }else{
                                            finalMessage = "$userName has $movementType ${place.placeName}!"
                                        }
                                        put("message", finalMessage)
                                        if (profileImageUrl.isNotEmpty()) put("imageUrl", profileImageUrl)
                                    }

                                    val body = RequestBody.create(
                                        "application/json".toMediaTypeOrNull(),
                                        json.toString()
                                    )

                                    val request = Request.Builder()
                                        .url(apiUrl)
                                        .post(body)
                                        .addHeader("Content-Type", "application/json")
                                        .build()

                                    runCatching { client.newCall(request).execute().close() }
                                }
                            }
                        }
                }
            }
        }
    }


    fun sendSosNotification(lat: Double, lng: Double) {
        val sharedPreferenceManager = MainApplication.getSharedPreferenceManager()
        val userIdsForNotification = sharedPreferenceManager.getUserIdsForNotification()
        val user = MainApplication.getGoogleAuthUiClient().getSignedInUser()

        val userName = user?.userName ?: "Someone"
        val profileImageUrl = user?.profileUrl ?: ""

        if (userIdsForNotification.isEmpty()) {
            Log.w("SOSNotification", "No user IDs found for notification.")
            return
        }

        val dbRef = FirebaseDatabase.getInstance().getReference("FcmTokens/Users")
        val client = OkHttpClient()
        val apiUrl = "https://sendsosnotification-yshvdyz2ka-uc.a.run.app"

        for (userId in userIdsForNotification) {
            dbRef.child(userId).child("fcmToken").get()
                .addOnSuccessListener { snapshot ->
                    val token = snapshot.getValue(String::class.java)
                    if (token != null) {
                        Log.d("SOSNotification", "Fetched FCM token for user : $token")
                        // Immediately send SOS for this token
                        CoroutineScope(Dispatchers.IO).launch {
                            val json = JSONObject().apply {
                                put("token", token)
                                put("lat", lat.toString())
                                put("lng", lng.toString())
                                put("name", userName)
                                if (profileImageUrl.isNotEmpty()) {
                                    put("profileImageUrl", profileImageUrl)
                                }
                            }

                            val body = RequestBody.create(
                                "application/json".toMediaTypeOrNull(),
                                json.toString()
                            )

                            val request = Request.Builder()
                                .url(apiUrl)
                                .post(body)
                                .addHeader("Content-Type", "application/json")
                                .build()

                            runCatching {
                                client.newCall(request).execute().use { response ->
                                    if (response.isSuccessful) {
                                        Log.d("SOSNotification", "SOS sent successfully ")
                                    } else {
                                        Log.e("SOSNotification", "Failed to send SOS to: ${response.code}")
                                    }
                                }
                            }.onFailure {
                                Log.e("SOSNotification", "Error sending SOS ", it)
                            }
                        }
                    } else {
                        Log.w("SOSNotification", "No FCM token found for user")
                    }
                }
                .addOnFailureListener {
                    Log.e("SOSNotification", "Failed to fetch FCM token for user", it)
                }
        }
    }

}
