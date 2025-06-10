package com.kgjr.safecircle.worker

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.ui.utils.FirebaseDataUpdateUtils
import com.kgjr.safecircle.ui.utils.LocationUtils
import com.kgjr.safecircle.ui.utils.NotificationService
import com.kgjr.safecircle.ui.utils.SharedPreferenceManager
import com.kgjr.safecircle.ui.utils.getBatteryPercentage
import java.util.Calendar

class PeriodicNotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val sharedPreferenceManager: SharedPreferenceManager = SharedPreferenceManager(appContext)
    private val notificationService: NotificationService = NotificationService(appContext)

    @RequiresPermission(allOf = [
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ])
    override suspend fun doWork(): Result {
        Log.d("PeriodicWorker", "doWork() started. Performing location and battery update.")

        return try {
            val userId = MainApplication.getGoogleAuthUiClient().getSignedInUser()?.userId

            if (userId == null) {
                Log.d("PeriodicWorker", "User not signed in. Skipping location update.")
                notificationService.showWorkerNotification("Worker active, but user not signed in.")
                return Result.success()
            }

            LocationUtils.getCurrentLocation(applicationContext) { currentLocation ->
                val batteryPercentage = getBatteryPercentage(applicationContext)
                val lastLocation = sharedPreferenceManager.getLastLocation()
                val lastActivityTime = sharedPreferenceManager.getLastActivityTimestamp()

                var shouldUpdate = false

                if (currentLocation == null) {
                    Log.e("PeriodicWorker", "Current location is null. Cannot update.")
                    notificationService.showWorkerNotification("Worker active, location unavailable.")
                    return@getCurrentLocation
                }

                if (lastLocation != null) {
                    val distance = lastLocation.distanceTo(currentLocation)
                    Log.d("PeriodicWorker", "Distance from last location: $distance meters")
                    shouldUpdate = distance >= 15
                } else {
                    shouldUpdate = true
                }

                val lastCal = Calendar.getInstance().apply { timeInMillis = lastActivityTime }
                val currentCal = Calendar.getInstance()
                val isNewDay = lastCal.get(Calendar.YEAR) != currentCal.get(Calendar.YEAR) ||
                        lastCal.get(Calendar.DAY_OF_YEAR) != currentCal.get(Calendar.DAY_OF_YEAR)

                if (isNewDay) {
                    shouldUpdate = true
                }

                if (shouldUpdate) {

                    val notificationMessage = "Checking for any update"
                    notificationService.showWorkerNotification(notificationMessage)
                    Log.d("PeriodicWorker", "Updating location and battery data.")
                    FirebaseDataUpdateUtils.archiveLocationData(
                        context = applicationContext,
                        userId = userId,
                        activityType = null,
                        address = "N.A",
                        batteryPercentage = batteryPercentage,
                        sharedPreferenceManager = sharedPreferenceManager,
                        notificationService = notificationService
                    )
                    FirebaseDataUpdateUtils.saveLastRecordedLocation(
                        context = applicationContext,
                        userId = userId,
                        address = "N.A",
                        activityType = null,
                        batteryPercentage = batteryPercentage,
                        sharedPreferenceManager = sharedPreferenceManager
                    )
                    sharedPreferenceManager.saveLastActivityTimestamp(System.currentTimeMillis())
                } else {
                    Log.d("PeriodicWorker", "No significant change, skipping location and battery update.")
                }
            }

            Result.success()
        } catch (e: SecurityException) {
            Log.e("PeriodicWorker", "Location permission denied: ${e.message}", e)
            notificationService.showWorkerNotification("Worker error: Location permission needed.")
            Result.failure()
        } catch (e: Exception) {
            Log.e("PeriodicWorker", "Error during doWork(): ${e.message}", e)
            notificationService.showWorkerNotification("Worker error: ${e.message}")
            e.printStackTrace()
            Result.failure()
        }
    }
}