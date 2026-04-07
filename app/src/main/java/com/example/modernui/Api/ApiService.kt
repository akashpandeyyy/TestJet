package com.example.modernui.Api


import com.example.modernui.Api.ApiEndpoints.Login.USER_LOGIN
import com.example.modernui.Api.ApiEndpoints.Login.USER_ValidateSession
import com.example.modernui.Api.ApiEndpoints.Aeps.AEPS_STATUS_APP
import com.example.modernui.Api.ApiEndpoints.Aeps.TRANSACTION
import com.example.modernui.Api.ApiEndpoints.Login.VALIDATE_OTP
import com.example.modernui.Api.model.LoginRequest
import com.example.modernui.Api.model.UserResponse
import com.example.modernui.Api.model.ValidateUser
import com.example.modernui.pagination.model.PagingResponceModel
import com.example.modernui.ui.screens.aeps.AepsModel
import com.example.modernui.ui.screens.aeps.AepsModelResponce
import com.example.modernui.ui.screens.common.TwoFAresponce
import com.example.modernui.ui.screens.login.otprequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService
{

    @POST(USER_LOGIN)
    suspend fun userLogin(
        @Body request: LoginRequest
    ): UserResponse

    @POST(VALIDATE_OTP)
    suspend fun validateuserotp(
        @Body request: otprequest
    ): UserResponse


    @POST(USER_ValidateSession)
    suspend fun validateuserLogin(
        @Body request: LoginRequest
    ): ValidateUser

    @POST(TRANSACTION)
    suspend fun validateuserAeps(
        @Body request: AepsModel
    ): AepsModelResponce

    @GET(AEPS_STATUS_APP)
    suspend fun checkAepsStatus(): TwoFAresponce


    @GET("posts")
    suspend fun getQuotes(@Query("page") page: Int): PagingResponceModel

}
