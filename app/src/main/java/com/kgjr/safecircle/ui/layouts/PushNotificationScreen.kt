package com.kgjr.safecircle.ui.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kgjr.safecircle.fcm.sendTestNotificationRequest

@Composable
fun PushNotificationScreen(
    modifier: Modifier = Modifier,
) {
    var token by remember { mutableStateOf("cPsZ1OH_QeidYC-54CSNzr:APA91bFcL-mFbzZAggRvBtmQ_rc6O-m0YoY7hKr2TSdpwr2kEwLh88d7S7TV-BBW94WMWi7Q8lCaIg85Ftdbz5Z123Sk0DjRpDe3ZYom_qSQq55r96rNFUY") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("Enter Device Token") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                sendTestNotificationRequest(
                    "https://sendtestnotification-yshvdyz2ka-uc.a.run.app",
                    token
                )
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Send")
        }

    }
}
