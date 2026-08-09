package com.example.wordcrush.domain.game

import com.example.wordcrush.ui.model.MatchCardFeedback
import com.example.wordcrush.ui.model.MatchCardUiModel
import com.example.wordcrush.constants.AppConstants
import com.example.wordcrush.constants.AppStrings

/** State owned by the pure interaction reducer for one game mode. */
data class MatchGameState(
    val score: Int = 0,
    val hearts: Int = AppConstants.Game.MAX_HEARTS,
    val remainingSeconds: Int = AppConstants.Game.DEFAULT_DURATION_SECONDS,
    val hasStarted: Boolean = false,
    val gameVisible: Boolean = false,
    val isResolvingPair: Boolean = false,
    val cards: List<MatchCardUiModel> = emptyList(),
    val latestMatchedWordId: Int? = null,
    val gameOverMessage: String? = null,
    val gameOverScore: Int? = null
)

sealed interface MatchGameAction {
    data class CardClicked(val index: Int) : MatchGameAction
    data class SetLatestMatchedWord(val wordId: Int?) : MatchGameAction
    data class ResolveCorrectPair(val wordId: Int) : MatchGameAction
    data object ResolveMismatch : MatchGameAction
    data class TimerTick(val remainingSeconds: Int) : MatchGameAction
    data class Finish(val message: String) : MatchGameAction
    data object ClearGameOver : MatchGameAction
}

sealed interface MatchGameCommand {
    data object Persist : MatchGameCommand
    data class CorrectMatch(val wordId: Int) : MatchGameCommand
    data class IncorrectMatch(val wordIds: Set<Int>) : MatchGameCommand
    data object StartNextRound : MatchGameCommand
    data class SaveGame(val message: String, val score: Int) : MatchGameCommand
}

data class MatchGameReduction(
    val state: MatchGameState,
    val commands: List<MatchGameCommand> = emptyList()
)

/**
 * Pure state transition logic for card interaction. It has no Android,
 * coroutine, persistence or network dependency, which makes the matching
 * rules independently testable.
 */
object MatchGameReducer {
    fun reduce(state: MatchGameState, action: MatchGameAction): MatchGameReduction {
        return when (action) {
            is MatchGameAction.CardClicked -> reduceCardClick(state, action.index)
            is MatchGameAction.SetLatestMatchedWord ->
                MatchGameReduction(state.copy(latestMatchedWordId = action.wordId))
            is MatchGameAction.ResolveCorrectPair -> resolveCorrectPair(state, action.wordId)
            MatchGameAction.ResolveMismatch -> resolveMismatch(state)
            is MatchGameAction.TimerTick -> reduceTimerTick(state, action.remainingSeconds)
            is MatchGameAction.Finish -> finish(state, action.message)
            MatchGameAction.ClearGameOver -> MatchGameReduction(
                state.copy(gameOverMessage = null, gameOverScore = null)
            )
        }
    }

    private fun reduceCardClick(state: MatchGameState, index: Int): MatchGameReduction {
        if (state.isResolvingPair) return MatchGameReduction(state)
        val cards = state.cards.toMutableList()
        val current = cards.getOrNull(index) ?: return MatchGameReduction(state)
        if (current.isMatched) return MatchGameReduction(state)

        val selectedIndex = cards.indexOfFirst { it.isSelected && !it.isMatched }
        if (selectedIndex == -1) {
            cards[index] = current.copy(isSelected = true)
            return MatchGameReduction(state.copy(cards = cards), listOf(MatchGameCommand.Persist))
        }

        if (selectedIndex == index) {
            cards[index] = current.copy(isSelected = false)
            return MatchGameReduction(state.copy(cards = cards), listOf(MatchGameCommand.Persist))
        }

        val selected = cards[selectedIndex]
        if (selected.wordId == current.wordId && selected.type != current.type) {
            cards[selectedIndex] = selected.copy(
                isSelected = false,
                isMatched = true,
                feedback = MatchCardFeedback.MATCHED
            )
            cards[index] = current.copy(
                isSelected = false,
                isMatched = true,
                feedback = MatchCardFeedback.MATCHED
            )
            val newScore = state.score + 1
            val newHearts = if (newScore % AppConstants.Game.SCORE_HEART_REWARD_INTERVAL == 0) {
                minOf(AppConstants.Game.MAX_HEARTS, state.hearts + 1)
            } else {
                state.hearts
            }
            return MatchGameReduction(
                state.copy(
                    cards = cards,
                    score = newScore,
                    hearts = newHearts,
                    isResolvingPair = true
                ),
                listOf(MatchGameCommand.CorrectMatch(current.wordId), MatchGameCommand.Persist)
            )
        }

        if (selected.type == current.type) {
            cards[selectedIndex] = selected.copy(isSelected = false)
            cards[index] = current.copy(isSelected = true)
            return MatchGameReduction(state.copy(cards = cards), listOf(MatchGameCommand.Persist))
        }

        cards[selectedIndex] = selected.copy(
            isSelected = false,
            feedback = MatchCardFeedback.MISMATCH
        )
        cards[index] = current.copy(
            isSelected = false,
            feedback = MatchCardFeedback.MISMATCH
        )
        return MatchGameReduction(
            state.copy(
                cards = cards,
                hearts = state.hearts - 1,
                isResolvingPair = true,
                latestMatchedWordId = state.latestMatchedWordId
                    ?.takeUnless { it == selected.wordId || it == current.wordId }
            ),
            listOf(
                MatchGameCommand.IncorrectMatch(setOf(selected.wordId, current.wordId)),
                MatchGameCommand.Persist
            )
        )
    }

    private fun resolveCorrectPair(
        state: MatchGameState,
        wordId: Int
    ): MatchGameReduction {
        val remainingCards = state.cards.filterNot { it.wordId == wordId }
        return if (remainingCards.isEmpty()) {
            MatchGameReduction(
                state.copy(cards = emptyList(), isResolvingPair = false),
                listOf(MatchGameCommand.StartNextRound)
            )
        } else {
            MatchGameReduction(
                state.copy(cards = remainingCards, isResolvingPair = false),
                listOf(MatchGameCommand.Persist)
            )
        }
    }

    private fun resolveMismatch(state: MatchGameState): MatchGameReduction {
        return if (state.hearts <= 0) {
            finish(state, AppStrings.Game.GAME_OVER)
        } else {
            MatchGameReduction(
                state.copy(
                    cards = state.cards.map { it.copy(feedback = MatchCardFeedback.NONE) },
                    isResolvingPair = false
                ),
                listOf(MatchGameCommand.Persist)
            )
        }
    }

    private fun reduceTimerTick(
        state: MatchGameState,
        remainingSeconds: Int
    ): MatchGameReduction {
        val normalized = remainingSeconds.coerceAtLeast(0)
        if (normalized > 0) {
            return MatchGameReduction(state.copy(remainingSeconds = normalized))
        }
        return finish(
            state.copy(remainingSeconds = 0),
            AppStrings.Game.TIME_IS_UP
        )
    }

    private fun finish(state: MatchGameState, message: String): MatchGameReduction {
        return MatchGameReduction(
            state.copy(
                hasStarted = false,
                gameVisible = false,
                cards = emptyList(),
                isResolvingPair = false,
                latestMatchedWordId = null,
                gameOverMessage = message,
                gameOverScore = state.score
            ),
            listOf(MatchGameCommand.SaveGame(message, state.score))
        )
    }
}
