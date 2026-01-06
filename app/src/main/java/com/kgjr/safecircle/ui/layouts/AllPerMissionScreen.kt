package com.kgjr.safecircle.ui.layouts

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kgjr.safecircle.R
import com.kgjr.safecircle.ui.utils.*

@Composable
fun AllPermissionScreen(
    nav: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // --- Optional DND access ---
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    var isDndAccessGranted by remember { mutableStateOf(notificationManager.isNotificationPolicyAccessGranted) }

    var isForegroundLocationGranted by remember { mutableStateOf(LocationUtils.isLocationPermissionGranted(context)) }
    var isBackgroundLocationGranted by remember { mutableStateOf(LocationUtils.isBackgroundLocationPermissionGranted(context)) }
    var notificationPermissionGranted by remember { mutableStateOf(NotificationUtils.isNotificationPermissionGranted(context)) }
    var activityPermissionGranted by remember { mutableStateOf(PhysicalActivityUtils.isActivityPermissionGranted(context)) }
    var isBatteryOptimizationIgnored by remember { mutableStateOf(isIgnoringBatteryOptimizations(context)) }

    var showBackgroundLocationRationaleDialog by remember { mutableStateOf(false) }

    val backgroundLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    val foregroundLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        isForegroundLocationGranted = fineLocationGranted || coarseLocationGranted

        if (isForegroundLocationGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isBackgroundLocationGranted) {
                backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                isBackgroundLocationGranted = true
            }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermissionGranted = isGranted
    }

    val activityPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        activityPermissionGranted = isGranted
    }

    LaunchedEffect(
        isForegroundLocationGranted,
        isBackgroundLocationGranted,
        notificationPermissionGranted,
        activityPermissionGranted,
        isBatteryOptimizationIgnored
    ) {
        val allLocationGranted = isForegroundLocationGranted &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || isBackgroundLocationGranted)

        if (allLocationGranted &&
            notificationPermissionGranted &&
            activityPermissionGranted &&
            isBatteryOptimizationIgnored
        ) {
            nav()
        }
    }

    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                isDndAccessGranted = nm.isNotificationPolicyAccessGranted
                isForegroundLocationGranted = LocationUtils.isLocationPermissionGranted(context)
                isBackgroundLocationGranted = LocationUtils.isBackgroundLocationPermissionGranted(context)
                notificationPermissionGranted = NotificationUtils.isNotificationPermissionGranted(context)
                activityPermissionGranted = PhysicalActivityUtils.isActivityPermissionGranted(context)
                isBatteryOptimizationIgnored = isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionItems = listOf(
//        // --- Optional DND Access ---
//        PermissionItemData(
//            icon = Icons.Default.MailOutline,
//            title = "DND Mode (optional)",
//            description = "Grant this permission to ensure you never miss important notifications when your loved ones need you.",
//            isGranted = isDndAccessGranted,
//            onClick = {
//                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
//                context.startActivity(intent)
//            }
//        ),
        PermissionItemData(
            icon = Icons.Default.LocationOn,
            title = "Location Permission",
            description = "Required to access the location so, that you and your loved ones can be safe.\nNote: we do not share any data with any third party company or app.",
            isGranted = isForegroundLocationGranted &&
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || isBackgroundLocationGranted),
            onClick = {
                val currentFineGranted = LocationUtils.isFineLocationPermissionGranted(context)
                val currentCoarseGranted = LocationUtils.isCoarseLocationPermissionGranted(context)

                if (!currentFineGranted || !currentCoarseGranted) {
                    foregroundLocationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isBackgroundLocationGranted) {
                    showBackgroundLocationRationaleDialog = true
                }
            }
        ),
        PermissionItemData(
            icon = Icons.Default.Notifications,
            title = "Notification Permission",
            description = "Required so we can alert you about your circle’s safety and SOS signals.",
            isGranted = notificationPermissionGranted,
            onClick = {
                if (NotificationUtils.isNotificationPermissionRequired()) {
                    notificationPermissionLauncher.launch(NotificationUtils.getNotificationPermission())
                }
            }
        ),
        PermissionItemData(
            painter = R.drawable.walk,
            title = "Physical Activity Permission",
            description = "Allows us to track your physical activity to provide better safety insights.",
            isGranted = activityPermissionGranted,
            onClick = {
                if (PhysicalActivityUtils.isActivityPermissionRequired()) {
                    activityPermissionLauncher.launch(PhysicalActivityUtils.getActivityPermission())
                }
            }
        ),
        PermissionItemData(
            painter = R.drawable.outline_battery_status_good_24,
            title = "Battery Optimization",
            description = "Required to run the app smoothly even in background during emergencies.",
            isGranted = isBatteryOptimizationIgnored,
            onClick = {
                requestIgnoreBatteryOptimizations(context)
            }
        )
    )

    if (showBackgroundLocationRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showBackgroundLocationRationaleDialog = false },
            title = { Text("Background Location Access Needed") },
            text = {
                Text(
                    "To ensure your location is accurately updated even when the app is closed, " +
                            "please grant 'Allow all the time' location permission in settings."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showBackgroundLocationRationaleDialog = false
                    LocationUtils.openAppSettings(context)
                }) { Text("Open Settings") }
            },
            dismissButton = {
                TextButton(onClick = { showBackgroundLocationRationaleDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Permissions Required",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Please tap the boxes below to grant the required permissions. " +
                        "These permissions help us keep you and your loved ones safe.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            permissionItems.forEach { item ->
                PermissionItemCard(
                    painter = item.painter,
                    icon = item.icon,
                    title = item.title,
                    description = item.description,
                    isGranted = item.isGranted,
                    onClick = item.onClick
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}