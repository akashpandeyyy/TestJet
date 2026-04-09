package com.example.modernui.ui.screens.home

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
class HomeViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _balance = MutableStateFlow("₹0.00")
    val balance: StateFlow<String> = _balance.asStateFlow()

    private val _userName = MutableStateFlow("User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _aepsBalance = MutableStateFlow("₹0.00")
    val aepsBalance: StateFlow<String> = _aepsBalance.asStateFlow()

    private val _walletBalance = MutableStateFlow("₹0.00")
    val walletBalance: StateFlow<String> = _walletBalance.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.userSessionFlow.collectLatest { session ->
                if (session.token != null) {
                    _userName.value = session.name ?: "User"
                    refreshData()
                }
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch specific wallet balances
                val balanceResponse = userRepo.fetchUserBalance()
                if (balanceResponse.status == 1) {
                    val data = balanceResponse.data
                    _aepsBalance.value = "₹${data?.aeps ?: 0.0}"
                    _walletBalance.value = "₹${data?.wallet ?: 0.0}"
                    _balance.value = "₹${data?.total ?: 0.0}"
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
