package com.example.modernui.Api.twofamodel


import android.content.Context
import android.provider.Settings
import android.util.Log
import com.example.modernui.BuildConfig
import com.example.modernui.core.datastore.SessionManager
import com.example.modernui.ui.screens.common.PrefManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class TFIntersecptorRequest(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Fetch token & deviceId from DataStore (SessionManager)
        val sessionManager = SessionManager(context)

        val token = PrefManager(context).getToken() ?: ""

        val newRequest = originalRequest.newBuilder()
            .addHeader("Content-Type", "application/json; charset=UTF-8")
            .addHeader("Authorization", token)
            .build()



        Log.d("RequestInterceptor", "RequestInterceptor Request Hit")
        Log.d("RequestInterceptor", "RequestInterceptor  method")

        return chain.proceed(newRequest)
    }
}