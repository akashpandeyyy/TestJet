package com.example.modernui.Api

import ValidateUser
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
interface ApiService {
    @Headers("Content-Type: application/json; charset=UTF-8") // Static headers

    @POST("login/userLogin")
    suspend fun userLogin(
        @Header("Authorization") token: String,
        @Header("deviceId") deviceId: String,
        @Body request: LoginRequest
    ): UserResponse


    @POST("login/validateSession")
    suspend fun validateuserLogin(
        @Header("Authorization") token: String,
        @Header("deviceId") deviceId: String,
        @Body request: LoginRequest
    ): ValidateUser
}