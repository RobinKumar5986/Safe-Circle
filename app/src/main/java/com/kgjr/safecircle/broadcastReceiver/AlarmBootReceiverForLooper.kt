package com.kgjr.safecircle.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kgjr.safecircle.ui.utils.AndroidAlarmScheduler

class AlarmBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("SafeCircle", "Device booted. Attempting to reschedule alarm.")

            val scheduler = AndroidAlarmScheduler(context)
            scheduler.scheduleAlarm(timeInSec = 60)
            Log.d("SafeCircle", "Alarm rescheduled after boot.")
        }
    }
}