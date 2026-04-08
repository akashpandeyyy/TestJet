package com.example.modernui.Api.model

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.modernui.core.datastore.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class ResponseInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        
        // Clone the response to read the body without consuming it
        val bodyString = response.body?.string() ?: ""
        val newResponse = response.newBuilder()
            .body(bodyString.toResponseBody(response.body?.contentType()))
            .build()

        // Check for session expiry
        var isSessionExpired = response.code == 401 || response.code == 403
        var reason = "Session Expired"

        if (!isSessionExpired) {
            try {
                // Parse the response using Gson to check the "status" field or "message"
                val userResponse = Gson().fromJson(bodyString, UserResponse::class.java)
                
                if (userResponse != null) {
                    isSessionExpired = userResponse.status == 3 ||
                            (userResponse.status == 0 && userResponse.message?.contains("invalid token", ignoreCase = true) == true) ||
                            (userResponse.status == 0 && userResponse.message?.contains("session expired", ignoreCase = true) == true)
                    
                    if (isSessionExpired) {
                        reason = userResponse.message ?: userResponse.errorMessage ?: "Session Expired"
                    }
                }
            } catch (e: Exception) {
                // Not a UserResponse or parsing failed, ignore
            }
        }

        if (isSessionExpired) {
            Log.e("ResponseInterceptor", "Redirecting to login: $reason")
            handleLogoutAndRedirect(reason)
        }

        return newResponse
    }

    private fun handleLogoutAndRedirect(reason: String) {
        // 1. Clear DataStore session
        val sessionManager = SessionManager(context)
        runBlocking {
            sessionManager.clearSession()
        }

        // 2. Clear SharedPreferences
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().clear().apply()

        // 3. Show Reason Toast (on UI thread)
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, reason, Toast.LENGTH_LONG).show()
        }

        // 4. Redirect to Login (Restarting app to clear state/navigation)
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }
}
