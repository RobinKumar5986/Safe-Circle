package com.kgjr.safecircle.ui.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.ActivityRecognition
import com.kgjr.safecircle.broadcastReceiver.ActivityTransitionReceiver
import com.kgjr.safecircle.ui.utils.NotificationService.Companion.UPDATE_LOCATION_CHANNEL_ID
import com.kgjr.safecircle.worker.PeriodicLocationUpdater
import java.util.concurrent.TimeUnit

object LocationActivityManager {

    fun initializeNotificationAndWorker(context: Context) {
        createNotificationChannel(context)
        schedulePeriodicNotificationWorker(context) // For confirmation that location will be collect in 15 min max.
    }

    @RequiresPermission(Manifest.permission.ACTIVITY_RECOGNITION)
    fun startActivityRecognition(context: Context) {
        val activityRecognitionClient = ActivityRecognition.getClient(context)
        val intent = Intent(context, ActivityTransitionReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        activityRecognitionClient.requestActivityUpdates(3000, pendingIntent) //kind of sating ask for update in every 3 sec...
            .addOnSuccessListener {
                Log.d("LocationActivityManager", "Activity updates requested successfully")
            }
            .addOnFailureListener {
                Log.e("LocationActivityManager", "Failed to request activity updates", it)
            }
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            UPDATE_LOCATION_CHANNEL_ID,
            "Location Updates",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Used for background location update notifications"
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun schedulePeriodicNotificationWorker(context: Context) {
        val periodicWorkRequest = PeriodicWorkRequestBuilder<PeriodicLocationUpdater>(
            15, TimeUnit.MINUTES
        ).addTag("PeriodicNotificationWorkerTag").build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "PeriodicNotificationWorkerTag",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }
    fun cancelPeriodicNotificationWorker(context: Context) {
        Log.d("WorkManager", "PeriodicNotificationWorker has been cancelled.")
        WorkManager.getInstance(context)
            .cancelUniqueWork("PeriodicNotificationWorkerTag")
    }
}
