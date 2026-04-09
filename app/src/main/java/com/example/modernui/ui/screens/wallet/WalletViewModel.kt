package com.example.modernui.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernui.Api.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Transaction(
    val id: String,
    val title: String,
    val date: String,
    val amount: String,
    val isDebit: Boolean
)

data class WalletState(
    val balance: String = "₹0.00",
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val userRepo: UserRepo
) : ViewModel() {

    private val _state = MutableStateFlow(WalletState())
    val state: StateFlow<WalletState> = _state.asStateFlow()

    init {
        loadWalletData()
    }

    fun loadWalletData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                // Simulate API call for now since UserRepo doesn't have wallet endpoints yet
                // In a real app, you'd call userRepo.getWalletBalance() etc.
                
                _state.value = WalletState(
                    balance = "₹24,500.00",
                    transactions = listOf(
                        Transaction("1", "Grocery Store", "Oct 24, 2023", "-₹45.00", true),
                        Transaction("2", "Salary", "Oct 23, 2023", "+₹50,000.00", false),
                        Transaction("3", "Electric Bill", "Oct 22, 2023", "-₹1,200.00", true),
                        Transaction("4", "Amazon Refund", "Oct 21, 2023", "+₹599.00", false),
                        Transaction("5", "Coffee Shop", "Oct 20, 2023", "-₹180.00", true)
                    ),
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load wallet data"
                )
            }
        }
    }
}