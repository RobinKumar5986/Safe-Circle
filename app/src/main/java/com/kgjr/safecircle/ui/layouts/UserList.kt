package com.kgjr.safecircle.ui.layouts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kgjr.safecircle.ui.viewmodels.GroupViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UserList(viewModel: GroupViewModel) {
    val userDataWithLocation by viewModel.groupWithLocation.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(userDataWithLocation) { data ->

            val name = data.userData.name ?: "Unknown"
            val imageUrl = data.userData.profileImageUrl ?: ""
            val location = data.locationData.address ?: "Location not available"
            val timestamp = data.locationData.timeStamp?.let {
                val date = Date(it)
                val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                "Since ${format.format(date)}"
            } ?: "Time unknown"
            val battery = data.locationData.battery?.toString() ?: "N/A"

            UserStatus(
                name = name,
                imageUrl = imageUrl,
                location = location,
                timestamp = timestamp,
                batteryPercentage = battery
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                thickness = 0.5.dp,
                color = Color.Gray.copy(alpha = 0.5f)
            )
        }
    }
}