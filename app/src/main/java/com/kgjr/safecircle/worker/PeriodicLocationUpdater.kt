package com.kgjr.safecircle.worker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.broadcastReceiver.ActivityTransitionReceiver
import com.kgjr.safecircle.broadcastReceiver.AlarmBootReceiverForLooper
import com.kgjr.safecircle.service.AlarmForegroundService
import com.kgjr.safecircle.ui.utils.BackgroundApiManagerUtil
import com.kgjr.safecircle.ui.utils.LocationUtils
import com.kgjr.safecircle.ui.utils.NotificationService
import com.kgjr.safecircle.ui.utils.SharedPreferenceManager
import com.kgjr.safecircle.ui.utils.getBatteryPercentage
import java.util.Calendar

class PeriodicLocationUpdater(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    companion object {
        private lateinit var sharedPreferenceManager: SharedPreferenceManager
    }
    private lateinit var notificationService: NotificationService
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override suspend fun doWork(): Result {
        return try {
            BackgroundApiManagerUtil.uploadAllPendingData()
            notificationService = NotificationService(applicationContext)
            sharedPreferenceManager = SharedPreferenceManager(applicationContext)
            if (!sharedPreferenceManager.getIsUpdateLocationApiCalled() && MainApplication.getGoogleAuthUiClient().getSignedInUser()?.userId != null) {
                Log.d("PeriodicWorker", "doWork() started. Performing location and battery update.")
//                val serviceIntent =
//                    Intent(applicationContext, AlarmForegroundService::class.java).apply {
//                        putExtra("ActivityType", "N.A")
//                    }
//                ContextCompat.startForegroundService(applicationContext, serviceIntent)
                LocationUtils.getCurrentLocation(applicationContext) { location ->
                    location?.let {
                        updateLocation(
                            context = applicationContext,
                            activityType = "N.A",
                            currentLocation = location,
                            onCompletion = {

                            }
                        )
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("PeriodicWorker", "Error starting AlarmForegroundService", e)
            Result.failure()
        }
    }
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
        var shouldUpdate: Boolean
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
        if ((currentTime - lastRecordedTime) < 10_000) { //minimum gap b.w two updates is 10 seconds
            shouldUpdate = false
        }
        if (shouldUpdate) {
            val notification = notificationService.getUpdateLocationNotification("Updates Location...")
            notificationService.notificationManager.notify(
                NotificationService.UPDATE_LOCATION_NOTIFICATION_ID,
                notification
            )
            val API_CALL_THRESHOLD_MILLIS = 1 * 60 * 1000L
            val lastTimeApiCalled = sharedPreferenceManager.getLastTimeForAddressApi()
            val lastApiLatLng = sharedPreferenceManager.getLastLocationLatLngApi()
            val lastAddressFromApi = sharedPreferenceManager.getActualAddressForApi()
            sharedPreferenceManager.saveLastActivityTimestamp(System.currentTimeMillis())
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
            }
        }
        else{
            onCompletion()
        }
    }
}