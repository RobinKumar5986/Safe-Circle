package com.kgjr.safecircle.broadcastReceiver

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.models.NominatimResponse
import com.kgjr.safecircle.ui.utils.LocationUtils
import com.kgjr.safecircle.ui.utils.NotificationService
import com.kgjr.safecircle.ui.utils.NotificationService.Companion.UPDATE_LOCATION_NOTIFICATION_ID
import com.kgjr.safecircle.ui.utils.SharedPreferenceManager
import com.kgjr.safecircle.ui.utils.getBatteryPercentage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.Calendar

class ActivityTransitionReceiver : BroadcastReceiver() {
    companion object {
        lateinit var sharedPreferenceManager: SharedPreferenceManager

        @SuppressLint("StaticFieldLeak")
        lateinit var notificationService: NotificationService

    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS])
    override fun onReceive(context: Context, intent: Intent) {
        notificationService = NotificationService(context)
        sharedPreferenceManager = SharedPreferenceManager(context)

        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            val activity = result!!.mostProbableActivity

            val type = getActivityType(activity.type)
            val confidence = activity.confidence
            Log.d("ActivityReceiver", "Detected activity: $type with confidence: $confidence")
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    showNotification(context, type)
                }.addOnFailureListener {
                    Log.e("ActivityReceiver", "Failed to get location: ${it.message}")
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
                Log.e("ActivityReceiver", "Missing location permission.")
            }
        }
    }


    private fun getActivityType(type: Int): String {
        return when (type) {
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.WALKING -> "WALKING"
            DetectedActivity.RUNNING -> "RUNNING"
            DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
            DetectedActivity.ON_BICYCLE -> "ON_BICYCLE"
            DetectedActivity.ON_FOOT -> "ON_FOOT"
            DetectedActivity.TILTING -> "TILTING"
            DetectedActivity.UNKNOWN -> "UNKNOWN"
            else -> "OTHER"
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun showNotification(
        context: Context,
        activityType: String
    ) {

        val message = if (activityType == "IN_VEHICLE") {
            "Activating Driving Mode.."
        } else {
            "Updating Location..."
        }


        val userId = MainApplication.getGoogleAuthUiClient().getSignedInUser()?.userId
        val batterPercentage = getBatteryPercentage(context)
        val lastLocation = sharedPreferenceManager.getLastLocation()
        val lastActivity = sharedPreferenceManager.getLastActivityStatus()
        val lastActivityTime = sharedPreferenceManager.getLastActivityTimestamp()

        LocationUtils.getCurrentLocation(context) { currentLocation ->
            var shouldUpdate = false

            if ((lastActivity == "IN_VEHICLE" || lastActivity == "ON_BICYCLE" || lastActivity == "ON_FOOT") &&
                activityType != "IN_VEHICLE" && activityType != "ON_BICYCLE" && activityType != "ON_FOOT"
            ) {
                shouldUpdate = true
            }
            if (lastLocation != null && currentLocation != null) {
                val distance = lastLocation.distanceTo(currentLocation)
                Log.d("DistanceCheck", "Distance from last location: $distance meters")
                shouldUpdate = distance >= 15
            }else{
                shouldUpdate = true
            }

            //checking if its a new day
            val lastCal = Calendar.getInstance().apply { timeInMillis = lastActivityTime }
            val currentCal = Calendar.getInstance()
            val isNewDay = lastCal.get(Calendar.YEAR) != currentCal.get(Calendar.YEAR) ||
                    lastCal.get(Calendar.DAY_OF_YEAR) != currentCal.get(Calendar.DAY_OF_YEAR)
            if (isNewDay) {
                shouldUpdate = true
            }
            if (userId != null && shouldUpdate ) {
                getAndLogAddressFromLatLng(
                    lat = currentLocation?.latitude ?: 0.0,
                    lng = currentLocation?.longitude ?: 0.0
                ) { openMapData ->
                    if (openMapData != null) {
                        archiveLocationData(
                            context = context,
                            userId = userId,
                            activityType = activityType,
                            address = openMapData.displayName,
                            batteryPercentage = batterPercentage
                        )
                        saveLastRecordedLocation(
                            context = context,
                            userId = userId,
                            activityType = activityType,
                            address = openMapData.displayName,
                            batteryPercentage = batterPercentage,
                        )
                        sharedPreferenceManager.saveLastActivityStatus(activityType)
                        sharedPreferenceManager.saveLastActivityTimestamp(System.currentTimeMillis())
                    }
                }
            }
            val notification = notificationService.getUpdateLocationNotification("$message:$shouldUpdate")
            NotificationManagerCompat.from(context)
                .notify(UPDATE_LOCATION_NOTIFICATION_ID, notification)
        }
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun archiveLocationData(
        context: Context,
        userId: String,
        activityType: String,
        batteryPercentage: Int,
        address: String,
        timeStamp: Long = System.currentTimeMillis(),
    ) {
        LocationUtils.getCurrentLocation(context) { location ->

            if (location != null) {
                val lat = location.latitude
                val lng = location.longitude

                sharedPreferenceManager.saveLastLocation(lat = lat, lng = lng)
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val locRef =
                    FirebaseDatabase.getInstance().getReference("Location").child("LocationArchive")
                        .child(userId)
                        .child(year.toString())
                        .child(month.toString())
                        .child(day.toString())

                val dataToAppendInTheList = mapOf(
                    "latitude" to lat,
                    "longitude" to lng,
                    "timeStamp" to timeStamp,
                    "activity" to activityType,
                    "battery" to batteryPercentage,
                    "address" to address
                )
                locRef.push().setValue(dataToAppendInTheList)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Location data archived successfully.")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "Failed to archive location data: ${exception.message}")
                    }
            } else {
                Log.w("Location", "Location is null. Data not archived.")
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun saveLastRecordedLocation(
        context: Context,
        userId: String,
        activityType: String,
        batteryPercentage: Int,
        address: String,
        timeStamp: Long = System.currentTimeMillis(),
    ) {
        LocationUtils.getCurrentLocation(context) { location ->
            if (location != null) {
                val lat = location.latitude
                val lng = location.longitude
                sharedPreferenceManager.saveLastLocation(lat = lat, lng = lng)

                val locRef = FirebaseDatabase.getInstance().getReference("Location")
                    .child("LastRecordedLocation")
                    .child(userId)

                val dataToSet = mapOf(
                    "latitude" to lat,
                    "longitude" to lng,
                    "timeStamp" to timeStamp,
                    "activity" to activityType,
                    "battery" to batteryPercentage,
                    "address" to address
                )

                locRef.setValue(dataToSet)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Last recorded location saved successfully.")
//                        notificationService.cancelUpdateLocationNotification()
                    }
                    .addOnFailureListener { exception ->
                        Log.e(
                            "Firebase",
                            "Failed to save last recorded location: ${exception.message}"
                        )
                    }
            } else {
                Log.w("Location", "Location is null. Last recorded location not saved.")
            }
        }
    }

    private fun getAndLogAddressFromLatLng(
        lat: Any,
        lng: Double,
        onSuccess: (NominatimResponse?) -> Unit,
    ) {
        val client = OkHttpClient()
        val url = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lng&format=json"

        val request = Request.Builder()
            .url(url)
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

                        // Now you can easily access the parsed data
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
                        ) // Logs the entire object
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
}
