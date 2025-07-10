package com.kgjr.safecircle.ui.layouts

import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.R
import com.kgjr.safecircle.models.ArchiveLocationData
import com.kgjr.safecircle.models.StayPoint
import com.kgjr.safecircle.theme.primaryVariant
import com.kgjr.safecircle.ui.utils.LocationUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

    val initialBounds = remember { mutableStateOf<LatLngBounds?>(null) }
    val profileImageUrl = MainApplication.imageUrl

    var selectedMapType by remember { mutableStateOf(MapType.NORMAL) }
    val sharedPreferenceManager = MainApplication.getSharedPreferenceManager()
    LaunchedEffect(Unit) {
        val mapTypeId = sharedPreferenceManager.getMapTypeId()
        selectedMapType = LocationUtils.getMapTypeFromId(mapTypeId)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 200.dp),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapType = selectedMapType)
        ) {
            val selectedPath = locationHistory.getOrNull(selectedGroupIndex)

            if (!selectedPath.isNullOrEmpty()) {
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

                val stayPoints = remember(selectedGroupIndex) {
                    val selectedPath = locationHistory.getOrNull(selectedGroupIndex) ?: emptyList()
                    if (selectedPath.isEmpty()) return@remember emptyList()

                    val minStayDuration = 20 * 60 * 1000
                    val maxDistanceMeters = 100.0

                    buildList {
                        var startIndex = 0
                        var startTime = selectedPath[0].timeStamp ?: return@buildList

                        for (i in 1 until selectedPath.size) {
                            val prevLoc = selectedPath[startIndex]
                            val currLoc = selectedPath[i]

                            if (prevLoc.latitude == null || prevLoc.longitude == null ||
                                currLoc.latitude == null || currLoc.longitude == null ||
                                currLoc.timeStamp == null
                            ) continue

                            val distance = FloatArray(1)
                            Location.distanceBetween(
                                prevLoc.latitude, prevLoc.longitude,
                                currLoc.latitude, currLoc.longitude,
                                distance
                            )

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

                stayPoints.forEachIndexed { index, stayPoint ->
                    val position = LatLng(stayPoint.location.latitude!!, stayPoint.location.longitude!!)
                    val address = stayPoint.location.address
                    val title = if (address.isNullOrBlank() || address == "N.A") {
                        "${stayPoint.location.latitude}, ${stayPoint.location.longitude}"
                    } else {
                        address.trim().split(",").take(4).joinToString(", ")
                    }

                    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val timeRange = "Stayed here from ${timeFormatter.format(Date(stayPoint.startTime))} - ${timeFormatter.format(Date(stayPoint.endTime))}"
                    val icon = if (index == 0) {
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    } else {
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    }

                    val markerState = remember { MarkerState(position = position) }

                    Marker(
                        state = markerState,
                        title = title,
                        snippet = timeRange,
                        icon = icon,
                        onClick = {
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(position, 18f),
                                    durationMs = 500
                                )
                            }
                            false
                        }
                    )
                }

                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profileImageUrl)
                        .size(Size.ORIGINAL)
                        .allowHardware(false)
                        .build()
                )
                val showMarkerImage = painter.state is AsyncImagePainter.State.Success

                selectedPath.lastOrNull()?.let { lastLoc ->
                    if (lastLoc.latitude != null && lastLoc.longitude != null && lastLoc.timeStamp != null) {
                        val position = LatLng(lastLoc.latitude, lastLoc.longitude)
                        val customMarkerState = remember { MarkerState(position = position) }

                        val address = lastLoc.address
                        val title = if (address.isNullOrBlank() || address == "N.A") {
                            "${lastLoc.latitude}, ${lastLoc.longitude}"
                        } else {
                            address.trim().split(",").take(4).joinToString(", ")
                        }

                        val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        val timeRange = "Ended at ${timeFormatter.format(Date(lastLoc.timeStamp))}"

                        MarkerComposable(
                            state = customMarkerState,
                            title = title,
                            snippet = timeRange,
                            keys = arrayOf(
                                selectedGroupIndex.toString(),
                                lastLoc.latitude.toString(),
                                lastLoc.longitude.toString(),
                                profileImageUrl,
                                showMarkerImage,
                                lastLoc.timeStamp.toString(),
                                lastLoc.battery?.toString() ?: ""
                            ),
                            onClick = {
                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        update = CameraUpdateFactory.newLatLngZoom(position, 18f),
                                        durationMs = 500
                                    )
                                }
                                false
                            }
                        ) {
                            if (showMarkerImage) {
                                CustomMapMarker(
                                    painter = painter,
                                    iconResId = null
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
