package com.example.modernui.Api


import com.example.modernui.Api.ApiEndpoints.Admin.FETCH_INCODE_BY_SERVICE
import com.example.modernui.Api.ApiEndpoints.Login.USER_LOGIN
import com.example.modernui.Api.ApiEndpoints.Login.USER_ValidateSession
import com.example.modernui.Api.ApiEndpoints.Aeps.AEPS_STATUS_APP
import com.example.modernui.Api.ApiEndpoints.CMS.CMS
import com.example.modernui.Api.ApiEndpoints.Insurance.Insurance as INSURANCE_ENDPOINT
import com.example.modernui.Api.ApiEndpoints.Aeps.TRANSACTION as AEPS_TRANSACTION
import com.example.modernui.Api.ApiEndpoints.Recharge.TRANSACTION as RECHARGE_TRANSACTION
import com.example.modernui.Api.ApiEndpoints.Recharge.MOBILE_PLANS
import com.example.modernui.Api.ApiEndpoints.User.FETCH_USER_BALANCE
import com.example.modernui.Api.ApiEndpoints.User.BANKS
import com.example.modernui.Api.ApiEndpoints.MTB.MTB as MTB_ENDPOINT
import com.example.modernui.Api.ApiEndpoints.Login.VALIDATE_OTP
import com.example.modernui.Api.model.Cmsresponce
import com.example.modernui.Api.model.InsuranceResponse
import com.example.modernui.Api.model.LoginRequest
import com.example.modernui.Api.model.UserResponse
import com.example.modernui.Api.model.ValidateUser
import com.example.modernui.Api.model.balanceresponce
import com.example.modernui.ui.screens.aeps.AepsModel
import com.example.modernui.ui.screens.aeps.AepsModelResponce
import com.example.modernui.ui.screens.common.model.TwoFAValiResponce
import com.example.modernui.ui.screens.common.model.TwoFAresponce
import com.example.modernui.ui.screens.common.model.TwoFaAuthrequest
import com.example.modernui.ui.screens.common.model.TwoFaFinalAuthResponse
import com.example.modernui.ui.screens.common.model.TwoFaValidationRequest
import com.example.modernui.ui.screens.login.otprequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

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

    @POST(AEPS_TRANSACTION)
    suspend fun validateuserAeps(
        @Body request: AepsModel
    ): AepsModelResponce

    @GET(AEPS_STATUS_APP)
    suspend fun checkAepsStatus(): TwoFAresponce



    // TwoFA

    @POST("https://pro.softmintindia.com/sdk/validate2FAToken")
    suspend fun validatetoken(
        @Body request: TwoFaValidationRequest
    ): TwoFAValiResponce



    @POST("https://pro.softmintindia.com/sdk/twoFactorAuthentication")
    suspend fun validateTwoFAfinal(
        @Body request: TwoFaAuthrequest
    ): TwoFaFinalAuthResponse




    // Recharge APIs
    @POST(RECHARGE_TRANSACTION)
    suspend fun doRecharge(
        @Body request: Map<String, String>
    ): UserResponse

    @GET(MOBILE_PLANS)
    suspend fun getMobilePlans(
        @Query("operator") operator: String,
        @Query("circle") circle: String
    ): UserResponse

    // User APIs
    @GET(FETCH_USER_BALANCE)
    suspend fun fetchUserBalance(): balanceresponce

    @GET(BANKS)
    suspend fun getBanks(): UserResponse

    // Admin APIs
    @GET(FETCH_INCODE_BY_SERVICE)
    suspend fun fetchIncodeByService(@Query("service") service: String): UserResponse

    // MTB APIs
    @POST(MTB_ENDPOINT)
    suspend fun fetchMtbData(): UserResponse

    // CMS APIs
    @GET(CMS)
    suspend fun cmsscreen(): Cmsresponce

    // Insurance APIs
    @GET(INSURANCE_ENDPOINT)
    suspend fun insuranceLead(): InsuranceResponse

}

//@GET("posts")
//suspend fun getQuotes(@Query("page") page: Int): PagingResponceModel