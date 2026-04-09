package com.example.modernui.ui.screens.common

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernui.Api.UserRepo
import com.example.modernui.ui.components.intentpackage.RdHelper
import com.example.modernui.ui.screens.aeps.AepsModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TwoFaUiState {
    object Idle : TwoFaUiState()
    object Verifying : TwoFaUiState()
    data class Success(val message: String) : TwoFaUiState()
    data class Error(val message: String) : TwoFaUiState()
}

@HiltViewModel
class TwoFaViewModel @Inject constructor(
    private val userRepo: UserRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow<TwoFaUiState>(TwoFaUiState.Idle)
    val uiState: StateFlow<TwoFaUiState> = _uiState.asStateFlow()

    private val _selectedDeviceId = MutableStateFlow("face")
    val selectedDeviceId: StateFlow<String> = _selectedDeviceId.asStateFlow()

    private var userMobile: String? = null

    init {
        viewModelScope.launch {
            userRepo.userSessionFlow.collect { session ->
                userMobile = session.mobile
            }
        }
    }

    fun onDeviceSelected(id: String) {
        _selectedDeviceId.value = id
    }

    fun handleRdServiceResult(responseData: String) {
        Log.d("TwoFaViewModel", "RD Service Response: $responseData")
        
        // Refined XML Parsing using Regex to extract errCode and errInfo
        // Supports both errCode="0" and errCode='0'
        val errCodeMatch = Regex("errCode\\s*=\\s*[\"']([^\"']*)[\"']", RegexOption.IGNORE_CASE).find(responseData)
        val errCode = errCodeMatch?.groupValues?.get(1)

        if (errCode == "0") {
            // Biometric captured successfully, now send to API
            performTwoFaVerification(responseData)
        } else {
            val errInfoMatch = Regex("errInfo\\s*=\\s*[\"']([^\"']*)[\"']", RegexOption.IGNORE_CASE).find(responseData)
            val errInfo = errInfoMatch?.groupValues?.get(1) ?: "Verification Failed"
            _uiState.value = TwoFaUiState.Error("$errInfo (Code: $errCode)")
        }
    }

    private fun performTwoFaVerification(pidDataXml: String) {
        viewModelScope.launch {
            _uiState.value = TwoFaUiState.Verifying
            try {
                // Here we call the API to verify the PID data.
                // Assuming we use the same AEPS transaction endpoint or a dedicated one.
                // Based on AepsModel, it has a 'biometric' field which likely takes the XML.
                
                val request = AepsModel(
                    aadhaar = null, 
                    amount = null,
                    biometric = pidDataXml,
                    iin = null,
                    latitude = "0.0",
                    longitude = "0.0",
                    mobile = userMobile, // Using mobile from session
                    source = "APP",
                    type = "2FA" // Special type for 2FA verification
                )

                val response = userRepo.validateuserAeps(request)
                
                if (response.status == 1) {
                    _uiState.value = TwoFaUiState.Success(response.message ?: "Verification Successful")
                } else {
                    _uiState.value = TwoFaUiState.Error(response.message ?: "API Verification Failed")
                }
            } catch (e: Exception) {
                Log.e("TwoFaViewModel", "API Error: ${e.message}", e)
                _uiState.value = TwoFaUiState.Error("Network Error: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = TwoFaUiState.Idle
    }
}
