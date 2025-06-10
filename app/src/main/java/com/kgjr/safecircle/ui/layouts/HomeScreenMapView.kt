package com.kgjr.safecircle.ui.layouts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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

    val initialBounds = remember { mutableStateOf<LatLngBounds?>(null) }

    val coroutineScope = rememberCoroutineScope()

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
            val currentZoom = cameraPositionState.position.zoom

            val relaxedBounds = initialBounds.value?.let {
                LatLngBounds.Builder()
                    .include(LatLng(it.southwest.latitude - 0.01, it.southwest.longitude - 0.01))
                    .include(LatLng(it.northeast.latitude + 0.01, it.northeast.longitude + 0.01))
                    .build()
            }

            shouldShowReCenterButton = if (relaxedBounds != null) {
                !relaxedBounds.contains(currentCameraPosition) || currentZoom < (cameraPositionState.position.zoom - 1f)
            } else {
                false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            uiSettings = MapUiSettings(zoomControlsEnabled = false),
            properties = MapProperties(mapType = selectedMapType),
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
                if(groupData.locationData.activity.equals("ON_FOOT")){
                    activityIcon = R.drawable.footprints
                }else if(groupData.locationData.activity.equals("IN_VEHICLE")){
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
                    )
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
        }

        // Re-center button positioned above the specified Row
        AnimatedVisibility(
            visible = shouldShowReCenterButton,
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
                            val center = bounds.center
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newLatLng(center),
                                durationMs = 700
                            )
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newLatLngBounds(bounds, 300),
                                durationMs = 1000
                            )
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