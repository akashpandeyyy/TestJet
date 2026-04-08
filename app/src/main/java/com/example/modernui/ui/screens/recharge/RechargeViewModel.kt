package com.example.modernui.ui.screens.recharge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernui.Api.UserRepo
import com.example.modernui.Api.model.UserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RechargeUiState {
    object Idle : RechargeUiState()
    object Loading : RechargeUiState()
    data class Success(val response: UserResponse) : RechargeUiState()
    data class Error(val message: String) : RechargeUiState()
}

@HiltViewModel
class RechargeViewModel @Inject constructor(
    private val userRepo: UserRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow<RechargeUiState>(RechargeUiState.Idle)
    val uiState: StateFlow<RechargeUiState> = _uiState.asStateFlow()

    // Form State
    var subscriberNumber = MutableStateFlow("")
    var selectedOperator = MutableStateFlow("")
    var selectedState = MutableStateFlow("")
    var amount = MutableStateFlow("")
    var selectedTab = MutableStateFlow(0) // 0 for Mobile, 1 for DTH

    fun onSubscriberNumberChange(value: String) {
        if (value.all { it.isDigit() }) {
            subscriberNumber.value = value
        }
    }

    fun onOperatorChange(value: String) {
        selectedOperator.value = value
    }

    fun onStateChange(value: String) {
        selectedState.value = value
    }

    fun onAmountChange(value: String) {
        amount.value = value
    }

    fun onTabChange(index: Int) {
        selectedTab.value = index
        // Reset fields on tab change
        selectedOperator.value = ""
        selectedState.value = ""
        amount.value = ""
        subscriberNumber.value = ""
    }

    fun performRecharge() {
        viewModelScope.launch {
            _uiState.value = RechargeUiState.Loading
            try {
                val request = mapOf(
                    "number" to subscriberNumber.value,
                    "operator" to selectedOperator.value,
                    "state" to selectedState.value,
                    "amount" to amount.value,
                    "type" to if (selectedTab.value == 0) "MOBILE" else "DTH"
                )
                val response = userRepo.doRecharge(request)
                if (response.status == 1) {
                    _uiState.value = RechargeUiState.Success(response)
                } else {
                    _uiState.value = RechargeUiState.Error(response.errorMessage ?: "Recharge Failed")
                }
            } catch (e: Exception) {
                _uiState.value = RechargeUiState.Error(e.message ?: "Network Error")
            }
        }
    }

    fun resetState() {
        _uiState.value = RechargeUiState.Idle
    }
}
