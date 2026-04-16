package com.example.modernui.ui.screens.mtb

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernui.Api.UserRepo
import com.example.modernui.Api.model.MtbBankData
import com.example.modernui.core.datastore.SessionManager
import com.example.modernui.ui.screens.mtb.model.Beniaddrequest
import com.example.modernui.ui.screens.mtb.model.PayoutRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MtbViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _balance = MutableStateFlow("₹0.00")
    val balance: StateFlow<String> = _balance.asStateFlow()

    private val _banks = MutableStateFlow<List<MtbBankData>?>(emptyList())
    val banks: StateFlow<List<MtbBankData>?> = _banks.asStateFlow()

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
                // Get userId from sessionManager once
                val session = sessionManager.userSessionFlow.first()
                val userId = session.userId
                if (userId != null) {
                    val response = userRepo.fetchMtbData(userId)
                    if (response.status == 1) {
//                        val mappedBanks = response.data?.map { data ->
//                            PayoutBank(
//                                id = data.id?.toString() ?: "",
//                                name = data.name ?: "N/A",
//                                mobile = data.mobile ?: "N/A",
//                                accountNo = data.accountNo ?: "N/A",
//                                ifsc = data.ifscCode ?: "N/A",
//                                bankName = data.bankName ?: "N/A",
//                                status = if (data.status == true) PayoutBankStatus.ACTIVE else PayoutBankStatus.INACTIVE,
//                                initials = (data.name?.take(2) ?: "BK").uppercase(Locale.ROOT)
//                            )
//                        } ?: emptyList()
                        _banks.value = response.data
                    }
else {
                        _errorMessage.value = response.message ?: "Failed to fetch banks"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addBank(name: String, mobile: String, accountNo: String, ifsc: String, bankName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val request = Beniaddrequest(
                    mobile = mobile,
                    account = accountNo,
                    ifsc = ifsc,
                    bankName = bankName,
                    beneName = name
                )
                val response = userRepo.addpayoutbank(request)
                if (response.status == 1) {
                    fetchBanks()
                } else {
                    _errorMessage.value = response.message ?: "Failed to add bank"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun performTransfer(bank: MtbBankData, amount: String, mode: TransferMode, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = PayoutRequest(
                    amount = amount,
                    mode = mode.name,
                    beneId = bank.beneId!!
                )
                val response = userRepo.payout(request)
                if (response.status == 1) {
                    onResult(true, response.message)
                    refreshBalance()
                } else {
                    onResult(false, response.message)
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Transfer failed")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
