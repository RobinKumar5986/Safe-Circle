package com.kgjr.safecircle.ui.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PhysicalActivityUtils {

    fun isActivityPermissionRequired(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    fun getActivityPermission(): String {
        return Manifest.permission.ACTIVITY_RECOGNITION
    }

    fun isActivityPermissionGranted(context: Context): Boolean {
        return if (isActivityPermissionRequired()) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}