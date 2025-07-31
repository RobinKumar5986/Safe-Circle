package com.kgjr.safecircle.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.ui.utils.AndroidAlarmSchedulerLooper
import com.kgjr.safecircle.ui.utils.SharedPreferenceManager

class AlarmBootReceiverForLooper : BroadcastReceiver() {
    companion object {
        private lateinit var sharedPreferenceManager: SharedPreferenceManager
    }
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("SafeCircle", "Device booted. Attempting to reschedule alarm.")
            sharedPreferenceManager = SharedPreferenceManager(context)
            val scheduler = MainApplication.getScheduler()
            scheduler.scheduleAlarm(timeInSec = 1)
            sharedPreferenceManager.saveLooperEnabled(true)
            Log.d("SafeCircle", "Alarm rescheduled after boot.")
        }
    }
}