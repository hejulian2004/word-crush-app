package com.example.wordcrush.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordcrush.domain.usecase.RegisterUseCase
import com.example.wordcrush.ui.architecture.UdfStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

sealed interface RegisterAction {
    data class UsernameChanged(val value: String) : RegisterAction
    data class PasswordChanged(val value: String) : RegisterAction
    data class ConfirmPasswordChanged(val value: String) : RegisterAction
    data object Submit : RegisterAction
    data object ClearError : RegisterAction
}

sealed interface RegisterEffect {
    data class RegistrationSucceeded(val message: String) : RegisterEffect
    data class ShowMessage(val message: String) : RegisterEffect
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {
    private val store = UdfStore<RegisterUiState, RegisterEffect>(RegisterUiState())
    val uiState = store.uiState
    val effect = store.effect

    private val currentState: RegisterUiState
        get() = store.currentState

    private fun updateState(transform: (RegisterUiState) -> RegisterUiState) = store.updateState(transform)
    private fun setState(state: RegisterUiState) = store.setState(state)
    private fun emitEffect(effect: RegisterEffect) = store.emitEffect(effect)
    private fun launchAction(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.UsernameChanged -> updateState {
                it.copy(username = action.value, error = null)
            }
            is RegisterAction.PasswordChanged -> updateState {
                it.copy(password = action.value, error = null)
            }
            is RegisterAction.ConfirmPasswordChanged -> updateState {
                it.copy(confirmPassword = action.value, error = null)
            }
            RegisterAction.Submit -> submit()
            RegisterAction.ClearError -> updateState { it.copy(error = null) }
        }
    }

    private fun submit() {
        val state = currentState
        launchAction {
            setState(state.copy(isLoading = true, error = null))
            registerUseCase(state.username, state.password, state.confirmPassword)
                .onSuccess { message ->
                    setState(RegisterUiState())
                    emitEffect(RegisterEffect.RegistrationSucceeded(message))
                }
                .onFailure { error ->
                    val message = error.message ?: "Registration failed."
                    setState(currentState.copy(isLoading = false, error = message))
                    emitEffect(RegisterEffect.ShowMessage(message))
                }
        }
    }

    override fun onCleared() {
        store.close()
        super.onCleared()
    }
}

data class RegisterUiState(
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
