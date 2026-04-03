package com.example.wordcrush.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordcrush.data.model.RankingItem
import com.example.wordcrush.data.repository.GameRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val gameRecordRepository: GameRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    fun loadRankings(gameType: Int, limit: Int = 50) {
        viewModelScope.launch {
            val cachedRankings = gameRecordRepository.getCachedRanking(gameType, limit)
            _uiState.value = if (cachedRankings.isEmpty()) {
                RankingUiState(isLoading = true)
            } else {
                RankingUiState(rankings = cachedRankings)
            }

            val result = gameRecordRepository.getRanking(gameType, limit)
            _uiState.value = result.fold(
                onSuccess = { rankings -> RankingUiState(rankings = rankings) },
                onFailure = { error ->
                    RankingUiState(
                        rankings = cachedRankings,
                        error = error.message ?: "Unable to load ranking data."
                    )
                }
            )
        }
    }
}

data class RankingUiState(
    val isLoading: Boolean = false,
    val rankings: List<RankingItem> = emptyList(),
    val error: String? = null
)
