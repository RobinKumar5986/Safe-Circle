package com.kgjr.safecircle.ui.layouts

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kgjr.safecircle.R
import com.kgjr.safecircle.ui.utils.LocationUtils
import com.kgjr.safecircle.ui.utils.NotificationUtils
import com.kgjr.safecircle.ui.utils.PermissionItemData
import com.kgjr.safecircle.ui.utils.PhysicalActivityUtils
import com.kgjr.safecircle.ui.utils.isIgnoringBatteryOptimizations
import com.kgjr.safecircle.ui.utils.requestIgnoreBatteryOptimizations

@Composable
fun AllPermissionScreen(
    nav: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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
                isForegroundLocationGranted = LocationUtils.isLocationPermissionGranted(context)
                isBackgroundLocationGranted = LocationUtils.isBackgroundLocationPermissionGranted(context)
                notificationPermissionGranted = NotificationUtils.isNotificationPermissionGranted(context)
                activityPermissionGranted = PhysicalActivityUtils.isActivityPermissionGranted(context)
                isBatteryOptimizationIgnored = isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionItems = listOf(
        PermissionItemData(
            icon = Icons.Default.LocationOn,
            title = "Location Permission",
            description = "Required to access the location so, that you and your love once can be safe.",
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
            description = "Required notification permission so, that we can let you know about your circle.",
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
            description = "This permission allows us to track your physical activity and provide better insights into your and your group’s overall health and safety.",
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
            description = "Required to run the app smoothly without any issue. So, that in case of emergency we can help.",
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
                    "To ensure your location is accurately updated even when the app is closed " +
                            "for features like family safety and alerts, " +
                            "please grant 'Allow all the time' location permission. " +
                            "Tap 'Open Settings', then go to 'Permissions', 'Location', and select 'Allow all the time'."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showBackgroundLocationRationaleDialog = false
                    LocationUtils.openAppSettings(context)
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackgroundLocationRationaleDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Spacer(modifier = Modifier.height(24.dp))
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