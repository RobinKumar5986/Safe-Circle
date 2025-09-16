package com.kgjr.safecircle.ui.layouts.customAlerts

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.kgjr.safecircle.theme.buttonLightGray
import com.kgjr.safecircle.theme.contactButtonBackground
import com.kgjr.safecircle.theme.contactButtonTextColor

@Composable
fun HelpAndSupportDialog(
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    val emailIntent = remember {
        Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:rkrobin6550@gmail.com".toUri()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, start = 12.dp, end = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .background(buttonLightGray, RoundedCornerShape(12.dp))
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                TextButton(
                    onClick = {
                        context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .background(contactButtonBackground, RoundedCornerShape(12.dp))
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Contact",
                        color = contactButtonTextColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = "For help and support, contact below email",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "rkrobin6550@gmail.com",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Blue,
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        tonalElevation = 6.dp
    )
}
