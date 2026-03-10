package com.example.modernui.Api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class ResponseInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        when (response.code) {
            401 -> {
                Log.e("ResponseInterceptor", "Unauthorized! Token expired.")
                // clear token + redirect to login
            }
            403 -> {
                Log.e("ResponseInterceptor", "Forbidden! Access denied.")
            }
            500 -> {
                Log.e("ResponseInterceptor", "Server Error!")
            }
        }



        return response
    }
}