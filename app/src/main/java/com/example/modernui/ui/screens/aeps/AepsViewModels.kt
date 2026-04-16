//package com.example.modernui.ui.screens.aeps
//
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.modernui.Api.UserRepo
//import com.example.modernui.Api.model.BankItem
//import com.example.modernui.core.location.UserLocationProvider
//import com.example.modernui.ui.components.intentpackage.RdHelper
//import com.example.modernui.ui.screens.common.TwoFaUiState
//import com.example.modernui.ui.screens.common.model.TwoFaValidationRequest
//import com.google.gson.JsonPrimitive
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//sealed class AepsUiState {
//    object Idle : AepsUiState()
//    object Loading : AepsUiState()
//    data class Success(val message: String, val txnId: String) : AepsUiState()
//    data class Error(val message: String) : AepsUiState()
//    object Needs2FA : AepsUiState() // Status is 17
//}
//
//sealed class RdCaptureState {
//    object Idle : RdCaptureState()
//    data class Capture(
//        val action: String,
//        val packageName: String,
//        val inputKey: String,
//        val pidOptions: String
//    ) : RdCaptureState()
//}
//
//enum class VerificationStep {
//    IDLE, SCANNING, SUCCESS, ERROR
//}
//
//
//
//@HiltViewModel
//class AepsViewModel @Inject constructor(
//    private val userRepo: UserRepo,
//    private val locationProvider: UserLocationProvider
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow<AepsUiState>(AepsUiState.Idle)
//    val uiState: StateFlow<AepsUiState> = _uiState.asStateFlow()
//
//    private val _isAepsAuthorized = MutableStateFlow(false)
//    val isAepsAuthorized: StateFlow<Boolean> = _isAepsAuthorized.asStateFlow()
//
//    private val _scanState = MutableStateFlow(VerificationStep.IDLE)
//    val scanState: StateFlow<VerificationStep> = _scanState.asStateFlow()
//
//    private val _rdCaptureState = MutableStateFlow<RdCaptureState>(RdCaptureState.Idle)
//    val rdCaptureState: StateFlow<RdCaptureState> = _rdCaptureState.asStateFlow()
//
//    // Form State
//    var aadhaarNumber = MutableStateFlow("")
//    var mobileNumber = MutableStateFlow("")
//    var amount = MutableStateFlow("")
//    var selectedBank = MutableStateFlow<BankItem?>(null)
//    var selectedTxnType = MutableStateFlow("")
//    var selectedDevice = MutableStateFlow("face_scan")
//    var biometricData = MutableStateFlow("")
//
//    private val _banks = MutableStateFlow<List<BankItem>>(emptyList())
//    val banks: StateFlow<List<BankItem>> = _banks.asStateFlow()
//
//
//    fun handleRdServiceResult(responseData: String) {
//        Log.d("AepsViewModel", "RD Service Response: $responseData")
//
//        // Parse errCode from XML
//        val errCodeMatch = Regex("errCode\\s*=\\s*[\"']([^\"']*)[\"']", RegexOption.IGNORE_CASE).find(responseData)
//        val errCode = errCodeMatch?.groupValues?.get(1)
//
//        if (errCode == "0") {
//            // Success: update biometric data and call transaction
//            biometricData.value = responseData
//            _scanState.value = VerificationStep.SUCCESS
//            performTransaction()
//        } else {
//            // Failure: update UI with error message
//            val errInfoMatch = Regex("errInfo\\s*=\\s*[\"']([^\"']*)[\"']", RegexOption.IGNORE_CASE).find(responseData)
//            val errInfo = errInfoMatch?.groupValues?.get(1) ?: "Verification Failed"
//            _uiState.value = AepsUiState.Error("$errInfo (Code: $errCode)")
//            _scanState.value = VerificationStep.ERROR
//        }
//    }
//
//    fun cusTwoFA() {
//        viewModelScope.launch {
//            _uiState.value = AepsUiState.Loading
//
//            try {
//                val currentDevice = selectedDevice.value
//                val deviceId = when (currentDevice) {
//                    "face_scan" -> "face"
//                    "mantra_mfs110" -> "mfs110"
//                    "mantra_iris" -> "mantra_mis100v2"
//                    "morpho_l1" -> "morpho_l1"
//                    else -> currentDevice
//                }
//
//                val pidOptData = when (deviceId) {
//                    "face" -> RdHelper.faceType
//                    "mfs110", "morpho_l1" -> RdHelper.fingType
//                    "mantra_mis100v2" -> RdHelper.irisType
//                    else -> RdHelper.fingType
//                }
//
//                val pidOptions = RdHelper.makePidXm(deviceId, pidOptData)
//                val action = RdHelper.getAction(deviceId)
//                val packageName = RdHelper.getPackage(deviceId)
//                val inputKey = RdHelper.getInputKey(deviceId)
//
//                _rdCaptureState.value = RdCaptureState.Capture(
//                    action = action,
//                    packageName = packageName,
//                    inputKey = inputKey,
//                    pidOptions = pidOptions
//                )
//
//                _uiState.value = AepsUiState.Idle
//            }
//            catch (e: Exception) {
//                Log.e("AepsViewModel", "cusTwoFA initiation Error", e)
//                _uiState.value = AepsUiState.Error("Capture initialization failed: ${e.message}")
//            }
//        }
//    }
//
//    fun checkAepsStatusAndNavigate(onNavigateToAeps: () -> Unit, onNavigateTo2FA: () -> Unit) {
//        viewModelScope.launch {
//            _uiState.value = AepsUiState.Loading
//            try {
//                val statusResponse = userRepo.checkAepsStatus()
//                Log.d("AepsViewModel", "Initial Status: ${statusResponse.status}")
//
//                when (statusResponse.status) {
//                    1 -> {
//                        // User already authorized for AEPS
//                        _isAepsAuthorized.value = true
//                        _uiState.value = AepsUiState.Idle
//                        onNavigateToAeps()
//                    }
//
//                    17 -> {
//                        val tokenElement = statusResponse.data
//                        if (tokenElement != null) {
//                            try {
//                                _uiState.value = AepsUiState.Loading
//                                val request = TwoFaValidationRequest(token = tokenElement)
//                                Log.d("AepsViewModel", "Token Fixed: $tokenElement")
//                                val validationRes = userRepo.validatetoken(request)
//
//                                when (validationRes.status) {
//                                    18 -> {
//                                        _isAepsAuthorized.value = false
//                                        _uiState.value = AepsUiState.Needs2FA
//                                        onNavigateTo2FA()
//                                    }
//                                    else -> {
//                                        _uiState.value = AepsUiState.Error(validationRes.message ?: "2FA Validation Failed")
//                                    }
//                                }
//                            } catch (e: Exception) {
//                                Log.e("AepsViewModel", "API Error: ${e.message}")
//                                _uiState.value = AepsUiState.Error("Network error: ${e.message}")
//                            }
//                        } else {
//                            _uiState.value = AepsUiState.Error("Token missing in status 17")
//                        }
//                    }
//
//                    else -> {
//                        _isAepsAuthorized.value = false
//                        _uiState.value = AepsUiState.Error(statusResponse.message ?: "Unauthorized (Status: ${statusResponse.status})")
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("AepsViewModel", "Error: ${e.message}")
//                _uiState.value = AepsUiState.Error(e.message ?: "Something went wrong")
//            }
//        }
//    }
//
//    // Fetch Banklist
//    fun refreshBanklist() {
//        viewModelScope.launch {
//            try {
//                val bnklistresponse = userRepo.getBanks()
//                if (bnklistresponse.status == 1) {
//                    _banks.value = bnklistresponse.data ?: emptyList()
//                }
//            } catch (e: Exception) {
//                Log.e("AepsViewModel", "Error fetching banks: ${e.message}")
//            }
//        }
//    }
//
//    fun onAadhaarChange(value: String) {
//        if (value.all { it.isDigit() } && value.length <= 12) {
//            aadhaarNumber.value = value
//        }
//    }
//
//    fun onMobileChange(value: String) {
//        if (value.all { it.isDigit() } && value.length <= 10) {
//            mobileNumber.value = value
//        }
//    }
//
//    fun onAmountChange(value: String) {
//        amount.value = value
//    }
//
//    fun onBankSelected(bank: BankItem) {
//        selectedBank.value = bank
//    }
//
//    fun onTxnTypeSelected(value: String) {
//        selectedTxnType.value = value
//    }
//
//    fun onDeviceSelected(id: String) {
//        selectedDevice.value = id
//    }
//
//    fun setScanState(state: VerificationStep) {
//        _scanState.value = state
//    }
//
//    fun onScanComplete(pidData: String) {
//        handleRdServiceResult(pidData)
//    }
//
//    fun resetRdCaptureState() {
//        _rdCaptureState.value = RdCaptureState.Idle
//    }
//    fun performTransaction() {
//        viewModelScope.launch {
//            _uiState.value = AepsUiState.Loading
//
//            try {
//                val location = locationProvider.getCurrentLocation()
//                if (location == null) {
//                    _uiState.value = AepsUiState.Error("Unable to fetch location. Please enable GPS.")
//                    return@launch
//                }
//
//                val lat = location.first
//                val long = location.second
//
//                val type = when (selectedTxnType.value) {
//                    "Cash Withdrawal" -> "CW"
//                    "Balance Enquiry" -> "BE"
//                    "Mini Statement" -> "MS"
//                    else -> "BE"
//                }
//
//                val bank = selectedBank.value
//                val iinString = "${bank?.iin},${bank?.bankname}"
//
//                val request = AepsModel(
//                    aadhaar = aadhaarNumber.value,
//                    mobile = mobileNumber.value,
//                    amount = if (type == "CW") amount.value else "0",
//                    latitude = lat,
//                    longitude = long,
//                    type = type,
//                    iin = iinString,
//                    biometric = biometricData.value, // Use actual scan data
//                    source = "APP"
//                )
//
//                val response = userRepo.validateuserAeps(request)
//
//                if (response.status == 1) {
//                    _uiState.value = AepsUiState.Success(
//                        message = response.message ?: "Transaction Successful",
//                        txnId = response.data?.rrn ?: ""
//                    )
//                } else if (response.status == 17) {
//                    _uiState.value = AepsUiState.Needs2FA
//                } else {
//                    _uiState.value = AepsUiState.Error(response.message ?: "Transaction Failed")
//                }
//
//            } catch (e: Exception) {
//                Log.e("AepsViewModel", "Transaction Error: ${e.message}")
//                _uiState.value = AepsUiState.Error("Network error: ${e.message}")
//                _scanState.value = VerificationStep.ERROR
//            }
//        }
//    }
//
//    fun resetState() {
//        _uiState.value = AepsUiState.Idle
//    }
//}
//



package com.example.modernui.ui.screens.aeps

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernui.Api.UserRepo
import com.example.modernui.Api.model.BankItem
import com.example.modernui.core.location.UserLocationProvider
import com.example.modernui.ui.components.intentpackage.RdHelper
import com.example.modernui.ui.screens.common.model.TwoFaValidationRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


// ══════════════════════════════════════════════════════════
//  AEPS TYPE  — differentiates the two AEPS services
//
//  AEPS1  → Standard AEPS (Cash Withdrawal, Balance Enquiry,
//            Mini Statement) — endpoint: validateuserAeps
//
//  AEPS2  → AEPS2 / Aadhaar Pay variant with different auth
//            flow and endpoint: validateuserAeps2
// ══════════════════════════════════════════════════════════

enum class AepsType {
    AEPS1,  // Standard AEPS
    AEPS2   // AEPS2 / alternate endpoint
}


// ══════════════════════════════════════════════════════════
//  SHARED UI STATES
// ══════════════════════════════════════════════════════════

sealed class AepsUiState {
    object Idle     : AepsUiState()
    object Loading  : AepsUiState()
    data class Success(val message: String, val txnId: String) : AepsUiState()
    data class Error(val message: String)  : AepsUiState()
    object Needs2FA : AepsUiState()        // Status 17 — needs 2FA before transaction
}

sealed class RdCaptureState {
    object Idle : RdCaptureState()
    data class Capture(
        val action:      String,
        val packageName: String,
        val inputKey:    String,
        val pidOptions:  String
    ) : RdCaptureState()
}

enum class VerificationStep { IDLE, SCANNING, SUCCESS, ERROR }


// ══════════════════════════════════════════════════════════
//  SHARED BASE VIEWMODEL
//  Both AEPS1 and AEPS2 extend this — common logic lives here
// ══════════════════════════════════════════════════════════

abstract class BaseAepsViewModel(
    protected val userRepo:         UserRepo,
    protected val locationProvider: UserLocationProvider
) : ViewModel() {

    companion object {
        const val TAG = "AepsViewModel"
    }

    // ── Common UI state ───────────────────────

    protected val _uiState = MutableStateFlow<AepsUiState>(AepsUiState.Idle)
    val uiState: StateFlow<AepsUiState> = _uiState.asStateFlow()

    protected val _isAuthorized = MutableStateFlow(false)
    val isAuthorized: StateFlow<Boolean> = _isAuthorized.asStateFlow()

    protected val _scanState = MutableStateFlow(VerificationStep.IDLE)
    val scanState: StateFlow<VerificationStep> = _scanState.asStateFlow()

    protected val _rdCaptureState = MutableStateFlow<RdCaptureState>(RdCaptureState.Idle)
    val rdCaptureState: StateFlow<RdCaptureState> = _rdCaptureState.asStateFlow()

    // ── Common form state ─────────────────────

    val aadhaarNumber  = MutableStateFlow("")
    val mobileNumber   = MutableStateFlow("")
    val amount         = MutableStateFlow("")
    val selectedBank   = MutableStateFlow<BankItem?>(null)
    val selectedTxnType = MutableStateFlow("")
    val selectedDevice  = MutableStateFlow("face_scan")
    val biometricData   = MutableStateFlow("")

    private val _banks = MutableStateFlow<List<BankItem>>(emptyList())
    val banks: StateFlow<List<BankItem>> = _banks.asStateFlow()

    // ── Common form handlers ──────────────────

    fun onAadhaarChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 12) aadhaarNumber.value = value
    }
    fun onMobileChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 10) mobileNumber.value = value
    }
    fun onAmountChange(value: String)  { amount.value = value }
    fun onBankSelected(bank: BankItem) { selectedBank.value = bank }
    fun onTxnTypeSelected(value: String) { selectedTxnType.value = value }
    fun onDeviceSelected(id: String)     { selectedDevice.value = id }
    fun setScanState(state: VerificationStep) { _scanState.value = state }
    fun resetRdCaptureState() { _rdCaptureState.value = RdCaptureState.Idle }
    fun resetState()          { _uiState.value = AepsUiState.Idle }

    // ── Bank list ─────────────────────────────

    fun refreshBanklist() {
        viewModelScope.launch {
            try {
                val res = userRepo.getBanks()
                if (res.status == 1) _banks.value = res.data ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching banks: ${e.message}")
            }
        }
    }

    // ── RD service result handler (shared) ────

    fun handleRdServiceResult(responseData: String) {
        Log.d(TAG, "RD Response received (length=${responseData.length})")

        val errCode = Regex("errCode\\s*=\\s*[\"']([^\"']*)[\"']", RegexOption.IGNORE_CASE)
            .find(responseData)?.groupValues?.get(1)

        if (errCode == "0") {
            biometricData.value = responseData
            _scanState.value    = VerificationStep.SUCCESS
            performTransaction()
        } else {
            val errInfo = Regex("errInfo\\s*=\\s*[\"']([^\"']*)[\"']", RegexOption.IGNORE_CASE)
                .find(responseData)?.groupValues?.get(1) ?: "Verification Failed"
            Log.e(TAG, "RD error: $errInfo (Code=$errCode)")
            _uiState.value   = AepsUiState.Error("$errInfo (Code: $errCode)")
            _scanState.value = VerificationStep.ERROR
        }
    }

    fun onScanComplete(pidData: String) = handleRdServiceResult(pidData)

    // ── RD capture intent builder (shared) ────

    fun cusTwoFA() {
        viewModelScope.launch {
            _uiState.value = AepsUiState.Loading
            try {
                val deviceId = mapDeviceId(selectedDevice.value)
                val pidType  = RdHelper.getPidType(deviceId)
                val pidXml   = RdHelper.makePidXm(deviceId, pidType)

                _rdCaptureState.value = RdCaptureState.Capture(
                    action      = RdHelper.getAction(deviceId),
                    packageName = RdHelper.getPackage(deviceId),
                    inputKey    = RdHelper.getInputKey(deviceId),
                    pidOptions  = pidXml
                )
                _uiState.value = AepsUiState.Idle
                Log.d(TAG, "RD capture initiated | device=$deviceId")
            } catch (e: Exception) {
                Log.e(TAG, "cusTwoFA error: ${e.message}", e)
                _uiState.value = AepsUiState.Error("Capture init failed: ${e.message}")
            }
        }
    }

    // ── Status check + 2FA navigation ─────────

    fun checkStatusAndNavigate(
        onNavigateToForm: () -> Unit,
        onNavigateTo2FA:  () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AepsUiState.Loading
            try {
                val statusRes = userRepo.checkAepsStatus()
                Log.d(TAG, "Status: ${statusRes.status}")

                when (statusRes.status) {
                    1 -> {
                        _isAuthorized.value = true
                        _uiState.value      = AepsUiState.Idle
                        onNavigateToForm()
                    }
                    17 -> {
                        val token = statusRes.data
                        if (token != null) {
                            val validRes = userRepo.validatetoken(TwoFaValidationRequest(token = token))
                            when (validRes.status) {
                                18 -> {
                                    _isAuthorized.value = false
                                    _uiState.value      = AepsUiState.Needs2FA
                                    onNavigateTo2FA()
                                }
                                else -> _uiState.value = AepsUiState.Error(
                                    validRes.message ?: "2FA Validation Failed"
                                )
                            }
                        } else {
                            _uiState.value = AepsUiState.Error("Token missing in status 17")
                        }
                    }
                    else -> {
                        _isAuthorized.value = false
                        _uiState.value = AepsUiState.Error(
                            statusRes.message ?: "Unauthorized (Status: ${statusRes.status})"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Status check error: ${e.message}")
                _uiState.value = AepsUiState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    // ── Abstract — each AEPS type implements its own transaction ──

    abstract fun performTransaction()

    // ── Helpers ───────────────────────────────

    protected fun mapDeviceId(selectedDevice: String): String = when (selectedDevice) {
        "face_scan"       -> "face"
        "mantra_mfs110"   -> "mfs110"
        "mantra_iris"     -> "mantra_mis100v2"
        "morpho_l1"       -> "morpho_l1"
        else              -> selectedDevice
    }

    protected fun mapTxnType(txnType: String): String = when (txnType) {
        "Cash Withdrawal" -> "CW"
        "Balance Enquiry" -> "BE"
        "Mini Statement"  -> "MS"
        else              -> "BE"
    }
}


// ══════════════════════════════════════════════════════════
//  AEPS 1 VIEWMODEL
//  Endpoint: userRepo.validateuserAeps(request)
//  Supports: Cash Withdrawal, Balance Enquiry, Mini Statement
// ══════════════════════════════════════════════════════════

@HiltViewModel
class AepsViewModel @Inject constructor(
    userRepo:         UserRepo,
    locationProvider: UserLocationProvider
) : BaseAepsViewModel(userRepo, locationProvider) {

    val aepsType = AepsType.AEPS1

    override fun performTransaction() {
        viewModelScope.launch {
            _uiState.value = AepsUiState.Loading

            try {
                val location = locationProvider.getCurrentLocation()
                if (location == null) {
                    _uiState.value = AepsUiState.Error("Unable to fetch location. Please enable GPS.")
                    return@launch
                }

                val type    = mapTxnType(selectedTxnType.value)
                val bank    = selectedBank.value
                val iinStr  = "${bank?.iin},${bank?.bankname}"

                val request = AepsModel(
                    aadhaar   = aadhaarNumber.value,
                    mobile    = mobileNumber.value,
                    amount    = if (type == "CW") amount.value else "0",
                    latitude  = location.first,
                    longitude = location.second,
                    type      = type,
                    iin       = iinStr,
                    biometric = biometricData.value,
                    source    = "APP"
                )

                Log.d(TAG, "AEPS1 Transaction | type=$type | bank=${bank?.bankname} | iin=${bank?.iin}")
                val response = userRepo.validateuserAeps(request)

                when (response.status) {
                    1  -> _uiState.value = AepsUiState.Success(
                        message = response.message ?: "Transaction Successful",
                        txnId   = response.data?.rrn ?: ""
                    )
                    17 -> _uiState.value = AepsUiState.Needs2FA
                    else -> _uiState.value = AepsUiState.Error(
                        response.message ?: "Transaction Failed"
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "AEPS1 Transaction error: ${e.message}", e)
                _uiState.value   = AepsUiState.Error("Network error: ${e.message}")
                _scanState.value = VerificationStep.ERROR
            }
        }
    }
}


// ══════════════════════════════════════════════════════════
//  AEPS 2 VIEWMODEL
//  Endpoint: userRepo.validateuserAeps2(request)
//
//  Key differences from AEPS1:
//  — Different API endpoint
//  — May support additional txn types
//  — Has its own success/error response structure
//  — Identified in UI as "AEPS2"
// ══════════════════════════════════════════════════════════

@HiltViewModel
class Aeps2ViewModel @Inject constructor(
    userRepo:         UserRepo,
    locationProvider: UserLocationProvider
) : BaseAepsViewModel(userRepo, locationProvider) {

    val aepsType = AepsType.AEPS2

    override fun performTransaction() {
        viewModelScope.launch {
            _uiState.value = AepsUiState.Loading

            try {
                val location = locationProvider.getCurrentLocation()
                if (location == null) {
                    _uiState.value = AepsUiState.Error("Unable to fetch location. Please enable GPS.")
                    return@launch
                }

                val type    = mapTxnType(selectedTxnType.value)
                val bank    = selectedBank.value
                val iinStr  = "${bank?.iin},${bank?.bankname}"

                val request = AepsModel(
                    aadhaar   = aadhaarNumber.value,
                    mobile    = mobileNumber.value,
                    amount    = if (type == "CW") amount.value else "0",
                    latitude  = location.first,
                    longitude = location.second,
                    type      = type,
                    iin       = iinStr,
                    biometric = biometricData.value,
                    source    = "APP"
                    // AEPS2-specific fields can be added here
                )

                Log.d(TAG, "AEPS2 Transaction | type=$type | bank=${bank?.bankname}")

                // ── AEPS2 uses its own endpoint ────────────
                val response = userRepo.validateuserAeps2(request)

                when (response.status) {
                    1  -> _uiState.value = AepsUiState.Success(
                        message = response.message ?: "AEPS2 Transaction Successful",
                        txnId   = response.data?.rrn ?: ""
                    )
                    17 -> _uiState.value = AepsUiState.Needs2FA
                    else -> _uiState.value = AepsUiState.Error(
                        response.message ?: "AEPS2 Transaction Failed"
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "AEPS2 Transaction error: ${e.message}", e)
                _uiState.value   = AepsUiState.Error("Network error: ${e.message}")
                _scanState.value = VerificationStep.ERROR
            }
        }
    }
}

