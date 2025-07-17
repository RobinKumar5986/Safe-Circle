package com.kgjr.safecircle.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kgjr.safecircle.models.StayPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StayPointsStepper(stayPoints: List<StayPoint>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "Location History 😊",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            ),
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        stayPoints.forEachIndexed { index, stayPoint ->
            val isCompleted = index < stayPoints.lastIndex
            val isCurrent = index == stayPoints.lastIndex

            val label = when (index) {
                0 -> "Start"
                stayPoints.lastIndex -> "End"
                else -> null
            }

            val address = stayPoint.location.address?.takeIf {
                it.isNotBlank() && it != "N.A"
            }?.trim()?.split(",")?.take(4)?.joinToString(", ") ?:
            "${stayPoint.location.latitude}, ${stayPoint.location.longitude}"

            val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

            val timeRange = when (index) {
                0 -> "Start from ${timeFormatter.format(Date(stayPoint.startTime))}"
                stayPoints.lastIndex -> "End from ${timeFormatter.format(Date(stayPoint.startTime))}"
                else -> "${timeFormatter.format(Date(stayPoint.startTime))} - ${timeFormatter.format(Date(stayPoint.endTime))}"
            }


            Row(modifier = Modifier.padding(bottom = 16.dp)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = when {
                                    isCurrent -> Color.White
                                    isCompleted -> MaterialTheme.colorScheme.primary
                                    else -> Color.Gray
                                },
                                shape = CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = when {
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    isCompleted -> MaterialTheme.colorScheme.primary
                                    else -> Color.Gray
                                },
                                shape = CircleShape
                            )
                    )

                    if (index < stayPoints.lastIndex) {
                        Spacer(
                            modifier = Modifier
                                .height(40.dp)
                                .width(2.dp)
                                .drawBehind {
                                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    drawLine(
                                        color = Color.Gray.copy(alpha = 0.6f),
                                        start = Offset(x = size.width / 2, y = 0f),
                                        end = Offset(x = size.width / 2, y = size.height),
                                        strokeWidth = 4f,
                                        pathEffect = pathEffect
                                    )
                                }
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    label?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Text(
                        text = timeRange,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

