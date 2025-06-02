@file:Suppress("TYPE_INTERSECTION_AS_REIFIED_WARNING")

package com.kgjr.safecircle.ui.layouts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.kgjr.safecircle.R
import com.kgjr.safecircle.theme.orangeColor
import com.kgjr.safecircle.ui.viewmodels.GroupViewModel

@Composable
fun MapWithCustomMarker(selectedMapType: MapType, viewModel: GroupViewModel) {
    val groupWithLocation by viewModel.groupWithLocation.collectAsState()

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        uiSettings = MapUiSettings(zoomControlsEnabled = false),
        properties = MapProperties(mapType = selectedMapType)
    ) {
        groupWithLocation.forEach { groupData ->
            val lat = groupData.locationData.latitude
            val lng = groupData.locationData.longitude
            val imageUrl = groupData.userData.profileImageUrl

            if (lat == null || lng == null || imageUrl.isNullOrEmpty()) return@forEach

            // Use remember with keys to ensure marker state updates
            val markerState = remember(groupData.id, lat, lng) {
                MarkerState(position = LatLng(lat, lng))
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
                        iconResId = R.drawable.footprints
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

@Composable
fun CustomMapMarker(
    painter: Painter,
    modifier: Modifier = Modifier,
    backgroundColor: Color = orangeColor,
    iconResId: Int? = null
) {
    Column(
        modifier = modifier.wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .padding(top = 18.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(27.dp))
                    .background(backgroundColor)
                    .border(4.dp, Color.White, RoundedCornerShape(27.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painter,
                    contentDescription = "Marker Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(27.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            iconResId?.let { resId ->
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "Overlay Icon",
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color.Gray, CircleShape)
                        .padding(4.dp)
                )
            }
        }

        Canvas(modifier = Modifier.size(8.dp)) {
            val path = Path().apply {
                moveTo(size.width / 2, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(path, color = Color.White)
            drawPath(path, color = Color.White, style = Stroke(width = 4f))
        }
    }
}
