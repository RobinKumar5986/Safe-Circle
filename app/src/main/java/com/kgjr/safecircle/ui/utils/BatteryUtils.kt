package com.kgjr.safecircle.ui.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat

fun getBatteryPercentage(context: Context): Int {
    val batteryStatus: Intent? = ContextCompat.registerReceiver(
        context,
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED),
        ContextCompat.RECEIVER_NOT_EXPORTED
    )

    batteryStatus?.let {
        val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

        if (level != -1 && scale != -1) {
            return ((level.toFloat() / scale.toFloat()) * 100).toInt()
        }
    }
    return -1
}
