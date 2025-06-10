package com.kgjr.safecircle.ui.layouts

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kgjr.safecircle.models.ArchiveLocationData
import com.kgjr.safecircle.theme.baseThemeColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("DefaultLocale")
@Composable
fun LocationGroupItem(
    group: List<ArchiveLocationData>,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Use 12-hour format with AM/PM
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()) }

    val backgroundColor = if (isSelected) baseThemeColor.copy(alpha = 0.18f) else Color.White

    // All text colors set to black unconditionally
    val textColor = Color.Black
    val subTextColor = Color.Black

    // Calculate total distance traveled for the group in meters
    val totalDistanceMeters = remember(group) {
        var dist = 0f
        for (i in 0 until group.size - 1) {
            val start = group[i]
            val end = group[i + 1]
            if (start.latitude != null && start.longitude != null && end.latitude != null && end.longitude != null) {
                val result = FloatArray(1)
                Location.distanceBetween(
                    start.latitude,
                    start.longitude,
                    end.latitude,
                    end.longitude,
                    result
                )
                dist += result[0]
            }
        }
        dist
    }

    // Format distance text for display (km if > 1000m)
    val distanceText = if (totalDistanceMeters >= 1000) {
        String.format("%.2f km", totalDistanceMeters / 1000)
    } else {
        String.format("%.0f m", totalDistanceMeters)
    }

    val batteryLevels = remember(group) {
        group.mapNotNull { it.battery }
    }

    // Calculate total duration of the group (hours, minutes, seconds)
    val durationText = remember(group) {
        val first = group.firstOrNull()?.timeStamp ?: 0L
        val last = group.lastOrNull()?.timeStamp ?: 0L
        val durationMillis = last - first
        if (durationMillis > 0) {
            val seconds = durationMillis / 1000 % 60
            val minutes = durationMillis / (1000 * 60) % 60
            val hours = durationMillis / (1000 * 60 * 60)
            buildString {
                if (hours > 0) append("${hours}h ")
                if (minutes > 0) append("${minutes}m ")
                append("${seconds}s")
            }
        } else "Unknown duration"
    }

    val firstLocation = group.firstOrNull()
    val lastLocation = group.lastOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Time range display
            val timeRange = if (firstLocation?.timeStamp != null && lastLocation?.timeStamp != null) {
                "${formatter.format(Date(firstLocation.timeStamp))} - ${formatter.format(Date(lastLocation.timeStamp))}"
            } else "Unknown Time Range"

            Text(
                text = timeRange,
                style = MaterialTheme.typography.labelLarge,
                color = textColor
            )

            Spacer(Modifier.height(8.dp))

            // Summary row with points count, distance and duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${group.size} points",
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor
                )
                Text(
                    text = "$distanceText traveled",
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor
                )
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor
                )
            }

            Spacer(Modifier.height(8.dp))

            // Show first and last coordinates if available
            if (firstLocation?.latitude != null && firstLocation.longitude != null &&
                lastLocation?.latitude != null && lastLocation.longitude != null
            ) {
                Text(
                    text = "Start: (${String.format("%.5f", firstLocation.latitude)}, ${String.format("%.5f", firstLocation.longitude)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor
                )
                Text(
                    text = "End: (${String.format("%.5f", lastLocation.latitude)}, ${String.format("%.5f", lastLocation.longitude)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Battery power consumption sparkline with title
            if (batteryLevels.isNotEmpty()) {
                Text(
                    text = "Battery Power Consumption",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = textColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                BatterySparkline(group)
            }
        }
    }
}
