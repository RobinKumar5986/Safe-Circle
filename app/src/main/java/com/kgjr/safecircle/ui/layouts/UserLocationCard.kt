package com.kgjr.safecircle.ui.layouts

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kgjr.safecircle.R
import com.kgjr.safecircle.theme.SoftDarkGreen
import com.kgjr.safecircle.theme.SoftDarkRed
import com.kgjr.safecircle.theme.SoftOrange

@Composable
fun UserStatus(
    imageUrl: String,
    name: String,
    location: String,
    timestamp: String,
    batteryPercentage: String,
    lat: Double,
    lng: Double,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val batteryColor = getBatteryColor(batteryPercentage)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clickable(onClick = {
                onClick()
            }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(64.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .allowHardware(false)
                    .build(),
                contentDescription = "User avatar",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFFBBDEFB))
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .shadow(4.dp, shape = MaterialTheme.shapes.small)
                    .background(Color(0xFFF5F5F5), shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_battery_android_bolt_24),
                    contentDescription = "Battery",
                    tint = batteryColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "$batteryPercentage%",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = batteryColor
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
//                Icon(
//                    painter = painterResource(id = R.drawable.outline_signal_wifi_off_24),
//                    contentDescription = "Wifi off",
//                    tint = Color.Red.copy(alpha = 0.6f),
//                    modifier = Modifier.size(16.dp)
//                )
            }
            Text(
                text = "Lat: $lat, Lng: $lng",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black
            )
            // Clickable text for opening map
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append("Direction")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable {
                        val gmmIntentUri = "geo:$lat,$lng?q=$lat,$lng".toUri()
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            val browserIntent = Intent(
                                Intent.ACTION_VIEW,
                                "http://maps.google.com/?q=$lat,$lng".toUri()
                            )
                            context.startActivity(browserIntent)
                        }
                    }
            )
            Text(
                text = timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

// Helper function to get battery color based on percentage
@Composable
private fun getBatteryColor(batteryPercentage: String): Color {
    return when (batteryPercentage.toIntOrNull()) {
        null -> Color.Gray
        in 0..15 -> SoftDarkRed
        in 16..40 -> SoftOrange
        in 41..100 -> SoftDarkGreen
        else -> Color.Gray
    }
}