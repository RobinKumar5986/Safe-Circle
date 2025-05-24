package com.kgjr.safecircle

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.kgjr.safecircle.ui.navigationGraph.NavigationDestinations
import com.kgjr.safecircle.ui.utils.LocationUtils
import com.kgjr.safecircle.ui.utils.NotificationUtils
import com.kgjr.safecircle.ui.utils.PhysicalActivityUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = applicationContext
        val destination = if (MainApplication.getGoogleAuthUiClient().getSignedInUser() == null) {
            NavigationDestinations.loginScreenMain
        }
        else if (!LocationUtils.isLocationPermissionGranted(context) || !NotificationUtils.isNotificationPermissionGranted(context) || !PhysicalActivityUtils.isActivityPermissionGranted(context)){
            NavigationDestinations.allPermissionMain
        }
        else {
            NavigationDestinations.homeScreenMain
        }
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("destination", destination)
        startActivity(intent)
        finish()
    }
}
