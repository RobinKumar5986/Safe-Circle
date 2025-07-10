package com.kgjr.safecircle.ui.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.maps.android.compose.MapType

object LocationUtils {

    fun isLocationPermissionGranted(context: Context): Boolean {
        return isFineLocationPermissionGranted(context) || isCoarseLocationPermissionGranted(context)
    }

    fun isFineLocationPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun isCoarseLocationPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun isBackgroundLocationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun getFusedLocationClient(context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getCurrentLocation(context: Context, callback: (Location?) -> Unit) {
        if (!isLocationPermissionGranted(context)) {
            callback(null)
            return
        }
        val fusedLocationClient = getFusedLocationClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            callback(location)
        }.addOnFailureListener {
            callback(null)
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getCurrentLatitude(context: Context, callback: (Double?) -> Unit) {
        getCurrentLocation(context) { location ->
            callback(location?.latitude)
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getCurrentLongitude(context: Context, callback: (Double?) -> Unit) {
        getCurrentLocation(context) { location ->
            callback(location?.longitude)
        }
    }

    fun getMapTypeFromId(type: Int): MapType {
        return when (type) {
            1 -> MapType.NORMAL
            2 -> MapType.HYBRID
            3 -> MapType.SATELLITE
            4 -> MapType.TERRAIN
            5 -> MapType.NONE
            else -> MapType.NORMAL
        }
    }
    fun getMapTypeId(mapType: MapType): Int {
        return when (mapType) {
            MapType.NORMAL -> 1
            MapType.HYBRID -> 2
            MapType.SATELLITE -> 3
            MapType.TERRAIN -> 4
            MapType.NONE -> 5
            else -> 1
        }
    }
}