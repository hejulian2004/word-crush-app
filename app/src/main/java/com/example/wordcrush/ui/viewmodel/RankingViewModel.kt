package com.example.wordcrush.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordcrush.constants.AppConstants
import com.example.wordcrush.constants.AppStrings
import com.example.wordcrush.data.model.RankingItem
import com.example.wordcrush.domain.usecase.GetRankingUseCase
import com.example.wordcrush.ui.architecture.UdfStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

sealed interface RankingAction {
    data class Load(
        val gameType: Int,
        val limit: Int = AppConstants.Ranking.DEFAULT_LIMIT
    ) : RankingAction
    data object Retry : RankingAction
}

sealed interface RankingEffect {
    data class ShowMessage(val message: String) : RankingEffect
}

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val getRankingUseCase: GetRankingUseCase
) : ViewModel() {
    private var lastGameType: Int = 0
    private var lastLimit: Int = AppConstants.Ranking.DEFAULT_LIMIT

    private val store = UdfStore<RankingUiState, RankingEffect>(RankingUiState())
    val uiState = store.uiState
    val effect = store.effect

    private fun updateState(transform: (RankingUiState) -> RankingUiState) = store.updateState(transform)
    private fun emitEffect(effect: RankingEffect) = store.emitEffect(effect)
    private fun launchAction(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    fun onAction(action: RankingAction) {
        when (action) {
            is RankingAction.Load -> {
                lastGameType = action.gameType
                lastLimit = action.limit
                load(action.gameType, action.limit)
            }
            RankingAction.Retry -> load(lastGameType, lastLimit)
        }
    }

    private fun load(gameType: Int, limit: Int) {
        launchAction {
            val cachedRankings = getRankingUseCase.cached(gameType, limit)
            updateState {
                it.copy(
                    isLoading = cachedRankings.isEmpty(),
                    rankings = cachedRankings,
                    error = null
                )
            }
            getRankingUseCase(gameType, limit)
                .onSuccess { rankings ->
                    updateState { it.copy(isLoading = false, rankings = rankings, error = null) }
                }
                .onFailure { error ->
                    val message = error.message ?: AppStrings.Errors.LOAD_RANKING_FAILED
                    updateState {
                        it.copy(
                            isLoading = false,
                            rankings = cachedRankings,
                            error = message
                        )
                    }
                    emitEffect(RankingEffect.ShowMessage(message))
                }
        }
    }

    override fun onCleared() {
        store.close()
        super.onCleared()
    }
}

data class RankingUiState(
    val isLoading: Boolean = false,
    val rankings: List<RankingItem> = emptyList(),
    val error: String? = null
)
