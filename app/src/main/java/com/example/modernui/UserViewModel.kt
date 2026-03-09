package com.example.modernui

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernui.Api.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val userResponse: UserResponse) : UiState()
    data class Error(val message: String) : UiState()
}

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepo: UserRepo
) : ViewModel() {

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state

    fun performLogin(context: Context, userIn: String, passIn: String) {
        val deviceId = getDeviceId(context)
        val authHeader = "Basic YOUR_AUTH_TOKEN"

        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val response = userRepo.userLogin(authHeader, deviceId, LoginRequest(userIn, passIn))
                if (response.status == 1) {
                    _state.value = UiState.Success(response) // ✅ stays alive in memory
                } else {
                    _state.value = UiState.Error(response.errorMessage ?: "Invalid Login")
                }
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Network Error")
            }
        }
    }

    private fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"
    }
}