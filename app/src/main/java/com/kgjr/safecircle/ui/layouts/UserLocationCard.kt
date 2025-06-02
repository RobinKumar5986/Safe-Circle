package com.kgjr.safecircle.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kgjr.safecircle.R

@Composable
fun UserStatus(
    imageUrl: String,
    name: String,
    location: String,
    timestamp: String,
    batteryPercentage: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
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
                    tint = Color.Green,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "$batteryPercentage%",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = Color.Black
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
                text = location,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black
            )
            Text(
                text = timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}