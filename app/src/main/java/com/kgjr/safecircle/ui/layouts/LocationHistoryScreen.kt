package com.kgjr.safecircle.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.maps.android.compose.rememberCameraPositionState
import com.kgjr.safecircle.ui.viewmodels.GroupViewModel

@Composable
fun LocationHistoryScreen(userId: String) {
    val viewModel: GroupViewModel = viewModel()
    val locationHistory by viewModel.archiveLocationList.collectAsState()
    val isLoading by viewModel.isLoadingForArchive.collectAsState()
    var selectedGroupIndex by remember { mutableIntStateOf(0) }
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(userId) {
        if (locationHistory.isEmpty()) {
            viewModel.fetchLocationsFromArchive(userId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Background map
        LocationMap(
            locationHistory = locationHistory,
            selectedGroupIndex = selectedGroupIndex,
            cameraPositionState = cameraPositionState
        )

        // Fixed bottom panel (not draggable)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(390.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .align(Alignment.BottomCenter)
                .background(Color.White)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                locationHistory.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No location history available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        locationHistory.forEachIndexed { index, group ->
                            LocationGroupItem(
                                group = group,
                                isSelected = index == selectedGroupIndex,
                                onClick = { selectedGroupIndex = index }
                            )
                        }
                    }
                }
            }
        }
    }
}