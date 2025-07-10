package com.kgjr.safecircle.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.service.AlarmForegroundService
import com.kgjr.safecircle.ui.utils.SharedPreferenceManager

class PeriodicLocationUpdater(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    companion object {
        private lateinit var sharedPreferenceManager: SharedPreferenceManager
    }
    override suspend fun doWork(): Result {
        return try {
            sharedPreferenceManager = SharedPreferenceManager(applicationContext)
            if (sharedPreferenceManager.getIsUpdateLocationApiCalled() == false && MainApplication.getGoogleAuthUiClient().getSignedInUser()?.userId != null) {
                Log.d("PeriodicWorker", "doWork() started. Performing location and battery update.")
                val serviceIntent =
                    Intent(applicationContext, AlarmForegroundService::class.java).apply {
                        putExtra("ActivityType", "N.A")
                    }
                ContextCompat.startForegroundService(applicationContext, serviceIntent)
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("PeriodicWorker", "Error starting AlarmForegroundService", e)
            Result.failure()
        }
    }
}