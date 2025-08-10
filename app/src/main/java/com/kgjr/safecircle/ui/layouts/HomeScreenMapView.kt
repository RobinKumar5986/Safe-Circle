package com.kgjr.safecircle.ui.layouts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.kgjr.safecircle.R
import com.kgjr.safecircle.theme.primaryVariant
import com.kgjr.safecircle.ui.viewmodels.GroupViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreenMapView(selectedMapType: MapType, viewModel: GroupViewModel) {
    val groupWithLocation by viewModel.groupWithLocation.collectAsState()

    val cameraPositionState = rememberCameraPositionState()
    var shouldShowReCenterButton by remember { mutableStateOf(false) }
    var isMarkerClicked by remember { mutableStateOf(false) }

    val initialBounds = remember { mutableStateOf<LatLngBounds?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val mapStyleOptions = remember {
        if (selectedMapType == MapType.NORMAL) {
            try {
                val jsonString = context.resources.openRawResource(R.raw.uber_style_map1)
                    .bufferedReader().use { it.readText() }
                MapStyleOptions(jsonString)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    LaunchedEffect(groupWithLocation) {
        if (groupWithLocation.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.Builder()
            groupWithLocation.forEach { groupData ->
                val lat = groupData.locationData.latitude
                val lng = groupData.locationData.longitude
                if (lat != null && lng != null) {
                    boundsBuilder.include(LatLng(lat, lng))
                }
            }
            val bounds = boundsBuilder.build()
            initialBounds.value = bounds

            val center = bounds.center
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLng(center),
                durationMs = 1000
            )
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(bounds, 300),
                durationMs = 1000
            )
            shouldShowReCenterButton = false
        }
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving && initialBounds.value != null && groupWithLocation.isNotEmpty()) {
            val currentCameraPosition = cameraPositionState.position.target
            val relaxedBounds = initialBounds.value?.let {
                LatLngBounds.Builder()
                    .include(LatLng(it.southwest.latitude - 0.05, it.southwest.longitude - 0.05))
                    .include(LatLng(it.northeast.latitude + 0.05, it.northeast.longitude + 0.05))
                    .build()
            }
            shouldShowReCenterButton = relaxedBounds?.contains(currentCameraPosition)?.not() == true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.67f),
            uiSettings = MapUiSettings(zoomControlsEnabled = false),
            properties = MapProperties(
                mapType = selectedMapType,
                mapStyleOptions = mapStyleOptions
                ),
            cameraPositionState = cameraPositionState
        ) {
            groupWithLocation.forEach { groupData ->
                val lat = groupData.locationData.latitude
                val lng = groupData.locationData.longitude
                val imageUrl = groupData.userData.profileImageUrl
                var activityIcon: Int? = null

                if (lat == null || lng == null || imageUrl.isNullOrEmpty()) return@forEach

                val markerState = remember(groupData.id, lat, lng) {
                    MarkerState(position = LatLng(lat, lng))
                }
                if (groupData.locationData.activity.equals("ON_FOOT")) {
                    activityIcon = R.drawable.footprints
                } else if (groupData.locationData.activity.equals("IN_VEHICLE")) {
                    activityIcon = R.drawable.car
                }

                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .size(Size.ORIGINAL)
                        .allowHardware(false)
                        .build()
                )

                val showMarkerImage = painter.state is AsyncImagePainter.State.Success

                MarkerComposable(
                    state = markerState,
                    title = groupData.userData.name ?: "Unknown",
                    snippet = groupData.locationData.address ?: "No address",
                    keys = arrayOf(
                        groupData.id,
                        lat,
                        lng,
                        imageUrl,
                        showMarkerImage,
                        groupData.locationData.timeStamp?.toString() ?: "",
                        groupData.locationData.battery?.toString() ?: ""
                    ),
                    onClick = {
                        coroutineScope.launch {
                            val currentZoom = cameraPositionState.position.zoom
                            if (currentZoom < 18f) {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(markerState.position, 18f),
                                    durationMs = 1000
                                )
                            } else {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLng(markerState.position),
                                    durationMs = 1000
                                )
                            }
                        }
                        isMarkerClicked = true
                        false
                    }
                ) {
                    if (showMarkerImage) {
                        CustomMapMarker(
                            painter = painter,
                            iconResId = activityIcon
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        )
                    }
                }
            }

            // Adjust camera to fit all markers with extra top padding
            LaunchedEffect(groupWithLocation) {
                if (groupWithLocation.isNotEmpty()) {
                    val boundsBuilder = LatLngBounds.Builder()
                    groupWithLocation.forEach { groupData ->
                        val lat = groupData.locationData.latitude
                        val lng = groupData.locationData.longitude
                        if (lat != null && lng != null) {
                            boundsBuilder.include(LatLng(lat, lng))
                        }
                    }
                    val bounds = boundsBuilder.build()
                    initialBounds.value = bounds
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngBounds(
                            bounds,
                           300
                        ),
                        durationMs = 1000
                    )
                }
            }
        }

        // Re-center button positioned within the map area (unchanged)
        AnimatedVisibility(
            visible = shouldShowReCenterButton || isMarkerClicked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = (-420).dp)
                .padding(end = 16.dp, bottom = 8.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        initialBounds.value?.let { bounds ->
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newLatLngBounds(bounds, 300),
                                durationMs = 1000
                            )
                            isMarkerClicked = false
                            shouldShowReCenterButton = false
                        }
                    }
                },
                modifier = Modifier.size(35.dp),
                shape = CircleShape,
                containerColor = Color.White,
                contentColor = Color.DarkGray
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_my_location),
                    contentDescription = "Re-center Map",
                    tint = primaryVariant
                )
            }
        }
    }
}