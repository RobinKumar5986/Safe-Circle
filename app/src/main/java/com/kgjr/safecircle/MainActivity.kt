package com.kgjr.safecircle

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Column
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.installStatus
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.kgjr.safecircle.theme.SafeCircleTheme
import com.kgjr.safecircle.ui.navigationGraph.MainGraph
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var appUpdateManager : AppUpdateManager
    private var updateType = AppUpdateType.IMMEDIATE
    private var appUpdateCallbackId = 1001


    @RequiresPermission(Manifest.permission.ACTIVITY_RECOGNITION)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val destination: String = intent.getStringExtra("destination").toString()
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        if(updateType == AppUpdateType.FLEXIBLE){
            appUpdateManager.registerListener(installStateUpdatedListener)
        }
        checkForAppUpdate()
        setContent {
            SafeCircleTheme {
                enableEdgeToEdge()
                Column {
                    val navController = rememberNavController()
                    MainGraph(navController = navController,destination = destination)
                }
            }
        }
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener{ state ->
        if(state.installStatus == InstallStatus.DOWNLOADED){
            Toast.makeText(this, "Download is Successful restarting the app in 3 sec", Toast.LENGTH_LONG).show()
            lifecycleScope.launch {
                delay(3.seconds)
                appUpdateManager.completeUpdate()
            }
        }else if(state.installStatus == InstallStatus.FAILED || state.installStatus == InstallStatus.UNKNOWN){
            Toast.makeText(this, "Something went wrong while downloading the app :-(", Toast.LENGTH_SHORT).show()
        }
    }
    private fun checkForAppUpdate(){
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateAllowed = when(updateType){
                AppUpdateType.FLEXIBLE -> info.isFlexibleUpdateAllowed
                AppUpdateType.IMMEDIATE -> info.isImmediateUpdateAllowed
                else -> false
            }
            if(isUpdateAvailable && isUpdateAllowed){
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateType,
                    this,
                    appUpdateCallbackId
                )
            }
        }.addOnFailureListener {
            Log.d("SafeCircle", "Not able to fetch the app update <OnCreate>")
        }
    }

    override fun onResume() {
        super.onResume()
        if(updateType == AppUpdateType.IMMEDIATE) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateType,
                        this,
                        appUpdateCallbackId
                    )
                }
            }.addOnFailureListener {
                Log.d("SafeCircle", "Not able to fetch the app update <OnResume>")
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == appUpdateCallbackId){
            if(requestCode != RESULT_OK){
                Log.d("SafeCircle", "Not able to fetch the app update <OnActivity Result>")
                return
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(updateType == AppUpdateType.FLEXIBLE) {
            appUpdateManager.unregisterListener(installStateUpdatedListener)
        }
    }
}