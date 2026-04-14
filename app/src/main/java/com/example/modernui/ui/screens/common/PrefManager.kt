package com.example.modernui.ui.screens.common

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefManager @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun saveToken(twofaauth_token: String) {
        prefs.edit().putString("twofaauth_token", twofaauth_token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("twofaauth_token", null)
    }

    fun clearToken() {
        prefs.edit().remove("auth_token").apply()
    }
}