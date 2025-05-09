package com.kgjr.safecircle

import android.app.Application
import android.content.SharedPreferences
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication: Application() {
    /**
     * @Info: this class will be alive through out of the life cycle of the application.
     * so, if want to share something b/w the class and other activity we can do it from here.
     */


    companion object {
        private lateinit var instance: MainApplication
        private const val PREF_NAME = "app_prefs"
        private const val KEY_IS_LOGIN = "isLogin"

        fun setLoginState(value: Boolean) {
            val prefs = instance.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_IS_LOGIN, value).apply()
        }

        fun isLogin(): Boolean {
            val prefs = instance.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            return prefs.getBoolean(KEY_IS_LOGIN, false)
        }
    }

    @Override
    override fun onCreate() {
        super.onCreate()
        instance = this
    }


}