package com.example.modernui.ui.screens.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernui.Api.*
import com.example.modernui.Api.model.LoginRequest
import com.example.modernui.Api.model.UserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val userResponse: UserResponse) : UiState()
    data class OtpRequired(val userResponse: UserResponse) : UiState()
    data class Error(val message: String) : UiState()
}

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepo: UserRepo,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state

    private var lastUsername: String? = null
    private var lastPassword: String? = null

    fun performLogin(userIn: String, passIn: String) {
        lastUsername = userIn
        lastPassword = passIn
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val response = userRepo.userLogin(LoginRequest(userIn, passIn))
                when (response.status) {
                    1 -> {
                        saveToken(response.data?.token)
                        _state.value = UiState.Success(response)
                    }
                    12 -> {
                        // Status 12 indicates OTP is required for login
                        _state.value = UiState.OtpRequired(response)
                    }
                    else -> {
                        _state.value = UiState.Error(response.errorMessage ?: "Invalid Login")
                    }
                }
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Network Error")
            }
        }
    }

    fun validateOtp(otp: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val request = otprequest(
                    otp = otp,
                    password = lastPassword,
                    username = lastUsername,
                    source = "APP"
                )
                val response = userRepo.validateuserotp(request)
                if (response.status == 1) {
                    saveToken(response.data?.token)
                    _state.value = UiState.Success(response)
                } else {
                    _state.value = UiState.Error(response.errorMessage ?: "Invalid OTP")
                }
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "OTP Validation Failed")
            }
        }
    }

    fun validateSession(onValid: () -> Unit, onInvalid: () -> Unit) {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", null)
        val password = sharedPref.getString("password", null)

        if (username != null && password != null) {
            viewModelScope.launch {
                try {
                    val response = userRepo.validateuserLogin(LoginRequest(username, password))
                    if (response.status == 1) {
                        onValid()
                    } else {
                        onInvalid()
                    }
                } catch (e: Exception) {
                    onInvalid()
                }
            }
        } else {
            onInvalid()
        }
    }

    private fun saveToken(token: String?) {
        if (token != null) {
            val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            sharedPref.edit()
                .putString("auth_token", token)
                .putString("username", lastUsername)
                .putString("password", lastPassword)
                .apply()
        }
    }
    
    fun setSuccess(response: UserResponse) {
        saveToken(response.data?.token)
        _state.value = UiState.Success(response)
    }

    fun resetState() {
        _state.value = UiState.Idle
    }
}
