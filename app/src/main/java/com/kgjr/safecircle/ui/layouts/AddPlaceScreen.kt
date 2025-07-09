package com.kgjr.safecircle.ui.layouts

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.kgjr.safecircle.R
import com.kgjr.safecircle.models.SavedPlaceData
import com.kgjr.safecircle.theme.baseThemeColor
import com.kgjr.safecircle.ui.utils.LocationUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaceScreen(
    placeType: String?,
    radius: Int?,
    placeId: String?,
    onBackPress: () -> Unit,
    onSavePlace: (SavedPlaceData, Boolean, String?) -> Unit
) {
    var placeName by remember { mutableStateOf("Place") }

    var radiusInFeet by remember { mutableStateOf(300f) }

    val radiusInMeters = remember(radiusInFeet) { radiusInFeet / 3.28084 }

    var defaultLatLng by remember { mutableStateOf(LatLng(19.0760, 72.8777)) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 17.5f)
    }


    LaunchedEffect(Unit) {
        placeName = placeType ?: "Place"
        radius?.let {
            radiusInFeet = radius.toFloat()
        }
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted && coarseGranted) {
            LocationUtils.getCurrentLocation(context) { location ->
                location?.let {
                    defaultLatLng = LatLng(it.latitude, it.longitude)
                    coroutineScope.launch {
                        val zoom = when (radiusInFeet) {
                            in 0f..200f -> 18.5f
                            in 201f..400f -> 17.5f
                            in 401f..600f -> 16.5f
                            in 601f..800f -> 15.5f
                            else -> 15.5f
                        }
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(defaultLatLng,zoom)
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Surface(tonalElevation = 4.dp, shadowElevation = 4.dp) {
                TopAppBar(
                    title = {
                        Text("Add ${placeName.ifBlank { "Place" }}", color = Color.Black)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPress) {
                            Icon(Icons.Filled.Close, contentDescription = "Back", tint = Color.Black)
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            val latLng = cameraPositionState.position.target

                            if (placeName.isBlank()) {
                                Toast.makeText(context, "Please enter place name", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            
                            val radiusInFeet = radiusInFeet.toInt()
                            val savedData = SavedPlaceData(
                                placeName = placeName,
                                lat = latLng.latitude,
                                lng = latLng.longitude,
                                radiusInFeet = radiusInFeet
                            )
                            if(radius != null) {
                                onSavePlace(savedData, true,placeId)
                            }else{
                                onSavePlace(savedData, false,null)
                            }
                        }) {
                            Text("Save", color = baseThemeColor)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.White)
            ) {
                TextField(
                    value = placeName,
                    onValueChange = { placeName = it },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.bookmark),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(Color.Gray)
                        )
                    },
                    placeholder = { Text("Place name", color = Color.Gray) },
                    textStyle = TextStyle(color = Color.Gray),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Gray,
                        unfocusedIndicatorColor = Color.LightGray,
                        disabledIndicatorColor = Color.LightGray,
                        cursorColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    singleLine = true
                )

                Box(modifier = Modifier.weight(1f)) {
                    val markerPosition = cameraPositionState.position.target
                    val mapType = if (radiusInFeet < 200f) MapType.HYBRID else MapType.NORMAL

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            isMyLocationEnabled = true,
                            mapType = mapType
                        ),
                        uiSettings = MapUiSettings(zoomControlsEnabled = false)
                    ) {
                        Circle(
                            center = markerPosition,
                            radius = radiusInMeters,
                            strokeColor = baseThemeColor.copy(alpha = 0.5f),
                            fillColor = baseThemeColor.copy(alpha = 0.15f),
                            strokeWidth = 2f
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = "Pin",
                        tint = baseThemeColor,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(36.dp)
                    )

                    // Adjust zoom based on radius (feet)
                    LaunchedEffect(radiusInFeet) {
                        val marker = cameraPositionState.position.target
                        val zoom = when (radiusInFeet) {
                            in 0f..200f -> 18.5f
                            in 201f..400f -> 17.5f
                            in 401f..600f -> 16.5f
                            in 601f..800f -> 15.5f
                            else -> 15.5f
                        }
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition(marker, zoom, 0f, 0f)
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        value = radiusInFeet,
                        onValueChange = { radiusInFeet = it },
                        valueRange = 100f..1000f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = baseThemeColor,
                            activeTrackColor = baseThemeColor,
                            inactiveTrackColor = Color.LightGray
                        ),
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(baseThemeColor, CircleShape)
                            )
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${radiusInFeet.toInt()} ft", color = baseThemeColor)
                }
            }
        }
    )
}
