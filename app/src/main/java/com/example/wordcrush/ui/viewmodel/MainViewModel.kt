package com.example.wordcrush.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordcrush.data.local.PreferenceManager
import com.example.wordcrush.data.repository.AccountRepository
import com.example.wordcrush.utils.AppStateManager
import com.example.wordcrush.utils.AvatarUrlFactory
import com.example.wordcrush.utils.LogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val preferenceManager: PreferenceManager,
    private val appStateManager: AppStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<SessionNavigationEvent?>(null)
    val navigationEvent: StateFlow<SessionNavigationEvent?> = _navigationEvent.asStateFlow()

    fun validateTokenAndInit() {
        viewModelScope.launch {
            _uiState.value = MainUiState(isLoading = true, hasCheckedSession = false)
            try {
                val token = preferenceManager.tokenFlow.firstOrNull()
                val username = preferenceManager.usernameFlow.firstOrNull().orEmpty()
                val uid = preferenceManager.uidFlow.firstOrNull().orEmpty()
                val avatarUrl = preferenceManager.avatarUrlFlow.firstOrNull().orEmpty()

                if (token.isNullOrBlank()) {
                    _navigationEvent.value = SessionNavigationEvent.NavigateToLogin
                    _uiState.value = MainUiState(hasCheckedSession = true)
                    return@launch
                }

                accountRepository.checkToken(token).fold(
                    onSuccess = {
                        appStateManager.setUserInfo(username, token, uid)
                        if (avatarUrl.isNotBlank()) {
                            appStateManager.setAvatarUrl(avatarUrl)
                        } else if (username.isNotBlank()) {
                            appStateManager.setAvatarUrl(AvatarUrlFactory(appStateManager.domain).create(username))
                        }
                        _uiState.value = MainUiState(
                            isLoggedIn = true,
                            hasCheckedSession = true
                        )
                    },
                    onFailure = { error ->
                        LogUtils.e("Token validation failed", error)
                        accountRepository.logout()
                        appStateManager.clearUserInfo()
                        _navigationEvent.value = SessionNavigationEvent.NavigateToLogin
                        _uiState.value = MainUiState(
                            error = error.message,
                            hasCheckedSession = true
                        )
                    }
                )
            } catch (error: Exception) {
                LogUtils.e("Main initialization failed", error)
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
