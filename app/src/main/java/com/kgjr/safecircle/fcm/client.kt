package com.kgjr.safecircle.fcm

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val client = OkHttpClient()
val JSON = "application/json; charset=utf-8".toMediaType()

fun sendTestNotificationRequest(functionUrl: String, token: String) {
    val jsonBody = """{"token":"$token"}"""
    val body = jsonBody.toRequestBody(JSON)

    val request = Request.Builder()
        .url(functionUrl)
        .post(body)
        .build()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            Log.d("SafeCircle", "Response: $responseBody")
        } catch (e: Exception) {
            Log.d("SafeCircle", e.message.toString())
            Log.e("SafeCircle", "Error sending request", e)
        }
    }
}
