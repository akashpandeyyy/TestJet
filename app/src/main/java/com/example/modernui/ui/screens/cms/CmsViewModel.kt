package com.example.modernui.ui.screens.cms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernui.Api.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CmsViewModel @Inject constructor(
    private val userRepo: UserRepo
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _cmsData = MutableStateFlow<com.example.modernui.Api.model.Cmsresponce?>(null)
    val cmsData: StateFlow<com.example.modernui.Api.model.Cmsresponce?> = _cmsData.asStateFlow()

    private val _insuranceData = MutableStateFlow<com.example.modernui.Api.model.InsuranceResponse?>(null)
    val insuranceData: StateFlow<com.example.modernui.Api.model.InsuranceResponse?> = _insuranceData.asStateFlow()

    fun resetError() {
        _errorMessage.value = null
    }

    fun fetchCmsData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = userRepo.cmsscreen()
                _cmsData.value = response
                if (response.status != 1) {
                    _errorMessage.value = response.message ?: "Failed to fetch CMS link"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to fetch CMS data"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchInsuranceLead() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = userRepo.insuranceLead()
                _insuranceData.value = response
                if (response.status != 1) {
                    _errorMessage.value = response.message ?: "Failed to generate insurance lead"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to fetch insurance lead"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
