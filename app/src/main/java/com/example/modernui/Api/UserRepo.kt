package com.example.modernui.Api

import android.content.Context
import com.example.modernui.Api.model.Cmsresponce
import com.example.modernui.Api.model.LoginRequest
import com.example.modernui.Api.model.UserResponse
import com.example.modernui.Api.model.ValidateUser
import com.example.modernui.Api.model.balanceresponce
import com.example.modernui.core.datastore.SessionManager
import com.example.modernui.ui.screens.aeps.AepsModel
import com.example.modernui.ui.screens.aeps.AepsModelResponce
import com.example.modernui.ui.screens.common.model.TwoFAValiResponce
import com.example.modernui.ui.screens.common.model.TwoFAresponce
import com.example.modernui.ui.screens.common.model.TwoFaAuthrequest
import com.example.modernui.ui.screens.common.model.TwoFaFinalAuthResponse
import com.example.modernui.ui.screens.common.model.TwoFaValidationRequest
import com.example.modernui.ui.screens.login.otprequest
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

    //2fa validataion and call





    suspend fun validateuserLogin(request: LoginRequest): ValidateUser {
        val response = apiService.validateuserLogin(request)
        if (response.status == 1 && response.data != null) {
            // Update session data if needed
            val userData = response.data
            // We might not have the token here, so we only update if we have a way to get it or if it's already there
        }
        return response
    }

    suspend fun validateuserAeps(request: AepsModel): AepsModelResponce {
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

    suspend fun doRecharge(request: Map<String, String>): UserResponse {
        return apiService.doRecharge(request)
    }

    suspend fun getMobilePlans(operator: String, circle: String): UserResponse {
        return apiService.getMobilePlans(operator, circle)
    }

    suspend fun fetchUserBalance(): balanceresponce {
        return apiService.fetchUserBalance()
    }

    suspend fun getBanks(): UserResponse {
        return apiService.getBanks()
    }

    suspend fun fetchIncodeByService(service: String): UserResponse {
        return apiService.fetchIncodeByService(service)
    }

    suspend fun fetchMtbData(): UserResponse {
        return apiService.fetchMtbData()
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

    // Keeping getUser for compatibility if needed, but updating to use userLogin logic
    suspend fun getUser(username: String, password: String): UserResponse {
        return userLogin(LoginRequest(username, password))
    }
}
