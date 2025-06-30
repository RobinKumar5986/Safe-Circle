package com.kgjr.safecircle.ui.layouts

import android.util.Log
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
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.tasks.await

@Composable
fun PushNotificationScreen(
    modifier: Modifier = Modifier,
) {
    var token by remember { mutableStateOf("") }

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
                //TODO: Send msg to some device
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Send")
        }

        Button(
            onClick = {
                Firebase.messaging.token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        Log.d("SafeCircle", token)
                    } else {
                        Log.e("SafeCircle", "Fetching FCM registration token failed", task.exception)
                    }
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Log Token")
        }
    }
}
