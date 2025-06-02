package com.kgjr.safecircle.ui.layouts

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@Composable
fun AllPermissionScreen(
    nav: () -> Unit
){
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val locationPermissionGranted = remember { mutableStateOf(LocationUtils.isLocationPermissionGranted(context)) }
    val notificationPermissionGranted = remember { mutableStateOf(NotificationUtils.isNotificationPermissionGranted(context)) }
    val activityPermissionGranted = remember { mutableStateOf(PhysicalActivityUtils.isActivityPermissionGranted(context))
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermissionGranted.value = isGranted
    }
    val activityPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        activityPermissionGranted.value = isGranted
    }
    LaunchedEffect(
        locationPermissionGranted.value,
        notificationPermissionGranted.value,
        activityPermissionGranted.value
    ) {
        if (locationPermissionGranted.value &&
            notificationPermissionGranted.value &&
            activityPermissionGranted.value
        ) {
            nav()
        }
    }
    // @Note: this is for on resume code (Life cycle controller)
    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                locationPermissionGranted.value = LocationUtils.isLocationPermissionGranted(context)
                notificationPermissionGranted.value = NotificationUtils.isNotificationPermissionGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val permissionItems = listOf( PermissionItemData(
            icon = Icons.Default.LocationOn,
            title = "Location Permission",
            description = "Required to access your location even in background",
            isGranted = locationPermissionGranted.value,
            onClick = {
                LocationUtils.openAppSettings(context)
            }
        ),
        PermissionItemData(
            icon = Icons.Default.Notifications,
            title = "Notification Permission",
            description = "Required to send local notifications",
            isGranted = notificationPermissionGranted.value,
            onClick = {
                if (NotificationUtils.isNotificationPermissionRequired()) {
                    notificationPermissionLauncher.launch(NotificationUtils.getNotificationPermission())
                }
            }
        ),
        PermissionItemData(
            painter = R.drawable.walk,
            title = "Physical Activity Permission",
            description = "Required to detect physical activity like walking or running",
            isGranted = activityPermissionGranted.value,
            onClick = {
                if (PhysicalActivityUtils.isActivityPermissionRequired()) {
                    activityPermissionLauncher.launch(PhysicalActivityUtils.getActivityPermission())
                }
            }
        )

    )

    Box(
        modifier = Modifier.fillMaxSize()
    ){
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
            }
        }

    }
}