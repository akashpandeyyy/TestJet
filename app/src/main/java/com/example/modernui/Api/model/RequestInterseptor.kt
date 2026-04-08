package com.example.modernui.Api.model

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.example.modernui.BuildConfig
import com.example.modernui.core.datastore.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class RequestInterceptor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Fetch token & deviceId from DataStore (SessionManager)
        val sessionManager = SessionManager(context)
        val token = runBlocking {
            sessionManager.userSessionFlow.first().token ?: ""
        }
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        // Attach headers to EVERY request automatically
        val newRequest = originalRequest.newBuilder()
            .addHeader("Content-Type", "application/json; charset=UTF-8")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("deviceId", deviceId)
            .addHeader("appVersion", BuildConfig.VERSION_NAME)
            .build()

        Log.d("RequestInterceptor", "RequestInterceptor Request Hit")
        Log.d("RequestInterceptor", "RequestInterceptor  method")

        return chain.proceed(newRequest)
    }
}