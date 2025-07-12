package com.kgjr.safecircle.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.broadcastReceiver.ActivityTransitionReceiver
import com.kgjr.safecircle.ui.utils.AndroidAlarmScheduler
import com.kgjr.safecircle.ui.utils.BackgroundApiManagerUtil
import com.kgjr.safecircle.ui.utils.NotificationService
import com.kgjr.safecircle.ui.utils.NotificationService.Companion.UPDATE_LOCATION_NOTIFICATION_ID
import com.kgjr.safecircle.ui.utils.SharedPreferenceManager
import com.kgjr.safecircle.ui.utils.getBatteryPercentage
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AlarmForegroundService : Service() {

    companion object {
        private lateinit var sharedPreferenceManager: SharedPreferenceManager
    }

    private lateinit var scheduler: AndroidAlarmScheduler
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var notificationService: NotificationService
    lateinit var activityType: String

    override fun onCreate() {
        super.onCreate()
        scheduler = AndroidAlarmScheduler(applicationContext)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        notificationService = NotificationService(applicationContext)
        sharedPreferenceManager = SharedPreferenceManager(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SafeCircle", "Starting AlarmForegroundService...")
        activityType = intent?.getStringExtra("ActivityType") ?: "N.A"
        val message = if (activityType != "N.A") "Checking For Updates <Test>" else "Updating Location..."

        startForeground(
            UPDATE_LOCATION_NOTIFICATION_ID,
            notificationService.getUpdateLocationNotification(message)
        )

        // Check for location permissions
        if (hasLocationPermissions()) {
            startLocationUpdates()
        } else {
            Log.e("SafeCircle", "Location permissions missing")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun hasLocationPermissions(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val foregroundServiceLocationGranted =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.FOREGROUND_SERVICE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

        return (fineLocationGranted || coarseLocationGranted) && foregroundServiceLocationGranted
    }

    private fun startLocationUpdates() {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, TimeUnit.SECONDS.toMillis(5))
                .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5))
                .setMaxUpdateDelayMillis(TimeUnit.SECONDS.toMillis(10))
                .build()

        locationCallback = object : LocationCallback() {
            @RequiresPermission(allOf = [Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation

                if (location != null && location.accuracy <= 10.0f && System.currentTimeMillis() - location.time < TimeUnit.MINUTES.toMillis(1) && !sharedPreferenceManager.getIsUpdateLocationApiCalled()) {
                    updateLocation(
                        context = applicationContext,
                        activityType = activityType,
                        currentLocation = location
                    ) {
                        Log.d("SafeCircle", "Service stopped after location update")
                        sharedPreferenceManager.saveIsUpdateLocationApiCalled(false)
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopForeground(STOP_FOREGROUND_DETACH)
                        stopSelf()
                    }
                    stopLocationUpdates()
                } else {
                    stopLocationUpdates()
                    notificationService.cancelUpdateLocationNotification()
                    Log.e("SafeCircle", "Location is null")
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopForeground(STOP_FOREGROUND_DETACH)
                    stopSelf()
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            stopLocationUpdates()
            Log.e("SafeCircle", "SecurityException: ${e.message}")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }
    }

    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        Log.d("SafeCircle", "Service destroyed")
//        scheduler.scheduleAlarm(timeInSec = 10)
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
        sharedPreferenceManager.saveIsUpdateLocationApiCalled(true) // @Mark: to make sure the api is been called one at a time.
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
        if ((currentTime - lastRecordedTime) < 10_000) {
            shouldUpdate = false
        }
        if (shouldUpdate) {

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
                            address =  checkinAddress,
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