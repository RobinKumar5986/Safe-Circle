package com.kgjr.safecircle.ui.layouts

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.kgjr.safecircle.theme.contactButtonBackground

@Composable
fun UserItemForGeneralNotification(
    userId: String,
    name: String,
    imageUrl: String,
    isChecked: Boolean,
    onCheckedChange: (String, Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Small circular profile picture
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .allowHardware(false)
                .build(),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(contactButtonBackground),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

        Checkbox(
            checked = isChecked,
            onCheckedChange = { checked ->
                onCheckedChange(userId, checked)
                Log.d("UserItemForNotification", "User clicked: $userId, checked=$checked")
            }
        )
    }
}