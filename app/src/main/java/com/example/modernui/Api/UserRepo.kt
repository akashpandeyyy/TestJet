package com.example.modernui.Api

import android.content.Context
import com.example.modernui.Api.model.BankListResponse
import com.example.modernui.Api.model.Cmsresponce
import com.example.modernui.Api.model.LoginRequest
import com.example.modernui.Api.model.MtbBankResponse
import com.example.modernui.Api.model.UserResponse
import com.example.modernui.Api.model.ValidateUser
import com.example.modernui.Api.model.balanceresponce
import com.example.modernui.core.datastore.SessionManager
import com.example.modernui.ui.screens.aeps.AepsModel
//import com.example.modernui.ui.screens.aeps.AepsModelResponce
import com.example.modernui.ui.screens.aeps.AepsModelResponse
import com.example.modernui.ui.screens.common.model.TwoFAValiResponce
import com.example.modernui.ui.screens.common.model.TwoFAresponce
import com.example.modernui.ui.screens.common.model.TwoFaAuthrequest
import com.example.modernui.ui.screens.common.model.TwoFaFinalAuthResponse
import com.example.modernui.ui.screens.common.model.TwoFaValidationRequest
import com.example.modernui.ui.screens.dmt.jiomodel.CreateTnxResponse
import com.example.modernui.ui.screens.dmt.jiomodel.CreateTxnRequest
import com.example.modernui.ui.screens.dmt.jiomodel.KycDataRequest
import com.example.modernui.ui.screens.dmt.jiomodel.KycDataResponse
import com.example.modernui.ui.screens.dmt.jiomodel.ReValidateTnxRequest
import com.example.modernui.ui.screens.dmt.jiomodel.ReValidateTnxResponce
import com.example.modernui.ui.screens.dmt.jiomodel.RegbeniRequest
import com.example.modernui.ui.screens.dmt.jiomodel.SendOtpRequest
import com.example.modernui.ui.screens.dmt.jiomodel.SendOtpResponse
import com.example.modernui.ui.screens.dmt.jiomodel.ValidateTnxRequest
import com.example.modernui.ui.screens.dmt.jiomodel.ValidateTnxResponce
import com.example.modernui.ui.screens.dmt.jiomodel.ValidateUserRequest
import com.example.modernui.ui.screens.dmt.jiomodel.ValidateUserResponse
import com.example.modernui.ui.screens.dmt.jiomodel.VerifyOtpRequest
import com.example.modernui.ui.screens.dmt.jiomodel.VerifyOtpResponse
import com.example.modernui.ui.screens.login.otprequest
import com.example.modernui.ui.screens.mtb.model.Beniaddrequest
import com.example.modernui.ui.screens.mtb.model.PayoutRequest
import com.example.modernui.ui.screens.mtb.model.PayoutResponse
import com.example.modernui.ui.screens.recharge.fetchmodel.FetchDTHPlanRequest
import com.example.modernui.ui.screens.recharge.fetchmodel.FetchPlanResponse
import com.example.modernui.ui.screens.recharge.fetchmodel.dth.FetchDTHPlanResponce
import com.example.modernui.ui.screens.recharge.rechargemodel.RechargeRequest
import com.example.modernui.ui.screens.recharge.rechargemodel.RechargeResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepo @Inject constructor(
    @MainApi private val apiService: ApiService,
    @PlaceholderApi private val apiServiceTwoFa: ApiService,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) {
    suspend fun userLogin(request: LoginRequest): UserResponse {
        val response = apiService.userLogin(request)
        if (response.status == 1 && response.data != null) {
            val userData = response.data.userData
            sessionManager.saveSession(
                token = response.data.token ?: "",
                name = userData?.name ?: "",
                roleId = userData?.roleId ?: "",
                userId = userData?.userId ?: "",
                mobile = userData?.mobile ?: ""
            )
        }
        return response
    }


    suspend fun validateuserLogin(request: LoginRequest): ValidateUser {
        val response = apiService.validateuserLogin(request)
        if (response.status == 1 && response.data != null) {
            // Update session data if needed
            val userData = response.data
            // We might not have the token here, so we only update if we have a way to get it or if it's already there
        }
        return response
    }

    suspend fun validateuserAeps(request: AepsModel): AepsModelResponse {
        return apiService.validateuserAeps(request)
    }

    suspend fun checkAepsStatus(): TwoFAresponce     {
        return apiService.checkAepsStatus()
    }
    suspend fun validatetoken(request : TwoFaValidationRequest): TwoFAValiResponce {
        return apiServiceTwoFa.validatetoken(request)
    }


    suspend fun validateTwoFAfinal(request : TwoFaAuthrequest): TwoFaFinalAuthResponse {
        return apiServiceTwoFa.validateTwoFAfinal(request)
    }

    val userSessionFlow = sessionManager.userSessionFlow

    suspend fun fetchUserBalance(): balanceresponce {
        return apiService.fetchUserBalance()
    }

    suspend fun getBanks(): BankListResponse {
        return apiService.getBanks()
    }
    suspend fun doRecharge(request: RechargeRequest): RechargeResponse {
        return apiService.doRecharge(request)
    }
    suspend fun dodthRecharge(request: RechargeRequest): RechargeResponse {
        return apiService.dodthRecharge(request)
    }

    suspend fun fetchMtbData(userId: String): MtbBankResponse {
        val url = "${ApiEndpoints.MTB.MTB_LISTED_BANK}$userId"
        return apiService.fetchMtbData(url)
    }

    suspend fun addpayoutbank(request : Beniaddrequest): UserResponse {
        return apiService.addpayoutbank(request)
    }

    suspend fun payout(request : PayoutRequest): PayoutResponse {
        return apiService.payout(request)
    }
    suspend fun cmsscreen(): Cmsresponce {
        return apiService.cmsscreen()
    }

    suspend fun insuranceLead(): com.example.modernui.Api.model.InsuranceResponse {
        return apiService.insuranceLead()
    }

    // validate user otp
    suspend fun validateuserotp(request: otprequest): UserResponse {
        val response = apiService.validateuserotp(request)
        if (response.status == 1 && response.data != null) {
            val userData = response.data.userData
            sessionManager.saveSession(
                token = response.data.token ?: "",
                name = userData?.name ?: "",
                roleId = userData?.roleId ?: "",
                userId = userData?.userId ?: "",
                mobile = userData?.mobile ?: ""
            )
        }
        return response
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }

    private fun saveToken(token: String) {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("auth_token", token)
            apply()
        }
    }


        suspend fun fetchplan(mobileNumber: String): FetchPlanResponse {
            return apiService.fetchplan(mobileNumber)
    }
    suspend fun fetchdthplan(request : FetchDTHPlanRequest): FetchDTHPlanResponce {
            return apiService.fetchdthplan(request)
    }

    //DMT
    suspend fun jiodmtvalidatecustmoer(request : ValidateUserRequest): ValidateUserResponse {
        return apiService.jiodmtvalidatecustmoer(request)
    }

    suspend fun jiodmtcustmoerkyc(request : KycDataRequest): KycDataResponse {
        return apiService.jiodmtcustmoerkyc(request)
    }
 suspend fun jiodmtsendotp(request : SendOtpRequest): SendOtpResponse {
        return apiService.jiodmtsendotp(request)
    }
    suspend fun jiodmtverifyotp(request : VerifyOtpRequest): VerifyOtpResponse {
        return apiService.jiodmtverifyotp(request)
    }

    suspend fun regbeni(request : RegbeniRequest): ReValidateTnxResponce {
        return apiService.regbeni(request)
    }
    suspend fun createtnx(request : CreateTxnRequest): CreateTnxResponse {
        return apiService.createtnx(request)
    }
    suspend fun validatetnx(request : ValidateTnxRequest): ValidateTnxResponce {
        return apiService.validatetnx(request)
    }
    suspend fun resendtnx(request : ReValidateTnxRequest): ReValidateTnxResponce {
        return apiService.resendtnx(request)
    }


}
