package com.example.modernui.ui.screens.aeps

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernui.Api.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AepsUiState {
    object Idle : AepsUiState()
    object Loading : AepsUiState()
    data class Success(val message: String, val txnId: String) : AepsUiState()
    data class Error(val message: String) : AepsUiState()
    object Needs2FA : AepsUiState() // Status is 17
}

@HiltViewModel
class AepsViewModel @Inject constructor(
    private val userRepo: UserRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow<AepsUiState>(AepsUiState.Idle)
    val uiState: StateFlow<AepsUiState> = _uiState.asStateFlow()

    private val _isAepsAuthorized = MutableStateFlow(false)
    val isAepsAuthorized: StateFlow<Boolean> = _isAepsAuthorized.asStateFlow()

    // Form State
    var aadhaarNumber = MutableStateFlow("")
    var mobileNumber = MutableStateFlow("")
    var amount = MutableStateFlow("")
    var selectedBank = MutableStateFlow("")
    var selectedTxnType = MutableStateFlow("")
    var selectedDevice = MutableStateFlow("face_scan")

    fun checkAepsStatusAndNavigate(onNavigateToAeps: () -> Unit, onNavigateTo2FA: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AepsUiState.Loading
            try {
                val response = userRepo.checkAepsStatus()
                Log.d("AepsViewModel", "Response: $response")
                when (response.status) {
                    1 -> {
                        _isAepsAuthorized.value = true
                        _uiState.value = AepsUiState.Idle
                        onNavigateToAeps()
                    }
                    17 -> {
                        _isAepsAuthorized.value = false
                        _uiState.value = AepsUiState.Needs2FA
                        onNavigateTo2FA()
                    }
                    else -> {
                        _isAepsAuthorized.value = false
                        _uiState.value = AepsUiState.Error(response.message ?: "Unauthorized access (Status: ${response.status})")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AepsUiState.Error(e.message ?: "Status Check Failed")
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

    fun onBankSelected(value: String) {
        selectedBank.value = value
    }

    fun onTxnTypeSelected(value: String) {
        selectedTxnType.value = value
    }

    fun onDeviceSelected(id: String) {
        selectedDevice.value = id
    }

    fun performTransaction() {
        // ... existing performTransaction logic
    }

    fun resetState() {
        _uiState.value = AepsUiState.Idle
    }
}
