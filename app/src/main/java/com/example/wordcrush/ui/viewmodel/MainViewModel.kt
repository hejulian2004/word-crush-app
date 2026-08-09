package com.example.wordcrush.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordcrush.data.repository.AccountRepository
import com.example.wordcrush.data.session.SessionManager
import com.example.wordcrush.utils.LogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<SessionNavigationEvent?>(null)
    val navigationEvent: StateFlow<SessionNavigationEvent?> = _navigationEvent.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.sessionExpiredEvent.collectLatest { message ->
                _navigationEvent.value = SessionNavigationEvent.NavigateToLogin
                _uiState.value = MainUiState(
                    error = message,
                    hasCheckedSession = true
                )
            }
        }
    }

    fun validateTokenAndInit() {
        viewModelScope.launch {
            _uiState.value = MainUiState(isLoading = true, hasCheckedSession = false)
            try {
                sessionManager.restore()
                if (sessionManager.currentToken.isNullOrBlank()) {
                    _navigationEvent.value = SessionNavigationEvent.NavigateToLogin
                    _uiState.value = MainUiState(hasCheckedSession = true)
                    return@launch
                }

                accountRepository.checkToken().fold(
                    onSuccess = {
                        _uiState.value = MainUiState(
                            isLoggedIn = true,
                            hasCheckedSession = true
                        )
                    },
                    onFailure = { error ->
                        LogUtils.e("Token validation failed", error)
                        accountRepository.logout()
                        _navigationEvent.value = SessionNavigationEvent.NavigateToLogin
                        _uiState.value = MainUiState(
                            error = error.message,
                            hasCheckedSession = true
                        )
                    }
                )
            } catch (error: Exception) {
                LogUtils.e("Main initialization failed", error)
                accountRepository.logout()
                _navigationEvent.value = SessionNavigationEvent.NavigateToLogin
                _uiState.value = MainUiState(
                    error = error.message,
                    hasCheckedSession = true
                )
            }
        }
    }

    fun resetNavigationEvent() {
        _navigationEvent.value = null
    }
}

data class MainUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val hasCheckedSession: Boolean = false,
    val error: String? = null
)
