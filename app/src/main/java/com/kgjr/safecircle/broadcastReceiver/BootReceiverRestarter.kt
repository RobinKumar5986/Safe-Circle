package com.kgjr.safecircle.broadcastReceiver

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.ActivityRecognition

class BootReceiverRestarter : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.ACTIVITY_RECOGNITION)
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed - re-registering activity updates")
            val activityRecognitionClient = ActivityRecognition.getClient(context)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, ActivityTransitionReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            activityRecognitionClient.requestActivityUpdates(3000, pendingIntent)
                .addOnSuccessListener {
                    Log.d("BootReceiver", "Successfully re-registered activity updates")
                }
                .addOnFailureListener {
                    Log.e("BootReceiver", "Failed to re-register activity updates", it)
                }
        }
    }
}
