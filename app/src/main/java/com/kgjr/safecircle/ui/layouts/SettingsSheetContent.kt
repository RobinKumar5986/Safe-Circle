package com.kgjr.safecircle.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kgjr.safecircle.R
import com.kgjr.safecircle.models.SettingButtonType
import com.kgjr.safecircle.theme.titleBackground

@Composable
fun SettingsSheetContent(
    onItemClick: (SettingButtonType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 6.dp)
    ) {
        // Header Row with subtle transparency
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(titleBackground)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Gray
            )
        }


        SettingsRow(
            title = "Share Safe Circle",
            iconRes = R.drawable.share_icon,
            showRedDot = false,
            onClick = { onItemClick(SettingButtonType.SHARE_APP) }
        )

        Divider(color = Color.LightGray.copy(alpha = 0.4f))

        SettingsRow(
            title = "Privacy and Security",
            iconRes = R.drawable.ic_privecy,
            showRedDot = false,
            onClick = { onItemClick(SettingButtonType.PRIVACY_SECURITY) }
        )

        Divider(color = Color.LightGray.copy(alpha = 0.4f))

        SettingsRow(
            title = "Help & Feed Back",
            iconRes = R.drawable.ic_feedback,
            showRedDot = false,
            onClick = { onItemClick(SettingButtonType.HELP_AND_FEEDBACK) }
        )

        Divider(color = Color.LightGray.copy(alpha = 0.4f))

        SettingsRow(
            title = "Logout",
            iconRes = R.drawable.ic_logout,
            showRedDot = false,
            onClick = { onItemClick(SettingButtonType.LOGOUT) }
        )

    }
}
