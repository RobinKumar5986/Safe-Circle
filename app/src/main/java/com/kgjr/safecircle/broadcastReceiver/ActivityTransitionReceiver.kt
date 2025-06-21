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
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.ui.utils.BackgroundApiManagerUtil
import com.kgjr.safecircle.ui.utils.LocationUtils
import com.kgjr.safecircle.ui.utils.NotificationService
import com.kgjr.safecircle.ui.utils.NotificationService.Companion.UPDATE_LOCATION_NOTIFICATION_ID
import com.kgjr.safecircle.ui.utils.SharedPreferenceManager
import com.kgjr.safecircle.ui.utils.getBatteryPercentage
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
        LocationUtils.getCurrentLocation(context) { currentLocation ->

            if (currentLocation == null) {
                Log.e("PeriodicWorker", "Current location is null. Cannot update.")
//                notificationService.showWorkerNotification("Location unavailable", "Make sure your location is on for the smooth functioning of the service")
                return@getCurrentLocation
            }
            val message = if (activityType == "IN_VEHICLE") {
                "Activating Drive Mode.."
            } else {
                "Updating Location..."
            }
            val userId = MainApplication.getGoogleAuthUiClient().getSignedInUser()?.userId
            val batterPercentage = getBatteryPercentage(context)
            val lastLocation = sharedPreferenceManager.getLastLocation()
            val lastActivityTime = sharedPreferenceManager.getLastActivityTimestamp()

            var shouldUpdate = false
            var shouldCallAddressApi = false
            if (lastLocation != null) {
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

            val currentTime = System.currentTimeMillis()
            val lastRecordedTime  = sharedPreferenceManager.getLastActivityTimestamp()
            if(sharedPreferenceManager.getLastActivityStatus() == "IN_VEHICLE" && activityType == "IN_VEHICLE" && (currentTime - lastRecordedTime) < 10_000){
                shouldUpdate = false
            }
            if (shouldUpdate ) {

                val API_CALL_THRESHOLD_MILLIS = 1 * 60 * 1000L
                val lastTimeApiCalled = sharedPreferenceManager.getLastTimeForAddressApi()
                val lastApiLatLng = sharedPreferenceManager.getLastLocationLatLngApi()
                val lastAddressFromApi = sharedPreferenceManager.getActualAddressForApi()

                if(lastAddressFromApi == null || lastTimeApiCalled == 0L || lastApiLatLng == null ){
                    shouldCallAddressApi = true
                }else{
                    val distance = lastApiLatLng.distanceTo(currentLocation)
                    val timeDifference = currentTime - lastTimeApiCalled
                    if(distance > 100 && timeDifference > API_CALL_THRESHOLD_MILLIS){ //Note: this is the distance in meters
                        shouldCallAddressApi = true
                    }
                }
                val notification = notificationService.getUpdateLocationNotification(message)
                NotificationManagerCompat.from(context)
                    .notify(UPDATE_LOCATION_NOTIFICATION_ID, notification)
                if(shouldCallAddressApi){
                    BackgroundApiManagerUtil.getAndLogAddressFromLatLngNormApi(
                        lat = currentLocation.latitude ,
                        lng = currentLocation.longitude ) { addressData ->

                        if(addressData != null){
//                            val longestAltAddress = addressData.alt?.loc
//                                ?.mapNotNull { it.staddress }
//                                ?.maxByOrNull { it.length }
//
//                            val mainAddress = longestAltAddress
//                                ?: addressData.standard?.staddress
//                                ?: addressData.standard?.addressT
//
//                            val city = addressData.city?.takeIf { it.isNotBlank() }
//                            val country = addressData.country?.takeIf { it.isNotBlank() }
//
//                            // Collect options and choose the one with max length
//                            val options = listOfNotNull(mainAddress, city, country)
//                            val address = options.maxByOrNull { it.length } ?: ""
                            val address = addressData.displayName
                            Log.d("XYZSimpleAddress", "Resolved Longest Address: $address")

                            sharedPreferenceManager.saveLastTimeForAddressApi(currentTime)
                            sharedPreferenceManager.saveLocationActualAddressForApi(address)
                            sharedPreferenceManager.saveLastLocationLatLngApi(lat = currentLocation.latitude , lng = currentLocation.longitude)
                            BackgroundApiManagerUtil.archiveLocationData(
                                context = context,
                                userId = userId!!,
                                activityType = activityType,
                                address = address,
                                batteryPercentage = batterPercentage,
                                sharedPreferenceManager = sharedPreferenceManager,
                                notificationService = notificationService
                            )
                            BackgroundApiManagerUtil.saveLastRecordedLocation(
                                context = context,
                                userId = userId,
                                activityType = activityType,
                                address = address,
                                batteryPercentage = batterPercentage,
                                sharedPreferenceManager = sharedPreferenceManager
                            )
                            sharedPreferenceManager.saveLastActivityTimestamp(System.currentTimeMillis())
                        }else{
                            BackgroundApiManagerUtil.archiveLocationData(
                                context = context,
                                userId = userId!!,
                                activityType = activityType,
                                address = lastAddressFromApi ?: "N.A",
                                batteryPercentage = batterPercentage,
                                sharedPreferenceManager = sharedPreferenceManager,
                                notificationService = notificationService
                            )
                            BackgroundApiManagerUtil.saveLastRecordedLocation(
                                context = context,
                                userId = userId,
                                activityType = activityType,
                                address = lastAddressFromApi ?: "N.A",
                                batteryPercentage = batterPercentage,
                                sharedPreferenceManager = sharedPreferenceManager
                            )
                            sharedPreferenceManager.saveLastActivityTimestamp(System.currentTimeMillis())
                        }
                    }
                }else {
                    BackgroundApiManagerUtil.archiveLocationData(
                        context = context,
                        userId = userId!!,
                        activityType = activityType,
                        address = lastAddressFromApi ?: "N.A",
                        batteryPercentage = batterPercentage,
                        sharedPreferenceManager = sharedPreferenceManager,
                        notificationService = notificationService
                    )
                    BackgroundApiManagerUtil.saveLastRecordedLocation(
                        context = context,
                        userId = userId,
                        activityType = activityType,
                        address = lastAddressFromApi ?: "N.A",
                        batteryPercentage = batterPercentage,
                        sharedPreferenceManager = sharedPreferenceManager
                    )
                    sharedPreferenceManager.saveLastActivityTimestamp(System.currentTimeMillis())
                }
            }
        }
    }

}
