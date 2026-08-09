package com.example.wordcrush.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordcrush.domain.usecase.LogoutUseCase
import com.example.wordcrush.domain.usecase.ObserveSessionUseCase
import com.example.wordcrush.domain.usecase.BootstrapLearningDataUseCase
import com.example.wordcrush.domain.usecase.ValidateSessionUseCase
import com.example.wordcrush.ui.architecture.UdfStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed interface MainAction {
    data object Initialize : MainAction
    data object RefreshSession : MainAction
}

sealed interface MainEffect {
    data class ShowMessage(val message: String) : MainEffect
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val bootstrapLearningDataUseCase: BootstrapLearningDataUseCase,
    private val validateSessionUseCase: ValidateSessionUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val observeSessionUseCase: ObserveSessionUseCase
) : ViewModel() {
    private val store = UdfStore<MainUiState, MainEffect>(MainUiState())
    val uiState = store.uiState
    val effect = store.effect

    private val currentState: MainUiState
        get() = store.currentState

    private fun updateState(transform: (MainUiState) -> MainUiState) = store.updateState(transform)
    private fun setState(state: MainUiState) = store.setState(state)
    private fun emitEffect(effect: MainEffect) = store.emitEffect(effect)
    private fun launchAction(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    init {
        viewModelScope.launch {
            observeSessionUseCase().collectLatest { session ->
                updateState {
                    it.copy(
                        isLoggedIn = session.isLoggedIn,
                        hasCheckedSession = if (it.hasCheckedSession) it.hasCheckedSession else false
                    )
                }
            }
        }
        viewModelScope.launch {
            observeSessionUseCase.expiredMessages().collectLatest { message ->
                updateState { it.copy(isLoggedIn = false, hasCheckedSession = true, isLoading = false) }
                emitEffect(MainEffect.ShowMessage(message))
            }
        }
    }

    fun onAction(action: MainAction) {
        when (action) {
            MainAction.Initialize,
            MainAction.RefreshSession -> refreshSession()
        }
    }

    private fun refreshSession() {
        launchAction {
            setState(currentState.copy(isLoading = true, hasCheckedSession = false, error = null))
            validateSessionUseCase()
                .onSuccess { isLoggedIn ->
                    if (isLoggedIn) {
                        bootstrapLearningDataUseCase()
                            .onFailure { error ->
                                emitEffect(MainEffect.ShowMessage(
                                    error.message ?: "Learning data will use the local cache until sync succeeds."
                                ))
                            }
                    }
                    setState(
                        MainUiState(
                            isLoading = false,
                            isLoggedIn = isLoggedIn,
                            hasCheckedSession = true
                        )
                    )
                }
                .onFailure { error ->
                    logoutUseCase()
                    val message = error.message
                    setState(
                        MainUiState(
                            isLoading = false,
                            isLoggedIn = false,
                            hasCheckedSession = true,
                            error = message
                        )
                    )
                    if (!message.isNullOrBlank()) {
                        emitEffect(MainEffect.ShowMessage(message))
                    }
                }
        }
    }

    override fun onCleared() {
        store.close()
        super.onCleared()
    }
}

data class MainUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val hasCheckedSession: Boolean = false,
    val error: String? = null
)
