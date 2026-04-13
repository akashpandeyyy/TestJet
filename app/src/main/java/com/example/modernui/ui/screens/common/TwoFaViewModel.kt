package com.example.modernui.ui.screens.common

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernui.Api.UserRepo
import com.example.modernui.ui.screens.common.model.TwoFaAuthrequest
import com.example.modernui.ui.screens.common.model.TwoFaValidationRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val userRepo: UserRepo,
    private val locationManager:
) : ViewModel() {

    private val _uiState = MutableStateFlow<TwoFaUiState>(TwoFaUiState.Idle)
    val uiState: StateFlow<TwoFaUiState> = _uiState.asStateFlow()

    private val _selectedDeviceId = MutableStateFlow("face")
    val selectedDeviceId: StateFlow<String> = _selectedDeviceId.asStateFlow()

    private var userMobile: String? = null

    // Finger selection state
    data class FingerOption(val label: String, val value: String)

    private val _availableFingers = MutableStateFlow<List<FingerOption>>(emptyList())
    val availableFingers: StateFlow<List<FingerOption>> = _availableFingers.asStateFlow()

    private val _selectedFinger = MutableStateFlow<FingerOption?>(null)
    val selectedFinger: StateFlow<FingerOption?> = _selectedFinger.asStateFlow()

    private var currentOrderId: String? = null

    init {
        viewModelScope.launch {
            userRepo.userSessionFlow.collect { session ->
                userMobile = session.mobile
            }
        }
        setupTwoFa()
    }

    fun onDeviceSelected(id: String) {
        _selectedDeviceId.value = id
    }

    fun handleRdServiceResult(responseData: String) {
        Log.d("TwoFaViewModel", "RD Service Response: $responseData")

        val errCodeMatch = Regex("errCode\\s*=\\s*[\"']([^\"']*)[\"']", RegexOption.IGNORE_CASE).find(responseData)
        val errCode = errCodeMatch?.groupValues?.get(1)

        if (errCode == "0") {
            performTwoFaVerification(responseData)
        } else {
            val errInfoMatch = Regex("errInfo\\s*=\\s*[\"']([^\"']*)[\"']", RegexOption.IGNORE_CASE).find(responseData)
            val errInfo = errInfoMatch?.groupValues?.get(1) ?: "Verification Failed"
            _uiState.value = TwoFaUiState.Error("$errInfo (Code: $errCode)")
        }
    }



    fun setupTwoFa() {
        viewModelScope.launch {
            _uiState.value = TwoFaUiState.Verifying
            try {
                val statusResponse = userRepo.checkAepsStatus()
                if (statusResponse.status == 17) {
                    val tokenElement = statusResponse.data
                    val request = TwoFaValidationRequest(token = tokenElement)
                    Log.d("TwoFaViewModel", "Token Fixed: $tokenElement")
                    val validationRes = userRepo.validatetoken(request)
                    currentOrderId = validationRes.data?.orderId




                    if (validationRes != null) {


                        val list = mutableListOf<FingerOption>()
                        if (validationRes.data?.fing1 == true) list.add(FingerOption("AIRTEL", "F1"))
                        if (validationRes.data?.fing2 == true) list.add(FingerOption("NSDL", "F2"))
                        if (validationRes.data?.fing3 == true) list.add(FingerOption("FINO", "F3"))
                        if (validationRes.data?.fing4 == true) list.add(FingerOption("JIO", "F4"))

                        _availableFingers.value = list
                        if (list.isNotEmpty()) _selectedFinger.value = list[0]
                        _uiState.value = TwoFaUiState.Idle
                    } else {
                        _uiState.value = TwoFaUiState.Error("Invalid Data Received")
                    }
                } else if (statusResponse.status == 1) {
                    _uiState.value = TwoFaUiState.Success(statusResponse.message ?: "Already Authorized")
                } else {
                    _uiState.value = TwoFaUiState.Error(statusResponse.message ?: "Status: ${statusResponse.status}")
                }
            } catch (e: Exception) {
                Log.e("TwoFaViewModel", "Setup Error: ${e.message}", e)
                _uiState.value = TwoFaUiState.Error("Failed to fetch 2FA status: ${e.message}")
            }
        }
    }

    fun onFingerSelected(option: FingerOption) {
        _selectedFinger.value = option
    }

    fun resetState() {
        _uiState.value = TwoFaUiState.Idle
    }

//    private fun performTwoFaVerification(pidDataXml: String) {
//        viewModelScope.launch {
//            _uiState.value = TwoFaUiState.Verifying
//            try {
//
//
//                val orderId = currentOrderId ?: ""
//                val request = TwoFaAuthrequest(
//
//                    biometric = pidDataXml,
//                    orderId = currentOrderId ?: "",
//                    fing = _selectedFinger.value?.value ?: "",
//                    latitude = locationManager.getLat(), // instance
//                    longitude = locationManager.getLng(),
//                    source = "APP"
//                )
//
//                val response = userRepo.validateTwoFAfinal(request)
//
//                if (response.status == 1) {
//                    _uiState.value = TwoFaUiState.Success(response.message ?: "Verification Successful")
//                } else {
//                    _uiState.value = TwoFaUiState.Error(response.message ?: "API Verification Failed")
//                }
//            } catch (e: Exception) {
//                Log.e("TwoFaViewModel", "API Error: ${e.message}", e)
//                _uiState.value = TwoFaUiState.Error("Network Error: ${e.message}")
//            }
//        }
//    }
private fun performTwoFaVerification(pidDataXml: String) {
    viewModelScope.launch {
        _uiState.value = TwoFaUiState.Verifying
        try {


            val userLocation = UserLocation(this)
            userLocation.fetchAndSaveLocation(this)




            val request = TwoFaAuthrequest(
                biometric = pidDataXml,
                orderId = currentOrderId ?: "",
                fing = _selectedFinger.value?.value ?: "",
               latitude = location.latitude.toString(),
                longitude = location.longitude.toString(),
                source = "APP"
            )

            val response = userRepo.validateTwoFAfinal(request)
            Log.e("TwoFaViewModel", "Setup Error: ${response}")

                if (response.status == 1) {
                    _uiState.value = TwoFaUiState.Success(response.message ?: "Verification Successful")
                } else {
                    _uiState.value = TwoFaUiState.Error(response.message ?: "API Verification Failed")
                }

        } catch (e: Exception) {
            _uiState.value = TwoFaUiState.Error("Error: ${e.message}")
        }
    }
}
}
