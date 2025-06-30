package com.kgjr.safecircle.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.kgjr.safecircle.service.AlarmForegroundService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SafeCircle", "AlarmReceiver triggered")
        val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
            putExtra("ActivityType", "N.A")
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}