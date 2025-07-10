package com.kgjr.safecircle.broadcastReceiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.ui.utils.BackgroundApiManagerUtil
import com.kgjr.safecircle.ui.utils.LocationUtils
import com.kgjr.safecircle.ui.utils.NotificationService
import com.kgjr.safecircle.ui.utils.SharedPreferenceManager
import com.kgjr.safecircle.ui.utils.getBatteryPercentage
import java.util.Calendar

class ActivityTransitionReceiver : BroadcastReceiver() {
    companion object {
        private lateinit var sharedPreferenceManager: SharedPreferenceManager
    }
    private lateinit var notificationService: NotificationService
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS])
    override fun onReceive(context: Context, intent: Intent) {
        //initializing the managers in the starting...
        sharedPreferenceManager = SharedPreferenceManager(context)
        notificationService = NotificationService(context)
        if (MainApplication.getGoogleAuthUiClient().getSignedInUser()?.userId != null){
            if (ActivityRecognitionResult.hasResult(intent)) {
                val result = ActivityRecognitionResult.extractResult(intent)
                val activity = result!!.mostProbableActivity
                val type = getActivityType(activity.type)
                val confidence = activity.confidence
                Log.d("SafeCircle", "Detected activity: $type with confidence: $confidence")

                try {
                    LocationUtils.getCurrentLocation(context) { location ->
                        location?.let {
                            updateLocation(context, type, location) {

                            }
                        }
                    }

                } catch (e: SecurityException) {
                    e.printStackTrace()
                    Log.e("SafeCircle", "Missing location permission.")
                }
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

//    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
//    private fun updateLocation(
//        context: Context,
//        activityType: String
//    ) {
//        sharedPreferenceManager = SharedPreferenceManager(context)
//        print("Looper Status In Activity receiver: ${sharedPreferenceManager.getIsUpdateLocationApiCalledLooper()}")
//        if (sharedPreferenceManager.getIsUpdateLocationApiCalled() == false) {
//            Log.d("SafeCircle", "Activity Broadcast Receiver")
//            val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
//                putExtra("ActivityType", activityType)
//            }
//            ContextCompat.startForegroundService(context, serviceIntent)
//        }
//    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun updateLocation(
        context: Context,
        activityType: String,
        currentLocation: Location,
        onCompletion: () -> Unit
    ) {
        val userId = MainApplication.getGoogleAuthUiClient().getSignedInUser()?.userId
        val batterPercentage = getBatteryPercentage(context)
        val lastLocation = sharedPreferenceManager.getLastLocation()
        val lastActivityTime = sharedPreferenceManager.getLastActivityTimestamp()
        var shouldUpdate = false
        var shouldCallAddressApi = false
        if (lastLocation != null) {
            val distance = lastLocation.distanceTo(currentLocation)
            Log.d("SafeCircle", "Distance from last location: $distance meters")
            shouldUpdate = distance >= 15
        } else {
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
        if (userId == null)
            shouldUpdate = false

        val currentTime = System.currentTimeMillis()
        val lastRecordedTime = sharedPreferenceManager.getLastActivityTimestamp()
//        if (sharedPreferenceManager.getLastActivityStatus() == "IN_VEHICLE" && activityType == "IN_VEHICLE" && (currentTime - lastRecordedTime) < 10_000) {
//            shouldUpdate = false
//        }
        if ((currentTime - lastRecordedTime) < 10_000) {
            shouldUpdate = false
        }
        if (shouldUpdate) {
            val notification = notificationService.getUpdateLocationNotification("Checking For Updates...")
            notificationService.notificationManager.notify(
                NotificationService.UPDATE_LOCATION_NOTIFICATION_ID,
                notification
            )

            val API_CALL_THRESHOLD_MILLIS = 1 * 60 * 1000L
            val lastTimeApiCalled = sharedPreferenceManager.getLastTimeForAddressApi()
            val lastApiLatLng = sharedPreferenceManager.getLastLocationLatLngApi()
            val lastAddressFromApi = sharedPreferenceManager.getActualAddressForApi()

            if (lastAddressFromApi == null || lastTimeApiCalled == 0L || lastApiLatLng == null) {
                shouldCallAddressApi = true
            } else {
                val distance = lastApiLatLng.distanceTo(currentLocation)
                val timeDifference = currentTime - lastTimeApiCalled
                if (distance > 100 && timeDifference > API_CALL_THRESHOLD_MILLIS) { //Note: this is the distance in meters
                    shouldCallAddressApi = true
                }
            }
            var checkinAddress = "N.A"
            val placeCheckins = sharedPreferenceManager.getPlaceCheckins()
            for (place in placeCheckins) {
                val result = FloatArray(1)

                Location.distanceBetween(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    place.lat,
                    place.lng,
                    result
                )

                val distanceInFeet = result[0] * 3.28084f

                if (distanceInFeet <= place.radiusInFeet) {
                    checkinAddress = place.placeName
                    break
                }
            }
            if(checkinAddress != "N.A"){
                shouldCallAddressApi = false
            }else{
                checkinAddress = lastAddressFromApi ?: "N.A"
            }
            if (shouldCallAddressApi) {
                BackgroundApiManagerUtil.getAndLogAddressFromLatLngNormApi(
                    lat = currentLocation.latitude,
                    lng = currentLocation.longitude
                ) { addressData ->

                    if (addressData != null) {
                        val address = addressData.displayName
                        Log.d("SafeCircle", "Resolved Longest Address: $address")

                        sharedPreferenceManager.saveLastTimeForAddressApi(currentTime)
                        sharedPreferenceManager.saveLocationActualAddressForApi(address)
                        sharedPreferenceManager.saveIsUpdateLocationApiCalled(true) // @Mark: to make sure the api is been called one at a time.
                        sharedPreferenceManager.saveLastLocationLatLngApi(
                            lat = currentLocation.latitude,
                            lng = currentLocation.longitude
                        )
                        BackgroundApiManagerUtil.archiveLocationDataV2(
                            userId = userId!!,
                            activityType = activityType,
                            address = address,
                            batteryPercentage = batterPercentage,
                            sharedPreferenceManager = sharedPreferenceManager,
                            notificationService = notificationService,
                            currentLocation = currentLocation
                        ){
                            onCompletion()
                        }
                        BackgroundApiManagerUtil.saveLastRecordedLocationV2(
                            userId = userId,
                            activityType = activityType,
                            address = address,
                            batteryPercentage = batterPercentage,
                            sharedPreferenceManager = sharedPreferenceManager,
                            currentLocation = currentLocation
                        ){
//                            onCompletion()
                        }
                        sharedPreferenceManager.saveLastActivityTimestamp(System.currentTimeMillis())
                    } else {
                        BackgroundApiManagerUtil.archiveLocationDataV2(
                            userId = userId!!,
                            activityType = activityType,
                            address = checkinAddress,
                            batteryPercentage = batterPercentage,
                            sharedPreferenceManager = sharedPreferenceManager,
                            notificationService = notificationService,
                            currentLocation = currentLocation
                        ){
//                            onCompletion()
                        }
                        BackgroundApiManagerUtil.saveLastRecordedLocationV2(
                            userId = userId,
                            activityType = activityType,
                            address = checkinAddress,
                            batteryPercentage = batterPercentage,
                            sharedPreferenceManager = sharedPreferenceManager,
                            currentLocation = currentLocation
                        ){
                            onCompletion()
                        }
                        sharedPreferenceManager.saveLastActivityTimestamp(System.currentTimeMillis())
                    }
                }
            } else {
                BackgroundApiManagerUtil.archiveLocationDataV2(
                    userId = userId!!,
                    activityType = activityType,
                    address = checkinAddress,
                    batteryPercentage = batterPercentage,
                    sharedPreferenceManager = sharedPreferenceManager,
                    notificationService = notificationService,
                    currentLocation = currentLocation
                ){
//                    onCompletion()
                }
                BackgroundApiManagerUtil.saveLastRecordedLocationV2(
                    userId = userId,
                    activityType = activityType,
                    address = checkinAddress,
                    batteryPercentage = batterPercentage,
                    sharedPreferenceManager = sharedPreferenceManager,
                    currentLocation = currentLocation
                ){
                    onCompletion()
                }
                sharedPreferenceManager.saveLastActivityTimestamp(System.currentTimeMillis())
            }
        }
        else{
            onCompletion()
        }
    }

}
