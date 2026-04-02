package com.example.wordcrush.ui.model

import com.example.wordcrush.data.model.WordItem

enum class MatchCardType {
    ENGLISH,
    CHINESE
}

enum class MatchCardFeedback {
    NONE,
    MATCHED,
    MISMATCH
}

data class MatchCardUiModel(
    val id: Int,
    val wordId: Int,
    val text: String,
    val pronunciation: String? = null,
    val type: MatchCardType,
    val isMatched: Boolean = false,
    val isSelected: Boolean = false,
    val feedback: MatchCardFeedback = MatchCardFeedback.NONE
)

data class MatchRound(
    val words: List<WordItem>,
    val cards: List<MatchCardUiModel>,
    val nextCursor: Int
)

sealed interface MatchGameEvent {
    data class Message(val text: String) : MatchGameEvent
    data class PlayAudio(val word: String, val type: Int = 1) : MatchGameEvent
    data class GameOver(val score: Int, val message: String) : MatchGameEvent
}

internal fun buildMatchRound(words: List<WordItem>, startCursor: Int, pairCount: Int = 6): MatchRound? {
    if (words.isEmpty()) return null

    val selectionCount = minOf(pairCount, words.size)
    val normalizedCursor = startCursor.mod(words.size)
    val selectedWords = (0 until selectionCount).map { offset ->
        words[(normalizedCursor + offset) % words.size]
    }

    val cards = selectedWords.flatMap { word ->
        listOf(
            MatchCardUiModel(
                id = word.id * 10 + 1,
                wordId = word.id,
                text = word.english,
                pronunciation = word.pronunciation.replace("/", ""),
                type = MatchCardType.ENGLISH
            ),
            MatchCardUiModel(
                id = word.id * 10 + 2,
                wordId = word.id,
                text = word.chinese.replace("\n", " ").trim(),
                pronunciation = null,
                type = MatchCardType.CHINESE
            )
        )
    }.shuffled()

    return MatchRound(
        words = selectedWords,
        cards = cards,
        nextCursor = (normalizedCursor + selectionCount) % words.size
    )
}
