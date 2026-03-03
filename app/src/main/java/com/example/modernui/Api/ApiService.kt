package com.example.modernui.Api

import retrofit2.http.Body
import retrofit2.http.GET

interface ApiService {
    @GET("/auth/login")
    suspend fun getUser(@Body request: UserRequest): UserResponse
}


