package com.kgjr.safecircle.ui.layouts.customAlerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kgjr.safecircle.theme.contactButtonBackground
import com.kgjr.safecircle.theme.contactButtonTextColor

@Composable
fun NotificationSetupTourAlert(
    onStart: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            TextButton(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(contactButtonBackground, RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = "Start",
                    color = contactButtonTextColor,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        title = {
            Text(
                text = "Stay Connected with Notifications",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Setup notifications today to make sure you never miss out on what matters most:",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                )

                // Bullet points
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Text("• Get instant updates in real-time", color = Color.Black, style = MaterialTheme.typography.bodySmall)
                    Text("• Stay in touch during emergencies", color = Color.Black, style = MaterialTheme.typography.bodySmall)
                    Text("• Never miss important alerts & reminders", color = Color.Black, style = MaterialTheme.typography.bodySmall)
                    Text("• Control preferences anytime in Settings", color = Color.Black, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        tonalElevation = 6.dp
    )
}
