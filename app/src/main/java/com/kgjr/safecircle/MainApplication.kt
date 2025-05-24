package com.kgjr.safecircle

import android.app.Application
import com.google.android.gms.auth.api.identity.Identity
import com.kgjr.safecircle.ui.utils.Auth.google_sign_in.GoogleAuthUiClient
import dagger.hilt.android.HiltAndroidApp
import com.google.gson.Gson
import androidx.core.content.edit
import com.kgjr.safecircle.ui.utils.Auth.google_sign_in.CurrentUserData

@HiltAndroidApp
class MainApplication: Application() {
    /**
     * @Info: this class will be alive through out of the life cycle of the application.
     * so, if want to share something b/w the class and other activity we can do it from here.
     */

    val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }
    companion object {
        private lateinit var instance: MainApplication
        private const val PREF_NAME = "app_prefs"
        private const val KEY_IS_LOGIN = "isLogin"
        private const val KEY_USER_DATA = "userData"

        fun setLoginState(value: Boolean) {
            val prefs = instance.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            prefs.edit { putBoolean(KEY_IS_LOGIN, value) }
        }

        fun isLogin(): Boolean {
            val prefs = instance.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            return prefs.getBoolean(KEY_IS_LOGIN, false)
        }

        fun getUserData(): CurrentUserData? {
            val prefs = instance.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            val json = prefs.getString(KEY_USER_DATA, null)
            return json?.let {
                Gson().fromJson(it, CurrentUserData::class.java)
            }
        }

        fun saveUserData(userData: CurrentUserData?) {
            val prefs = instance.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            val json = Gson().toJson(userData)
            prefs.edit { putString(KEY_USER_DATA, json) }
        }
        fun clearUserData() {
            val prefs = instance.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            prefs.edit { remove(KEY_USER_DATA) }
        }

        fun getGoogleAuthUiClient(): GoogleAuthUiClient {
            return instance.googleAuthUiClient
        }
    }

    @Override
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}