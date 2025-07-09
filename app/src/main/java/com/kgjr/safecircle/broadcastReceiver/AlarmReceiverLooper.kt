package com.kgjr.safecircle.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.kgjr.safecircle.service.AlarmForegroundServiceLooper
import com.kgjr.safecircle.ui.utils.SharedPreferenceManager

class AlarmReceiverLooper : BroadcastReceiver() {
    companion object {
        private lateinit var sharedPreferenceManager: SharedPreferenceManager
    }
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SafeCircle", "AlarmReceiver triggered")
        sharedPreferenceManager = SharedPreferenceManager(context)
        print("Looper Status: ${sharedPreferenceManager.getIsUpdateLocationApiCalledLooper()} ")
        if (sharedPreferenceManager.getIsUpdateLocationApiCalledLooper() == false) {
            val serviceIntent = Intent(context, AlarmForegroundServiceLooper::class.java).apply {
                putExtra("ActivityType", "N.A")
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}