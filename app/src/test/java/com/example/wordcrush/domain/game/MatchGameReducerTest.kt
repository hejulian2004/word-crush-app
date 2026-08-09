package com.example.wordcrush.domain.game

import com.example.wordcrush.ui.model.MatchCardType
import com.example.wordcrush.ui.model.MatchCardUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchGameReducerTest {
    private fun card(
        id: Int,
        wordId: Int,
        type: MatchCardType,
        selected: Boolean = false
    ) = MatchCardUiModel(
        id = id,
        wordId = wordId,
        text = "word-$id",
        type = type,
        isSelected = selected
    )

    @Test
    fun firstClickSelectsCardAndRequestsPersistence() {
        val state = MatchGameState(cards = listOf(card(1, 7, MatchCardType.ENGLISH)))

        val reduction = MatchGameReducer.reduce(state, MatchGameAction.CardClicked(0))

        assertTrue(reduction.state.cards.single().isSelected)
        assertTrue(reduction.commands.contains(MatchGameCommand.Persist))
    }

    @Test
    fun matchingDifferentCardTypesAddsScoreAndEmitsCorrectCommand() {
        val state = MatchGameState(
            hasStarted = true,
            gameVisible = true,
            cards = listOf(
                card(1, 7, MatchCardType.ENGLISH, selected = true),
                card(2, 7, MatchCardType.CHINESE)
            )
        )

        val reduction = MatchGameReducer.reduce(state, MatchGameAction.CardClicked(1))

        assertEquals(1, reduction.state.score)
        assertTrue(reduction.state.isResolvingPair)
        assertTrue(reduction.state.cards.all { it.isMatched })
        assertTrue(reduction.commands.contains(MatchGameCommand.CorrectMatch(7)))
    }

    @Test
    fun mismatchingCardsLoseHeartAndClearPreviousLatestWord() {
        val state = MatchGameState(
            hearts = 2,
            latestMatchedWordId = 7,
            cards = listOf(
                card(1, 7, MatchCardType.ENGLISH, selected = true),
                card(2, 8, MatchCardType.CHINESE)
            )
        )

        val reduction = MatchGameReducer.reduce(state, MatchGameAction.CardClicked(1))

        assertEquals(1, reduction.state.hearts)
        assertTrue(reduction.state.cards.all { it.feedback.name == "MISMATCH" })
        assertEquals(null, reduction.state.latestMatchedWordId)
        assertTrue(reduction.commands.contains(MatchGameCommand.IncorrectMatch(setOf(7, 8))))
    }

    @Test
    fun resolvingLastCorrectPairRequestsNextRound() {
        val state = MatchGameState(
            cards = listOf(
                card(1, 7, MatchCardType.ENGLISH),
                card(2, 7, MatchCardType.CHINESE)
            ),
            isResolvingPair = true
        )

        val reduction = MatchGameReducer.reduce(
            state,
            MatchGameAction.ResolveCorrectPair(7)
        )

        assertTrue(reduction.state.cards.isEmpty())
        assertFalse(reduction.state.isResolvingPair)
        assertTrue(reduction.commands.contains(MatchGameCommand.StartNextRound))
    }

    @Test
    fun timerExpiryFinishesGameAndRequestsRecordSave() {
        val state = MatchGameState(
            hasStarted = true,
            gameVisible = true,
            score = 12,
            remainingSeconds = 1
        )

        val reduction = MatchGameReducer.reduce(state, MatchGameAction.TimerTick(0))

        assertFalse(reduction.state.hasStarted)
        assertEquals("Time is up.", reduction.state.gameOverMessage)
        assertTrue(reduction.commands.contains(MatchGameCommand.SaveGame("Time is up.", 12)))
    }
}
