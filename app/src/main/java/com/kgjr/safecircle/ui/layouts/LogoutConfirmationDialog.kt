package com.kgjr.safecircle.ui.layouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kgjr.safecircle.R
import com.kgjr.safecircle.theme.blackPurple
import com.kgjr.safecircle.theme.buttonLightGray
import com.kgjr.safecircle.theme.lightRed

@Composable
fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirmLogout: () -> Unit
) {
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
                        color = blackPurple,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                TextButton(
                    onClick = onConfirmLogout,
                    modifier = Modifier
                        .weight(1f)
                        .background(lightRed, RoundedCornerShape(12.dp))
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Logout",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.crying),
                    contentDescription = "Sad Emoji",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 12.dp)
                )
                Text(
                    text = "Are you sure?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = blackPurple
                    ),
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                text = "Do you really want to log out from the app?",
                style = MaterialTheme.typography.bodyMedium.copy(color = blackPurple),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        tonalElevation = 6.dp
    )
}
