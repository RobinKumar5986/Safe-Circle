package com.kgjr.safecircle.ui.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kgjr.safecircle.LauncherActivity
import com.kgjr.safecircle.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "UPDATE_LOCATION_CHANNEL"
        private const val NOTIFICATION_ID = 101
        private const val SOS_CHANNEL_ID = "SOS_ALERT_CHANNEL"
        private const val SOS_NOTIFICATION_ID = 102
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Message received: ${remoteMessage.data}")

        val type = remoteMessage.data["type"]
        val message = remoteMessage.data["message"] ?: "New Notification"
        val imageUrl = remoteMessage.data["imageUrl"]
        val lat = remoteMessage.data["lat"]
        val lng = remoteMessage.data["lng"]
        val profileImage = remoteMessage.data["profileImageUrl"]

        CoroutineScope(Dispatchers.IO).launch {
            val bitmap: Bitmap? = imageUrl?.let { loadBitmapFromUrl(it) }
            val sosBitmap: Bitmap? = profileImage?.let { loadBitmapFromUrl(it) }

            if (type == "SOS" && lat != null && lng != null) {
                showSosNotification(message, sosBitmap, lat, lng)
            } else {
                showNotification(message, bitmap)
            }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(title: String, largeIcon: Bitmap? = null) {
        createNotificationChannelIfNeeded(CHANNEL_ID, "General Notifications")

        val intent = Intent(this, LauncherActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_new_logo)
            .setContentTitle(title)
            .setLargeIcon(largeIcon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build())
    }

    // 🚨 SOS notification with custom sound
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showSosNotification(message: String, largeIcon: Bitmap?, lat: String, lng: String) {
        createSosChannelIfNeeded()

        val mapIntent = Intent(Intent.ACTION_VIEW).apply {
            data = "geo:$lat,$lng?q=$lat,$lng(SOS Location)".toUri()
        }

        val mapPendingIntent = PendingIntent.getActivity(
            this,
            1,
            mapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val soundUri = "android.resource://${packageName}/${R.raw.sos_notification_sound}".toUri()

        val builder = NotificationCompat.Builder(this, SOS_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_new_logo)
            .setContentTitle("🚨 SOS ALERT!")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setLargeIcon(largeIcon)
            .setSound(soundUri)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE or NotificationCompat.DEFAULT_LIGHTS)
            .addAction(R.drawable.direction, "View on Map", mapPendingIntent)
            .setAutoCancel(true)
//            .setColor(resources.getColor(R.color.red_notification_bg))
//            .setColorized(true)
            .setContentIntent(mapPendingIntent)

        NotificationManagerCompat.from(this).notify(SOS_NOTIFICATION_ID, builder.build())
    }

    private fun loadBitmapFromUrl(urlString: String): Bitmap? {
        return try {
            val url = URL(urlString)
            BitmapFactory.decodeStream(url.openConnection().getInputStream())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createNotificationChannelIfNeeded(id: String, name: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(id) == null) {
            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createSosChannelIfNeeded() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val soundUri = "android.resource://${packageName}/${R.raw.sos_notification_sound}".toUri()
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel = NotificationChannel(
            SOS_CHANNEL_ID,
            "SOS Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(soundUri, attributes)
            enableVibration(true)
            enableLights(true)
            description = "Channel for SOS emergency alerts"
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }

        manager.createNotificationChannel(channel)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token: $token")
    }
}
