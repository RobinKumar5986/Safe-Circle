package com.kgjr.safecircle.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.util.Log

class LocationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("LocationBroadcast", "Broadcast received: ${intent?.action}")
        when (intent?.action) {
            LocationManager.PROVIDERS_CHANGED_ACTION -> {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                Log.d("LocationBroadcast", "GPS: $isGpsEnabled, Network: $isNetworkEnabled")
            }
            Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                Log.d("LocationBroadcast", "Airplane mode changed")
            }
        }
    }
}
