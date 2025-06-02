package com.kgjr.safecircle

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Column
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.kgjr.safecircle.broadcastReceiver.ActivityTransitionReceiver
import com.kgjr.safecircle.theme.SafeCircleTheme
import com.kgjr.safecircle.ui.navigationGraph.MainGraph
import com.kgjr.safecircle.ui.utils.NotificationService.Companion.UPDATE_LOCATION_CHANNEL_ID
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var activityRecognitionClient: ActivityRecognitionClient

    @RequiresPermission(Manifest.permission.ACTIVITY_RECOGNITION)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //initialize notification channel
        createNotificationChannel()
        val destination: String = intent.getStringExtra("destination").toString()

        //reg the broadcast receiver
        activityRecognitionClient = ActivityRecognition.getClient(this)
        val intent = Intent(this, ActivityTransitionReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        //calling it again and again to receive the broadcast
        activityRecognitionClient.requestActivityUpdates(3000, pendingIntent)
            .addOnSuccessListener {
                Log.d("MainActivity", "Activity updates requested successfully")
            }
            .addOnFailureListener {
                Log.e("MainActivity", "Failed to request activity updates", it)
            }
        setContent {
            SafeCircleTheme {
                enableEdgeToEdge()
                Column {
                    val navController = rememberNavController()
                    MainGraph(navController = navController,destination = destination)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                UPDATE_LOCATION_CHANNEL_ID,
                "Location Updates",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used for background location update notifications"
            }

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
