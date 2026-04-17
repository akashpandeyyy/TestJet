package com.example.modernui.Api


import com.example.modernui.Api.ApiEndpoints.Login.USER_LOGIN
import com.example.modernui.Api.ApiEndpoints.Login.USER_ValidateSession
import com.example.modernui.Api.ApiEndpoints.Aeps.AEPS_STATUS_APP
import com.example.modernui.Api.ApiEndpoints.Aeps.AEPS_TRANSACTION
import com.example.modernui.Api.ApiEndpoints.BankList.Bank_List
import com.example.modernui.Api.ApiEndpoints.CMS.CMS
import com.example.modernui.Api.ApiEndpoints.Insurance.Insurance as INSURANCE_ENDPOINT
import com.example.modernui.Api.ApiEndpoints.Recharge.TRANSACTION as RECHARGE_TRANSACTION
import com.example.modernui.Api.ApiEndpoints.Recharge.MOBILE_PLANS
import com.example.modernui.Api.ApiEndpoints.User.FETCH_USER_BALANCE

import com.example.modernui.Api.ApiEndpoints.Login.VALIDATE_OTP
import com.example.modernui.Api.ApiEndpoints.MTB.MTB_ADD_PAYOUT_BANK
import com.example.modernui.Api.ApiEndpoints.MTB.MTB_PAYOUT
import com.example.modernui.Api.model.BankListResponse
import com.example.modernui.Api.model.Cmsresponce
import com.example.modernui.Api.model.InsuranceResponse
import com.example.modernui.Api.model.LoginRequest
import com.example.modernui.Api.model.MtbBankResponse
import com.example.modernui.Api.model.UserResponse
import com.example.modernui.Api.model.ValidateUser
import com.example.modernui.Api.model.balanceresponce
import com.example.modernui.ui.screens.aeps.AepsModel
import com.example.modernui.ui.screens.aeps.AepsModelResponse
import com.example.modernui.ui.screens.common.model.TwoFAValiResponce
import com.example.modernui.ui.screens.common.model.TwoFAresponce
import com.example.modernui.ui.screens.common.model.TwoFaAuthrequest
import com.example.modernui.ui.screens.common.model.TwoFaFinalAuthResponse
import com.example.modernui.ui.screens.common.model.TwoFaValidationRequest
import com.example.modernui.ui.screens.login.otprequest
import com.example.modernui.ui.screens.mtb.model.Beniaddrequest
import com.example.modernui.ui.screens.mtb.model.PayoutRequest
import com.example.modernui.ui.screens.mtb.model.PayoutResponse
import com.example.modernui.ui.screens.recharge.fetchmodel.FetchPlanResponse
import com.example.modernui.ui.screens.recharge.rechargemodel.RechargeRequest
import com.example.modernui.ui.screens.recharge.rechargemodel.RechargeResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Url

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
    ): AepsModelResponse

    @GET(AEPS_STATUS_APP)
    suspend fun checkAepsStatus(): TwoFAresponce

    // Bank List
    @GET(Bank_List)
    suspend fun getBanks(): BankListResponse

    // AEPS Transction


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
        @Body request: RechargeRequest
    ): RechargeResponse

    @GET(MOBILE_PLANS)
    suspend fun fetchplan(@Path("mobileNumber") mobileNumber: String): FetchPlanResponse


    // User APIs
    @GET(FETCH_USER_BALANCE)
    suspend fun fetchUserBalance(): balanceresponce



    // MTB APIs
    @POST
    suspend fun fetchMtbData(@Url url: String): MtbBankResponse
    @POST(MTB_ADD_PAYOUT_BANK)
    suspend fun addpayoutbank(
        @Body request: Beniaddrequest
    ): UserResponse

    @POST(MTB_PAYOUT)
    suspend fun payout(
        @Body request: PayoutRequest
    ): PayoutResponse

    // CMS APIs
    @GET(CMS)
    suspend fun cmsscreen(): Cmsresponce

    // Insurance APIs
    @GET(INSURANCE_ENDPOINT)
    suspend fun insuranceLead(): InsuranceResponse

}

//@GET("posts")
//suspend fun getQuotes(@Query("page") page: Int): PagingResponceModel