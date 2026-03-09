package com.example.modernui.Api

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepo @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun userLogin(auth: String, deviceId: String, request: LoginRequest): UserResponse {
        return apiService.userLogin(auth, deviceId, request)
    }

    // Keeping getUser for compatibility if needed, but updating to use userLogin logic
    suspend fun getUser(username: String, password: String): UserResponse {
        return apiService.userLogin("Basic YOUR_TOKEN", "unknown", LoginRequest(username, password))
    }
}
