package com.example.modernui.Api

import okhttp3.Interceptor

class ResponseInterseptor {
    val responseInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())

        // Check karo status code
        if (response.code == 401 || response.code == 403) {
            // Logout logic yahan aayega
            println("Session Expired! Logging out...")

            // Android mein login page pe bhejne ka logic (Intent use karke)
            // Note: Yahan se Activity start karne ke liye Context ki zaroorat padegi
        }

        response
    }
}