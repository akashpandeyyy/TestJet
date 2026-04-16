package com.example.modernui.ui.screens.aeps

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernui.Api.UserRepo
import com.example.modernui.Api.model.BankItem
import com.example.modernui.core.location.UserLocationProvider
import com.example.modernui.ui.components.intentpackage.RdHelper
import com.example.modernui.ui.screens.common.TwoFaUiState
import com.example.modernui.ui.screens.common.model.TwoFaValidationRequest
import com.google.gson.JsonPrimitive
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AepsUiState {
    object Idle : AepsUiState()
    object Loading : AepsUiState()
    data class Success(val response: AepsModelResponse) : AepsUiState()
    data class Error(val message: String, val response: AepsModelResponse? = null) : AepsUiState()
    object Needs2FA : AepsUiState() // Status is 17
}

sealed class RdCaptureState {
    object Idle : RdCaptureState()
    data class Capture(
        val action: String,
        val packageName: String,
        val inputKey: String,
        val pidOptions: String
    ) : RdCaptureState()
}

enum class VerificationStep {
    IDLE, SCANNING, SUCCESS, ERROR
}



@HiltViewModel
class AepsViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val locationProvider: UserLocationProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<AepsUiState>(AepsUiState.Idle)
    val uiState: StateFlow<AepsUiState> = _uiState.asStateFlow()

    private val _isAepsAuthorized = MutableStateFlow(false)
    val isAepsAuthorized: StateFlow<Boolean> = _isAepsAuthorized.asStateFlow()

    private val _scanState = MutableStateFlow(VerificationStep.IDLE)
    val scanState: StateFlow<VerificationStep> = _scanState.asStateFlow()

    private val _rdCaptureState = MutableStateFlow<RdCaptureState>(RdCaptureState.Idle)
    val rdCaptureState: StateFlow<RdCaptureState> = _rdCaptureState.asStateFlow()

    // Form State
    var aadhaarNumber = MutableStateFlow("")
    var mobileNumber = MutableStateFlow("")
    var amount = MutableStateFlow("")
    var selectedBank = MutableStateFlow<BankItem?>(null)
    var selectedTxnType = MutableStateFlow("")
    var selectedDevice = MutableStateFlow("face_scan")
    var biometricData = MutableStateFlow("")

    private val _banks = MutableStateFlow<List<BankItem>>(emptyList())
    val banks: StateFlow<List<BankItem>> = _banks.asStateFlow()


    fun handleRdServiceResult(responseData: String) {
        Log.d("AepsViewModel", "RD Service Response: $responseData")

        // Parse errCode from XML
        val errCodeMatch = Regex("errCode\\s*=\\s*[\"']([^\"']*)[\"']", RegexOption.IGNORE_CASE).find(responseData)
        val errCode = errCodeMatch?.groupValues?.get(1)

        if (errCode == "0") {
            // Success: update biometric data and call transaction
            biometricData.value = responseData
            _scanState.value = VerificationStep.SUCCESS
            performTransaction()
        } else {
            // Failure: update UI with error message
            val errInfoMatch = Regex("errInfo\\s*=\\s*[\"']([^\"']*)[\"']", RegexOption.IGNORE_CASE).find(responseData)
            val errInfo = errInfoMatch?.groupValues?.get(1) ?: "Verification Failed"
            _uiState.value = AepsUiState.Error("$errInfo (Code: $errCode)")
            _scanState.value = VerificationStep.ERROR
        }
    }

    fun cusTwoFA() {
        viewModelScope.launch {
            _uiState.value = AepsUiState.Loading

            try {
                val currentDevice = selectedDevice.value
                val deviceId = when (currentDevice) {
                    "face_scan" -> "face"
                    "mantra_mfs110" -> "mfs110"
                    "mantra_iris" -> "mantra_mis100v2"
                    "morpho_l1" -> "morpho_l1"
                    else -> currentDevice
                }

                val pidOptData = when (deviceId) {
                    "face" -> RdHelper.faceType
                    "mfs110", "morpho_l1" -> RdHelper.fingType
                    "mantra_mis100v2" -> RdHelper.irisType
                    else -> RdHelper.fingType
                }

                val pidOptions = RdHelper.makePidXm(deviceId, pidOptData)
                val action = RdHelper.getAction(deviceId)
                val packageName = RdHelper.getPackage(deviceId)
                val inputKey = RdHelper.getInputKey(deviceId)

                _rdCaptureState.value = RdCaptureState.Capture(
                    action = action,
                    packageName = packageName,
                    inputKey = inputKey,
                    pidOptions = pidOptions
                )
                
                _uiState.value = AepsUiState.Idle
            }
            catch (e: Exception) {
                Log.e("AepsViewModel", "cusTwoFA initiation Error", e)
                _uiState.value = AepsUiState.Error("Capture initialization failed: ${e.message}")
            }
        }
    }

    fun checkAepsStatusAndNavigate(onNavigateToAeps: () -> Unit, onNavigateTo2FA: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AepsUiState.Loading
            try {
                val statusResponse = userRepo.checkAepsStatus()
                Log.d("AepsViewModel", "Initial Status: ${statusResponse.status}")

                when (statusResponse.status) {
                    1 -> {
                        // User already authorized for AEPS
                        _isAepsAuthorized.value = true
                        _uiState.value = AepsUiState.Idle
                        onNavigateToAeps()
                    }

                    17 -> {
                        val tokenElement = statusResponse.data
                        if (tokenElement != null) {
                            try {
                                _uiState.value = AepsUiState.Loading
                                val request = TwoFaValidationRequest(token = tokenElement)
                                Log.d("AepsViewModel", "Token Fixed: $tokenElement")
                                val validationRes = userRepo.validatetoken(request)

                                when (validationRes.status) {
                                    18 -> {
                                        _isAepsAuthorized.value = false
                                        _uiState.value = AepsUiState.Needs2FA
                                        onNavigateTo2FA()
                                    }
                                    else -> {
                                        _uiState.value = AepsUiState.Error(validationRes.message ?: "2FA Validation Failed")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("AepsViewModel", "API Error: ${e.message}")
                                _uiState.value = AepsUiState.Error("Network error: ${e.message}")
                            }
                        } else {
                            _uiState.value = AepsUiState.Error("Token missing in status 17")
                        }
                    }

                    else -> {
                        _isAepsAuthorized.value = false
                        _uiState.value = AepsUiState.Error(statusResponse.message ?: "Unauthorized (Status: ${statusResponse.status})")
                    }
                }
            } catch (e: Exception) {
                Log.e("AepsViewModel", "Error: ${e.message}")
                _uiState.value = AepsUiState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    // Fetch Banklist
    fun refreshBanklist() {
        viewModelScope.launch {
            try {
                val bnklistresponse = userRepo.getBanks()
                if (bnklistresponse.status == 1) {
                    _banks.value = bnklistresponse.data ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("AepsViewModel", "Error fetching banks: ${e.message}")
            }
        }
    }

    fun onAadhaarChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 12) {
            aadhaarNumber.value = value
        }
    }

    fun onMobileChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 10) {
            mobileNumber.value = value
        }
    }

    fun onAmountChange(value: String) {
        amount.value = value
    }

    fun onBankSelected(bank: BankItem) {
        selectedBank.value = bank
    }

    fun onTxnTypeSelected(value: String) {
        selectedTxnType.value = value
    }

    fun onDeviceSelected(id: String) {
        selectedDevice.value = id
    }

    fun setScanState(state: VerificationStep) {
        _scanState.value = state
    }

    fun onScanComplete(pidData: String) {
        handleRdServiceResult(pidData)
    }

    fun resetRdCaptureState() {
        _rdCaptureState.value = RdCaptureState.Idle
    }
    fun performTransaction() {
        viewModelScope.launch {
            _uiState.value = AepsUiState.Loading

            try {
                val location = locationProvider.getCurrentLocation()
                if (location == null) {
                    _uiState.value = AepsUiState.Error("Unable to fetch location. Please enable GPS.")
                    return@launch
                }

                val lat = location.first
                val long = location.second

                val type = when (selectedTxnType.value) {
                    "Cash Withdrawal" -> "CW"
                    "Balance Enquiry" -> "BE"
                    "Mini Statement" -> "MS"
                    else -> "BE"
                }

                val bank = selectedBank.value
                val iinString = "${bank?.iin},${bank?.bankname}"

                val request = AepsModel(
                    aadhaar = aadhaarNumber.value,
                    mobile = mobileNumber.value,
                    amount = if (type == "CW") amount.value else "0",
                    latitude = lat,
                    longitude = long,
                    type = type,
                    iin = iinString,
                    biometric = biometricData.value, // Use actual scan data
                    source = "APP"
                )

                val response = userRepo.validateuserAeps(request)

                if (response.data?.status == "SUCCESS") {
                    _uiState.value = AepsUiState.Success(response)
                } else if (response.status == 1) {
                    _uiState.value = AepsUiState.Error(
                        message = response.message ?: "Transaction Failed",
                        response = response
                    )
                } else if (response.status == 17) {
                    _uiState.value = AepsUiState.Needs2FA
                } else {
                    _uiState.value = AepsUiState.Error(
                        message = response.message ?: "Transaction Failed",
                        response = response
                    )
                }

            } catch (e: Exception) {
                Log.e("AepsViewModel", "Transaction Error: ${e.message}")
                _uiState.value = AepsUiState.Error("Network error: ${e.message}")
                _scanState.value = VerificationStep.ERROR
            }
        }
    }

    fun resetState() {
        _uiState.value = AepsUiState.Idle
    }
}

