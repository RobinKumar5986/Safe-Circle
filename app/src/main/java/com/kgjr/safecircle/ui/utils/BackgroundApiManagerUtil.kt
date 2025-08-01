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
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.Calendar

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
}
