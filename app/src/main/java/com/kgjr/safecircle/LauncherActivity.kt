package com.kgjr.safecircle

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.kgjr.safecircle.ui.navigationGraph.NavigationDestinations
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val destination = if (!MainApplication.isLogin()) {
            NavigationDestinations.loginScreenMain
        } else {
            NavigationDestinations.homeScreenMain
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("destination", destination)
        startActivity(intent)
        finish()
    }
}
