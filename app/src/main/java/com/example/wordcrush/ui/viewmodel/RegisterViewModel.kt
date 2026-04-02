package com.example.wordcrush.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordcrush.data.repository.AccountRepository
import com.example.wordcrush.utils.LogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _registerResult = MutableStateFlow<RegisterResult?>(null)
    val registerResult: StateFlow<RegisterResult?> = _registerResult.asStateFlow()

    fun register(username: String, password: String, confirmPassword: String) {
        when {
            username.isBlank() -> {
                _uiState.value = RegisterUiState(error = "Username is required.")
                return
            }
            password.isBlank() || confirmPassword.isBlank() -> {
                _uiState.value = RegisterUiState(error = "Password is required.")
                return
            }
            password != confirmPassword -> {
                _uiState.value = RegisterUiState(error = "Passwords do not match.")
                return
            }
            password.length < 6 -> {
                _uiState.value = RegisterUiState(error = "Password must be at least 6 characters.")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            accountRepository.register(username, password).fold(
                onSuccess = { message ->
                    LogUtils.d("Register success: $message")
                    _uiState.value = RegisterUiState()
                    _registerResult.value = RegisterResult.Success(message)
                },
                onFailure = { error ->
                    val message = error.message ?: "Registration failed."
                    LogUtils.e("Register failed: $message")
                    _uiState.value = RegisterUiState(error = message)
                    _registerResult.value = RegisterResult.Error(message)
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetRegisterResult() {
        _registerResult.value = null
    }
}

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class RegisterResult {
    data class Success(val message: String) : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}
