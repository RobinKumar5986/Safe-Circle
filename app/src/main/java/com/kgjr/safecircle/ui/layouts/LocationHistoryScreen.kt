package com.kgjr.safecircle.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.maps.android.compose.rememberCameraPositionState
import com.kgjr.safecircle.ui.viewmodels.GroupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationHistoryScreen(userId: String) {
    val viewModel: GroupViewModel = viewModel()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val locationHistory by viewModel.archiveLocationList.collectAsState()
    val isLoading by viewModel.isLoadingForArchive.collectAsState()
    var selectedGroupIndex by remember { mutableIntStateOf(0) }
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(userId) {
        if (locationHistory.isEmpty()) {
            viewModel.fetchLocationsFromArchive(userId)
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(370.dp)
                    .background(Color.White)
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    locationHistory.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
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
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),

                        ) {
                            itemsIndexed(locationHistory) { index, group ->
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
        },
        sheetContainerColor = Color.White,
        modifier = Modifier.fillMaxSize(),
        sheetTonalElevation = 10.dp,
        sheetPeekHeight = 370.dp
    )
    {
        LocationMap(
            locationHistory = locationHistory,
            selectedGroupIndex = selectedGroupIndex,
            cameraPositionState = cameraPositionState
        )
    }
}

