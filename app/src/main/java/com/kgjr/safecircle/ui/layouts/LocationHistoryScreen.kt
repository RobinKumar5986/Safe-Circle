package com.kgjr.safecircle.ui.layouts

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.maps.android.compose.rememberCameraPositionState
import com.kgjr.safecircle.ui.viewmodels.GroupViewModel
import kotlin.collections.mutableListOf

@Composable
fun LocationHistoryScreen(userId: String) {
    val viewModel: GroupViewModel = viewModel()
    val locationHistory by viewModel.archiveLocationList.collectAsState()
    val isLoading by viewModel.isLoadingForArchive.collectAsState()
    var selectedGroupIndex by remember { mutableIntStateOf(0) }
    val cameraPositionState = rememberCameraPositionState()
    val context = LocalContext.current
    val updateMapStatus by viewModel.updateMap.collectAsState()
    val emptyDataForDate by viewModel.emptyDataForDate.collectAsState()

    val alreadySelectedDates = remember { mutableSetOf<Long>() }

    LaunchedEffect(userId) {
        if (locationHistory.isEmpty()) {
            viewModel.fetchLocationsFromArchive(userId)
        }
    }
    LaunchedEffect(emptyDataForDate) {
        if( emptyDataForDate) {
            Toast.makeText(context, "No Location History Found", Toast.LENGTH_SHORT).show()
            viewModel.resetEmptyDataState()
        }
    }
    LaunchedEffect(updateMapStatus) {
        if(updateMapStatus){
            selectedGroupIndex = 0
            viewModel.resetUpdateMap()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {

        // Background map
        LocationMap(
            locationHistory = locationHistory,
            selectedGroupIndex = selectedGroupIndex,
            cameraPositionState = cameraPositionState
        ){ selectedDate ->
            if(alreadySelectedDates.contains(selectedDate)) {
                Toast.makeText(context,"Data already present", Toast.LENGTH_SHORT).show()
            }else {
                viewModel.fetchSelectedDateFromArchive(userId = userId, selectedDate)
                alreadySelectedDates.add(selectedDate)
            }
        }

        // Fixed bottom panel (not draggable)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(390.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .align(Alignment.BottomCenter)
                .background(Color.White)
        ) {

            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
//                    AdBannerView(bannerId = MainApplication.LOCATION_HISTORY_AD_ID)
                }
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
}