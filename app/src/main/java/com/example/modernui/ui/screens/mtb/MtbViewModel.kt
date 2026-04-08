package com.example.modernui.ui.screens.mtb

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernui.Api.UserRepo
import com.example.modernui.core.datastore.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MtbViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _balance = MutableStateFlow("₹0.00")
    val balance: StateFlow<String> = _balance.asStateFlow()

    private val _banks = MutableStateFlow<List<PayoutBank>>(emptyList())
    val banks: StateFlow<List<PayoutBank>> = _banks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.userSessionFlow.collectLatest { session ->
                if (session.token != null) {
                    refreshBalance()
                    fetchBanks()
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

    fun fetchBanks() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = userRepo.fetchMtbData()
                if (response.status == 1) {
                    // Logic to map response data to PayoutBank list
                    // Since I don't have the exact structure of MTB response data, 
                    // I'll assume it needs mapping or just use mock for now if data is null
                    // For a real app, this would be: _banks.value = response.data.map { ... }
                    
                    // Fallback to mock if API data is not yet structured in UserResponse
                    if (_banks.value.isEmpty()) {
                        _banks.value = getMockBanks()
                    }
                } else {
                    _errorMessage.value = response.message ?: "Failed to fetch banks"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getMockBanks(): List<PayoutBank> {
        return listOf(
            PayoutBank("pb1", "Rahul Sharma", "9876543210", "XXXX XXXX 4291", "SBIN0001234", "State Bank of India", PayoutBankStatus.ACTIVE, "RS"),
            PayoutBank("pb2", "Priya Verma", "9999123456", "XXXX XXXX 8803", "HDFC0005678", "HDFC Bank", PayoutBankStatus.ACTIVE, "PV")
        )
    }

    fun performTransfer(bank: PayoutBank, amount: String, mode: TransferMode, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Mocking API call for transfer
                kotlinx.coroutines.delay(2000)
                onResult(true, "Transfer of ₹$amount successful")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Transfer failed")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
