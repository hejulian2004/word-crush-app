package com.example.wordcrush.ui.model

import com.example.wordcrush.data.model.WordItem
import com.example.wordcrush.constants.AppConstants
import com.example.wordcrush.constants.AppStrings

enum class MatchCardType {
    ENGLISH,
    CHINESE
}

enum class MatchCardFeedback {
    NONE,
    MATCHED,
    MISMATCH
}

enum class MatchMode(
    val gameType: Int,
    val title: String,
    val subtitle: String,
    val emptyTitle: String,
    val emptyMessage: String
) {
    CLASSIC(
        gameType = 0,
        title = AppStrings.Game.gameTypeName(isClassic = true),
        subtitle = AppStrings.Game.MODE_SUBTITLE,
        emptyTitle = AppStrings.Game.READY_TO_START,
        emptyMessage = AppStrings.Game.CLASSIC_EMPTY_MESSAGE
    ),
    TIMED(
        gameType = 1,
        title = AppStrings.Game.gameTypeName(isClassic = false),
        subtitle = AppStrings.Game.MODE_SUBTITLE,
        emptyTitle = AppStrings.Game.READY_TO_START,
        emptyMessage = AppStrings.Game.TIMED_EMPTY_MESSAGE
    )
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

internal fun buildMatchRound(
    words: List<WordItem>,
    startCursor: Int,
    pairCount: Int = AppConstants.Game.ROUND_PAIR_COUNT
): MatchRound? {
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
