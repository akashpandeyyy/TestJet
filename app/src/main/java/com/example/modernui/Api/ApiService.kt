package com.example.modernui.Api


import com.example.modernui.pagination.model.PagingResponceModel
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService
{

    @POST("login/userLogin")
    suspend fun userLogin(
        @Body request: LoginRequest
    ): UserResponse

    @POST("login/validateSession")
    suspend fun validateuserLogin(
        @Body request: LoginRequest
    ): ValidateUser
    @GET("posts")
    suspend fun getQuotes(@Query("page") page: Int): PagingResponceModel

}