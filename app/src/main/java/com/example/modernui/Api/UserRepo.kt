package com.example.modernui.Api

import android.content.Context
import com.example.modernui.ui.screens.aeps.AepsModel
import com.example.modernui.ui.screens.aeps.AepsModelResponce
import com.example.modernui.ui.screens.common.TwoFAresponce
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepo @Inject constructor(
    @MainApi private val apiService: ApiService,
    @ApplicationContext private val context: Context
) {
    suspend fun userLogin(request: LoginRequest): UserResponse {
        val response = apiService.userLogin(request)
        if (response.status == 1 && response.data?.token != null) {
            saveToken(response.data.token)
        }
        return response
    }

    suspend fun validateuserLogin(request: LoginRequest): ValidateUser {
        return apiService.validateuserLogin(request)
    }

    suspend fun validateuserAeps(request: AepsModel): AepsModelResponce {
        return apiService.validateuserAeps(request)
    }

    suspend fun checkAepsStatus(): TwoFAresponce {
        return apiService.checkAepsStatus()
    }

    private fun saveToken(token: String) {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("auth_token", token)
            apply()
        }
    }

    // Keeping getUser for compatibility if needed, but updating to use userLogin logic
    suspend fun getUser(username: String, password: String): UserResponse {
        return userLogin(LoginRequest(username, password))
    }
}
