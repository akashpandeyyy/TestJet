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

    fun fetchCmsData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Currently just a placeholder for potential API call
                // If there's a specific CMS data fetch endpoint, it would go here.
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to fetch CMS data"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
