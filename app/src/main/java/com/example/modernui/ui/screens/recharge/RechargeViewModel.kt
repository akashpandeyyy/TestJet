package com.example.modernui.ui.screens.recharge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernui.Api.UserRepo
import com.example.modernui.Api.model.UserResponse
import com.example.modernui.ui.screens.aeps.AepsUiState
import com.example.modernui.ui.screens.recharge.fetchmodel.Plan
import com.example.modernui.ui.screens.recharge.rechargemodel.RechargeRequest
import com.example.modernui.ui.screens.recharge.rechargemodel.RechargeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RechargeUiState {
    object Idle : RechargeUiState()
    object Loading : RechargeUiState()
    data class Success(val response: RechargeResponse) : RechargeUiState()
    data class Error(val message: String) : RechargeUiState()
}

@HiltViewModel
class RechargeViewModel @Inject constructor(
    private val userRepo: UserRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow<RechargeUiState>(RechargeUiState.Idle)
    val uiState: StateFlow<RechargeUiState> = _uiState.asStateFlow()
    private val _plans = MutableStateFlow<List<Plan>>(emptyList())
    val plans: StateFlow<List<Plan>> = _plans

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

   // (amt : String,operator : String, mob: String)
   fun performRechargemob() {
        viewModelScope.launch {
            _uiState.value = RechargeUiState.Loading
            try {
//                if(selectedOperator.value=="Reliance Jio"){
//                    val iincode="RJP"
//
//                }else if (selectedOperator.value=="Airtel"){
//                    val iincode="ATL"
//                }
//                else if (selectedOperator.value=="VI"){
//                    val iincode="VI"
//                }
//                else if (selectedOperator.value=="BSNL"){
//                    val iincode="BSNL"
//                }
//                else if (selectedOperator.value=="BSNL SPECIAL"){
//                    val iincode="BSNLS"
//                }

                val iincode = when (selectedOperator.value) {
                    "Reliance Jio" -> "RJP"
                    "Airtel" -> "ATL"
                    "VI" -> "VI"
                    "BSNL" -> "BSNL"
                    "BSNL SPECIAL" -> "BSNLS"
                    else -> throw IllegalArgumentException("Unknown operator")
                }

                val request = RechargeRequest(
                    mobile =subscriberNumber.value,
                    amount =amount.value,
                    incode = iincode
                )
                val response = userRepo.doRecharge(request)
                try {
                    if (response.status == 1) {
                        _uiState.value = RechargeUiState.Success(response)
                    } else if (response.status == 0) {
                        _uiState.value = RechargeUiState.Error(
                            message = response.message ?: "Transaction Failed",
                        )
                    } else if (response.status == 2) {

                    } else {
                        _uiState.value = RechargeUiState.Error(
                            message = response.message ?: "Transaction Failed",
                        )
                    }

                }catch (e: Exception) {
                    _uiState.value = RechargeUiState.Error(e.message ?: "Network Error")
                }

            } catch (e: Exception) {
                _uiState.value = RechargeUiState.Error(e.message ?: "Network Error")
            }
        }
    }

    fun performRechargedth() {
        viewModelScope.launch {
            _uiState.value = RechargeUiState.Loading
            try {

            } catch (e: Exception) {
                _uiState.value = RechargeUiState.Error(e.message ?: "Network Error")
            }
        }
    }

    fun onMobileNumberComplete(value: String) {
        viewModelScope.launch {
            val response = userRepo.fetchplan(value)
            if (response.status == 1) {
                _plans.value = response.data.plans
            }
        }
    }



    fun resetState() {
        _uiState.value = RechargeUiState.Idle
    }
}
