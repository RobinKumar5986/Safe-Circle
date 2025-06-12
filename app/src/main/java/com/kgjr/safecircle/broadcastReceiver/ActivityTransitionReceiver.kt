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
import com.kgjr.safecircle.ui.utils.FirebaseDataUpdateUtils
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
            "Activating Drive Mode.."
        } else {
            "Updating Location..."
        }


        val userId = MainApplication.getGoogleAuthUiClient().getSignedInUser()?.userId
        val batterPercentage = getBatteryPercentage(context)
        val lastLocation = sharedPreferenceManager.getLastLocation()
        val lastActivityTime = sharedPreferenceManager.getLastActivityTimestamp()

        LocationUtils.getCurrentLocation(context) { currentLocation ->
            var shouldUpdate = false

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
            if(userId== null)
                shouldUpdate = false

            val lastRecordedTime  = sharedPreferenceManager.getLastActivityTimestamp()
            val currentTime = System.currentTimeMillis()
            if(sharedPreferenceManager.getLastActivityStatus() == "IN_VEHICLE" && activityType == "IN_VEHICLE" && (currentTime - lastRecordedTime) < 10_000){
                shouldUpdate = false
            }
            if (shouldUpdate ) {
//                getAndLogAddressFromLatLng(
//                    lat = currentLocation?.latitude ?: 0.0,
//                    lng = currentLocation?.longitude ?: 0.0
//                ) { openMapData ->
//                    if (openMapData != null) {

                val notification = notificationService.getUpdateLocationNotification(message)
                NotificationManagerCompat.from(context)
                    .notify(UPDATE_LOCATION_NOTIFICATION_ID, notification)

                FirebaseDataUpdateUtils.archiveLocationData(
                    context = context,
                    userId = userId!!,
                    activityType = activityType,
                    address = "N.A",
                    batteryPercentage = batterPercentage,
                    sharedPreferenceManager = sharedPreferenceManager,
                    notificationService = notificationService
                )
                FirebaseDataUpdateUtils.saveLastRecordedLocation(
                    context = context,
                    userId = userId,
                    activityType = activityType,
                    address = "N.A",
                    batteryPercentage = batterPercentage,
                    sharedPreferenceManager = sharedPreferenceManager
                )
                sharedPreferenceManager.saveLastActivityTimestamp(System.currentTimeMillis())
//                    }
//                }
            }

        }
    }

}
