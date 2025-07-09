package com.kgjr.safecircle.ui.layouts

import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.kgjr.safecircle.R
import com.kgjr.safecircle.models.ArchiveLocationData
import com.kgjr.safecircle.models.StayPoint
import com.kgjr.safecircle.theme.primaryVariant
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LocationMap(
    locationHistory: List<List<ArchiveLocationData>>,
    selectedGroupIndex: Int,
    cameraPositionState: CameraPositionState,
    bottomOverlayHeightDp: Int = 370,
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    val visibleMapHeightPx = with(density) {
        (configuration.screenHeightDp.dp - bottomOverlayHeightDp.dp).roundToPx()
    }

    // Keep track of bounds of selected path for re-centering
    val initialBounds = remember { mutableStateOf<LatLngBounds?>(null) }

    // Calculate stay points (longer than 20 min, >100m apart)
    val stayPoints = remember(selectedGroupIndex) {
        val selectedPath = locationHistory.getOrNull(selectedGroupIndex) ?: emptyList()
        if (selectedPath.isEmpty()) return@remember emptyList()

        val minStayDuration = 20 * 60 * 1000 // 20 minutes in ms
        val maxDistanceMeters = 100.0

        buildList {
            var startIndex = 0
            var startTime = selectedPath[0].timeStamp ?: return@buildList

            for (i in 1 until selectedPath.size) {
                val prevLoc = selectedPath[startIndex]
                val currLoc = selectedPath[i]

                // Skip if either location lacks valid coordinates or timestamp
                if (prevLoc.latitude == null || prevLoc.longitude == null ||
                    currLoc.latitude == null || currLoc.longitude == null ||
                    currLoc.timeStamp == null) continue

                // Calculate distance between points
                val distance = FloatArray(1)
                Location.distanceBetween(
                    prevLoc.latitude, prevLoc.longitude,
                    currLoc.latitude, currLoc.longitude,
                    distance
                )

                // If distance > 100m or last point, check duration
                if (distance[0] > maxDistanceMeters || i == selectedPath.size - 1) {
                    val endTime = currLoc.timeStamp
                    val duration = endTime - startTime

                    if (duration >= minStayDuration) {
                        add(
                            StayPoint(
                                location = prevLoc,
                                startTime = startTime,
                                endTime = endTime
                            )
                        )
                    }
                    startIndex = i
                    startTime = currLoc.timeStamp
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 200.dp),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapType = MapType.TERRAIN)
        ) {
            val selectedPath = locationHistory.getOrNull(selectedGroupIndex)

            if (!selectedPath.isNullOrEmpty()) {
                // Draw polyline
                val points = selectedPath.mapNotNull { loc ->
                    val lat = loc.latitude
                    val lng = loc.longitude
                    if (lat != null && lng != null) LatLng(lat, lng) else null
                }

                if (points.isNotEmpty()) {
                    Polyline(
                        points = points,
                        color = primaryVariant,
                        width = 5f
                    )

                    // Set initial bounds
                    LaunchedEffect(selectedGroupIndex) {
                        val boundsBuilder = LatLngBounds.Builder()
                        points.forEach { boundsBuilder.include(it) }
                        val bounds = boundsBuilder.build()

                        initialBounds.value = bounds
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngBounds(
                                bounds,
                                screenWidthPx,
                                visibleMapHeightPx,
                                250
                            ),
                            durationMs = 1000
                        )
                    }
                }

                // Add markers for stay points
                stayPoints.forEach { stayPoint ->
                    val position = stayPoint.location.let { loc ->
                        LatLng(loc.latitude!!, loc.longitude!!)
                    }
                    val address = stayPoint.location.address
                    val title = if (address.isNullOrBlank() || address == "N.A") {
                        "${stayPoint.location.latitude}, ${stayPoint.location.longitude}"
                    } else {
                        address
                            .trim()
                            .split(",")
                            .take(4)
                            .joinToString(", ")
                    }

                    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val timeRange = "Stayed here form ${timeFormatter.format(Date(stayPoint.startTime))} - ${timeFormatter.format(Date(stayPoint.endTime))}"
                    Marker(
                        state = MarkerState(position = position),
                        title = title,
                        snippet = timeRange,
                        onInfoWindowClick = {
                            // Optional: Add custom action on info window click
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = initialBounds.value != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = (-400).dp)
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
                                update = CameraUpdateFactory.newLatLngBounds(bounds, 250),
                                durationMs = 1000
                            )
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