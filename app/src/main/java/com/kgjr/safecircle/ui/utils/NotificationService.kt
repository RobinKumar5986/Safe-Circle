package com.kgjr.safecircle.ui.utils

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kgjr.safecircle.LauncherActivity
import com.kgjr.safecircle.R

class NotificationService(private val context: Context) {

    companion object {
        const val UPDATE_LOCATION_CHANNEL_ID = "UPDATE_LOCATION_CHANNEL"
        const val UPDATE_LOCATION_NOTIFICATION_ID = 101
        const val WORKER_NOTIFICATION_ID = 105
    }

    val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(context)
    }

    fun getUpdateLocationNotification(message: String): Notification {
        val intent = Intent(context, LauncherActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(context, UPDATE_LOCATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.main_app_logo_empty_background)
            .setContentTitle(message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setAutoCancel(true)
            .build()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showWorkerNotification(message: String,bodyText: String? = null) {
        val intent = Intent(context, LauncherActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, UPDATE_LOCATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.main_app_logo_empty_background)
            .setContentTitle(message)
            .setContentText(bodyText)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(WORKER_NOTIFICATION_ID, notification)
    }

    fun cancelUpdateLocationNotification() {
        NotificationManagerCompat.from(context).cancel(UPDATE_LOCATION_NOTIFICATION_ID)
    }

    fun cancelWorkerNotification() {
        NotificationManagerCompat.from(context).cancel(WORKER_NOTIFICATION_ID)
    }
}