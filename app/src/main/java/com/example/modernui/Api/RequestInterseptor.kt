package com.example.modernui.Api

import okhttp3.Interceptor

val requestInterceptor = Interceptor { chain ->
    val originalRequest = chain.request()

    // Nayi request banao purani wali ko modify karke
    val modifiedRequest = originalRequest.newBuilder()
        .addHeader("Authorization", "Bearer YOUR_TOKEN_HERE")
        .addHeader("deviceid", "ANDROID_12345")
        .build()

    chain.proceed(modifiedRequest) // Request ko aage bhejo
}

