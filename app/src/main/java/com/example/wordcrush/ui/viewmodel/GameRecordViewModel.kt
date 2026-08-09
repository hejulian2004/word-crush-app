package com.example.wordcrush.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordcrush.constants.AppStrings
import com.example.wordcrush.data.model.GameRecordItem
import com.example.wordcrush.domain.usecase.DeleteGameRecordUseCase
import com.example.wordcrush.domain.usecase.GetGameRecordsUseCase
import com.example.wordcrush.ui.architecture.UdfStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

sealed interface GameRecordAction {
    data object Load : GameRecordAction
    data class ToggleExpanded(val recordId: Int) : GameRecordAction
    data class RequestDelete(val record: GameRecordItem) : GameRecordAction
    data object ConfirmDelete : GameRecordAction
    data object DismissDelete : GameRecordAction
}

sealed interface GameRecordEffect {
    data class ShowMessage(val message: String) : GameRecordEffect
}

@HiltViewModel
class GameRecordViewModel @Inject constructor(
    private val getGameRecordsUseCase: GetGameRecordsUseCase,
    private val deleteGameRecordUseCase: DeleteGameRecordUseCase
) : ViewModel() {
    private val store = UdfStore<GameRecordUiState, GameRecordEffect>(GameRecordUiState())
    val uiState = store.uiState
    val effect = store.effect

    private val currentState: GameRecordUiState
        get() = store.currentState

    private fun updateState(transform: (GameRecordUiState) -> GameRecordUiState) = store.updateState(transform)
    private fun emitEffect(effect: GameRecordEffect) = store.emitEffect(effect)
    private fun launchAction(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    fun onAction(action: GameRecordAction) {
        when (action) {
            GameRecordAction.Load -> load()
            is GameRecordAction.ToggleExpanded -> updateState {
                it.copy(
                    expandedRecordId = if (it.expandedRecordId == action.recordId) {
                        null
                    } else {
                        action.recordId
                    }
                )
            }
            is GameRecordAction.RequestDelete -> updateState {
                it.copy(pendingDelete = action.record)
            }
            GameRecordAction.ConfirmDelete -> confirmDelete()
            GameRecordAction.DismissDelete -> updateState { it.copy(pendingDelete = null) }
        }
    }

    private fun load() {
        launchAction {
            updateState { it.copy(isLoading = true, error = null) }
            runCatching { getGameRecordsUseCase() }
                .onSuccess { records ->
                    updateState { it.copy(isLoading = false, records = records) }
                }
                .onFailure { error ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: AppStrings.Errors.LOAD_RECORDS_FAILED
                        )
                    }
                    emitEffect(GameRecordEffect.ShowMessage(
                        error.message ?: AppStrings.Errors.LOAD_RECORDS_FAILED
                    ))
                }
        }
    }

    private fun confirmDelete() {
        val record = currentState.pendingDelete ?: return
        launchAction {
            updateState { it.copy(isDeleting = true, pendingDelete = null) }
            deleteGameRecordUseCase(record)
                .onSuccess {
                    updateState { it.copy(isDeleting = false) }
                    emitEffect(GameRecordEffect.ShowMessage(AppStrings.Errors.RECORD_DELETED))
                    load()
                }
                .onFailure { error ->
                    updateState { it.copy(isDeleting = false) }
                    emitEffect(GameRecordEffect.ShowMessage(
                        error.message ?: AppStrings.Errors.DELETE_RECORD_FAILED
                    ))
                }
        }
    }

    override fun onCleared() {
        store.close()
        super.onCleared()
    }
}

data class GameRecordUiState(
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val records: List<GameRecordItem> = emptyList(),
    val error: String? = null,
    val expandedRecordId: Int? = null,
    val pendingDelete: GameRecordItem? = null
)
