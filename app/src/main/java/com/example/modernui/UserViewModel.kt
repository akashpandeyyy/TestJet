package com.example.modernui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernui.Api.*
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
    data class Error(val message: String) : UiState()

}
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepo: UserRepo,
    @ApplicationContext private val context: Context  //  inject context via Hilt
) : ViewModel() {

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state

    fun performLogin(userIn: String, passIn: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val response = userRepo.userLogin(LoginRequest(userIn,   passIn))
                if (response.status == 1) {
                    val tkn = response.data?.token

                    //  save token with SAME key as interceptor reads
                    context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        .edit()
                        .putString("auth_token", tkn)
                        .apply()

                    _state.value = UiState.Success(response)
                } else {
                    _state.value = UiState.Error(response.errorMessage ?: "Invalid Login")
                }
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Network Error")
            }
        }
    }
}