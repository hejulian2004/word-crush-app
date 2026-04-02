package com.example.wordcrush.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordcrush.data.local.PreferenceManager
import com.example.wordcrush.data.repository.AccountRepository
import com.example.wordcrush.utils.LogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _loginResult = MutableStateFlow<LoginResult?>(null)
    val loginResult: StateFlow<LoginResult?> = _loginResult.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(error = "Username and password are required.")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            accountRepository.login(username, password).fold(
                onSuccess = { message ->
                    LogUtils.d("Login success: $message")
                    _uiState.value = LoginUiState()
                    _loginResult.value = LoginResult.Success(message)
                },
                onFailure = { error ->
                    val message = error.message ?: "Login failed."
                    LogUtils.e("Login failed: $message")
                    _uiState.value = LoginUiState(error = message)
                    _loginResult.value = LoginResult.Error(message)
                }
            )
        }
    }

    fun checkLocalLoginState() {
        viewModelScope.launch {
            if (preferenceManager.isLoggedIn()) {
                _loginResult.value = LoginResult.AlreadyLoggedIn
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetLoginResult() {
        _loginResult.value = null
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class LoginResult {
    data class Success(val message: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
    data object AlreadyLoggedIn : LoginResult()
}
