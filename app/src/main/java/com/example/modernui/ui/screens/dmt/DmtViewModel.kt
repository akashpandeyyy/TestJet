package com.example.modernui.ui.screens.dmt

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernui.Api.UserRepo
import com.example.modernui.core.datastore.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DmtViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _balance = MutableStateFlow("₹0.00")
    val balance: StateFlow<String> = _balance.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _beneficiaries = MutableStateFlow<List<Beneficiary>>(emptyList())
    val beneficiaries: StateFlow<List<Beneficiary>> = _beneficiaries.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.userSessionFlow.collectLatest { session ->
                if (session.token != null) {
                    refreshBalance()
                }
            }
        }
    }

    fun refreshBalance() {
        viewModelScope.launch {
            try {
                val response = userRepo.fetchUserBalance()
                if (response.status == 1) {
                    _balance.value = "₹${response.data?.total ?: 0.0}"
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun checkMobile(mobile: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Mocking mobile check logic
                delay(1200)
                val isKnown = mobile in setOf("91653371777", "9999999999", "8888888888")
                if (isKnown) {
                    _beneficiaries.value = getMockBeneficiaries()
                }
                onResult(isKnown)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to check mobile"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendOtp(mobile: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                delay(1500)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyOtp(otp: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                delay(1500)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun performTransfer(beneficiary: Beneficiary, amount: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                delay(2000)
                onResult(true, "DMT transaction of ₹$amount successful")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Transfer failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getMockBeneficiaries(): List<Beneficiary> {
        return listOf(
            Beneficiary("b1", "Ansh Sharma", "XXXX XXXX 4291", "State Bank of India", "SBIN0001234", "RS"),
            Beneficiary("b2", "Ayush Mishra", "XXXX XXXX 8803", "HDFC Bank", "HDFC0005678", "PV"),
            Beneficiary("b3", "Akhil Dwivedi", "XXXX XXXX 1147", "ICICI Bank", "ICIC0009876", "AG"),
            Beneficiary("b4", "Anurag Dwivedi", "XXXX XXXX 3366", "Punjab National Bank", "PUNB0004321", "SD"),
        )
    }
}
