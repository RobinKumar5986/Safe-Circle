package com.kgjr.safecircle.ui.layouts// Required Imports
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.tooling.preview.Preview
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SliderDefaults
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaceScreen(
    onBackPress: () -> Unit = {}
) {
    var placeName by remember { mutableStateOf("Home") }
    var radius by remember { mutableStateOf(300f) }
    val coroutineScope = rememberCoroutineScope()

    // Default location
    val defaultLatLng = LatLng(13.0368, 77.5970) // Bangalore
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 16f)
    }

    val markerPosition = cameraPositionState.position.target

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add ${placeName.ifBlank { "Place" }}") },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.Filled.Close, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        println("Place Name: $placeName")
                        println("Lat: ${markerPosition.latitude}, Lng: ${markerPosition.longitude}")
                        println("Radius: ${radius.toInt()} meters")
                    }) {
                        Text("Save", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                OutlinedTextField(
                    value = placeName,
                    onValueChange = { placeName = it },
                    leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = null) },
                    placeholder = { Text("Place name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = true),
                        uiSettings = MapUiSettings(zoomControlsEnabled = false)
                    )

                    // Pin icon in the center
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = "Pin",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(36.dp)
                    )

                    // Update camera zoom when radius changes
                    LaunchedEffect(radius) {
                        val zoom = when {
                            radius <= 300 -> 17f
                            radius <= 600 -> 16f
                            radius <= 900 -> 15f
                            else -> 14f
                        }
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition(markerPosition, zoom, 0f, 0f)
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        value = radius,
                        onValueChange = { radius = it },
                        valueRange = 100f..1000f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${radius.toInt()} m", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    )
}
