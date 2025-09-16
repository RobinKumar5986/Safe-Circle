package com.kgjr.safecircle.ui.layouts.customAlerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kgjr.safecircle.theme.buttonLightGray
import com.kgjr.safecircle.theme.contactButtonBackground
import com.kgjr.safecircle.theme.contactButtonTextColor

@Composable
fun CreateGroupAlert(
    groupName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, start = 12.dp, end = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = onCancel,
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
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .background(contactButtonBackground, RoundedCornerShape(12.dp))
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Confirm",
                        color = contactButtonTextColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        title = {
            Text(
                text = "Create Group",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = buildAnnotatedString {
                    append("Are you sure you want to create the group ")

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(groupName)
                    }

                    append("?")
                },
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
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
