package com.example.wordcrush.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordcrush.data.model.GameRecordItem
import com.example.wordcrush.data.repository.GameRecordRepository
import com.example.wordcrush.ui.model.MatchGameEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class GameRecordViewModel @Inject constructor(
    private val gameRecordRepository: GameRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameRecordUiState())
    val uiState: StateFlow<GameRecordUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<MatchGameEvent>()
    val event: SharedFlow<MatchGameEvent> = _event.asSharedFlow()

    fun loadGameRecords() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            runCatching { gameRecordRepository.getLocalRecords() }
                .onSuccess { records ->
                    _uiState.value = GameRecordUiState(records = records)
                }
                .onFailure { error ->
                    _uiState.value = GameRecordUiState(
                        error = error.message ?: "Unable to load game records."
                    )
                }
        }
    }

    fun deleteRecord(record: GameRecordItem) {
        viewModelScope.launch {
            gameRecordRepository.deleteRecord(record)
                .onSuccess {
                    _event.emit(MatchGameEvent.Message("Record deleted."))
                    loadGameRecords()
                }
                .onFailure { error ->
                    _event.emit(MatchGameEvent.Message(error.message ?: "Unable to delete record."))
                }
        }
    }
}

data class GameRecordUiState(
    val isLoading: Boolean = false,
    val records: List<GameRecordItem> = emptyList(),
    val error: String? = null
)
