package com.example.wordcrush.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordcrush.domain.usecase.LoginUseCase
import com.example.wordcrush.domain.usecase.RestoreSessionUseCase
import com.example.wordcrush.ui.architecture.UdfStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

sealed interface LoginAction {
    data class UsernameChanged(val value: String) : LoginAction
    data class PasswordChanged(val value: String) : LoginAction
    data object Initialize : LoginAction
    data object Submit : LoginAction
    data object ClearError : LoginAction
}

sealed interface LoginEffect {
    data object LoginSucceeded : LoginEffect
    data class ShowMessage(val message: String) : LoginEffect
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val restoreSessionUseCase: RestoreSessionUseCase
) : ViewModel() {
    private val store = UdfStore<LoginUiState, LoginEffect>(LoginUiState())
    val uiState = store.uiState
    val effect = store.effect

    private val currentState: LoginUiState
        get() = store.currentState

    private fun updateState(transform: (LoginUiState) -> LoginUiState) = store.updateState(transform)
    private fun setState(state: LoginUiState) = store.setState(state)
    private fun emitEffect(effect: LoginEffect) = store.emitEffect(effect)
    private fun launchAction(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.UsernameChanged -> updateState {
                it.copy(username = action.value, error = null)
            }
            is LoginAction.PasswordChanged -> updateState {
                it.copy(password = action.value, error = null)
            }
            LoginAction.Initialize -> initialize()
            LoginAction.Submit -> submit()
            LoginAction.ClearError -> updateState { it.copy(error = null) }
        }
    }

    private fun initialize() {
        launchAction {
            if (restoreSessionUseCase()) {
                emitEffect(LoginEffect.LoginSucceeded)
            }
        }
    }

    private fun submit() {
        val state = currentState
        launchAction {
            setState(state.copy(isLoading = true, error = null))
            loginUseCase(state.username, state.password)
                .onSuccess {
                    setState(currentState.copy(isLoading = false, password = ""))
                    emitEffect(LoginEffect.LoginSucceeded)
                }
                .onFailure { error ->
                    val message = error.message ?: "Login failed."
                    setState(currentState.copy(isLoading = false, error = message))
                    emitEffect(LoginEffect.ShowMessage(message))
                }
        }
    }

    override fun onCleared() {
        store.close()
        super.onCleared()
    }
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
