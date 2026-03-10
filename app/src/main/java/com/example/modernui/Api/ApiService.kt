package com.example.modernui.Api


import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
interface ApiService {

    @POST("login/userLogin")
    suspend fun userLogin(
        @Body request: LoginRequest
    ): UserResponse

    @POST("login/validateSession")
    suspend fun validateuserLogin(
        @Body request: LoginRequest
    ): ValidateUser

}