package com.kgjr.safecircle.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

object FcmManager {

    private const val TAG = "FcmManager"

    /**
     * Register app for FCM token.
     * Fetches token and can be sent to backend for registration.
     */
    fun registerForFcmToken(onTokenReceived: (String) -> Unit = {}) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.d(TAG, "FCM Token registered: $token")

                // Callback for usage
                onTokenReceived(token)
            }
    }

    /**
     * Get the current FCM token (on demand).
     */
    fun getFcmToken(onResult: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(task.result)
                } else {
                    Log.w(TAG, "Failed to get FCM token", task.exception)
                    onResult(null)
                }
            }
    }
}