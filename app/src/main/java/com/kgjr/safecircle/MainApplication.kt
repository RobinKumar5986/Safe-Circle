package com.kgjr.safecircle

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import com.google.android.gms.auth.api.identity.Identity
import com.kgjr.safecircle.ui.utils.AndroidAlarmSchedulerLooper
import com.kgjr.safecircle.ui.utils.Auth.google_sign_in.GoogleAuthUiClient
import com.kgjr.safecircle.ui.utils.SharedPreferenceManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication: Application() {
    /**
     * @Info: this class will be alive throughout the lifecycle of the application.
     * so, if you want to share something between classes and activities, you can do it from here.
     */

    val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    val sharedPreferenceManager by lazy {
        SharedPreferenceManager(applicationContext)
    }
    val scheduler by  lazy {
        AndroidAlarmSchedulerLooper(applicationContext)
    }

    companion object {
        private lateinit var instance: MainApplication
        var imageUrl: String = ""
        fun getGoogleAuthUiClient(): GoogleAuthUiClient {
            return instance.googleAuthUiClient
        }

        fun getSharedPreferenceManager(): SharedPreferenceManager {
            return instance.sharedPreferenceManager
        }

        fun getScheduler(): AndroidAlarmSchedulerLooper {
            return instance.scheduler
        }

        val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        val HOME_AD_ID = "ca-app-pub-7602909356852315/6891375589"
        val LOCATION_HISTORY_AD_ID = "ca-app-pub-7602909356852315/8472351883"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}