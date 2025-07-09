package com.kgjr.safecircle.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.kgjr.safecircle.service.AlarmForegroundService
import com.kgjr.safecircle.ui.utils.SharedPreferenceManager

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private lateinit var sharedPreferenceManager: SharedPreferenceManager
    }

    override fun onReceive(context: Context, intent: Intent) {
        sharedPreferenceManager = SharedPreferenceManager(context)
        print("Looper Status In Alarm Receiver: ${sharedPreferenceManager.getIsUpdateLocationApiCalledLooper()}")
        if (sharedPreferenceManager.getIsUpdateLocationApiCalled() == false) {
            Log.d("SafeCircle", "AlarmReceiver triggered")
            val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
                putExtra("ActivityType", "N.A")
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        }

    }
}