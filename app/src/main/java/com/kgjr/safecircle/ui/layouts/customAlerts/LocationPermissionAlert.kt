package com.kgjr.safecircle.ui.layouts.customAlerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kgjr.safecircle.theme.contactButtonBackground
import com.kgjr.safecircle.theme.contactButtonTextColor

@Composable
fun LocationPermissionAlert(
    onGoToSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onGoToSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(contactButtonBackground, RoundedCornerShape(12.dp))
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Go to settings",
                    color = contactButtonTextColor,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Warning Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFFFF5252), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "!", 
                        color = Color.White, 
                        fontWeight = FontWeight.Black, 
                        fontSize = 32.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "SafeCircle only works correctly if it can access your location \"all the time\"",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Go to settings and follow these steps",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    ),
                    textAlign = TextAlign.Center
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("1. In SafeCircle app info, tap Permissions", color = Color.Black, textAlign = TextAlign.Center)
                    Text("2. Tap into Location permission", color = Color.Black, textAlign = TextAlign.Center)
                    Text(
                        text = "3. Select \"Allow all the time\"", 
                        color = Color.Black, 
                        fontWeight = FontWeight.Bold, // Emphasis on the final step
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        tonalElevation = 6.dp
    )
}