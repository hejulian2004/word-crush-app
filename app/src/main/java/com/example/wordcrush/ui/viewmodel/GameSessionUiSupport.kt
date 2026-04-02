package com.example.wordcrush.ui.viewmodel

import com.example.wordcrush.data.model.DailyLearningPlan
import com.example.wordcrush.data.model.ActiveMatchCardSnapshot
import com.example.wordcrush.data.model.WordItem
import com.example.wordcrush.ui.model.MatchCardFeedback
import com.example.wordcrush.ui.model.MatchCardType
import com.example.wordcrush.ui.model.MatchCardUiModel

internal data class DailyPlanPresentation(
    val title: String,
    val message: String
)

internal fun DailyLearningPlan.toPresentation(): DailyPlanPresentation {
    return DailyPlanPresentation(
        title = when {
            allWordsMastered -> "All words learned"
            isDailyCompleted -> "Today's words are done"
            else -> "Today's fixed set"
        },
        message = when {
            allWordsMastered -> "You have already learned every word in the word book."
            isDailyCompleted && canIncreaseDailyTarget ->
                "Today's words are complete. Increase the daily learning count if you want more words today."
            isDailyCompleted -> "Today's words are complete."
            todayTotalCount == 0 -> "No study words are available yet."
            else -> "Today's plan is fixed at $todayTotalCount words. Each word needs 3 correct matches."
        }
    )
}

internal fun DailyLearningPlan.toCompletionMessage(): String {
    return when {
        allWordsMastered -> "You have already learned all words."
        canIncreaseDailyTarget -> "Today's words are complete. Increase the daily learning count if you want to continue today."
        else -> "Today's words are complete."
    }
}

internal fun buildLearnedWordSummaries(words: List<WordItem>): List<String> {
    return words.map { word ->
        "${word.english} - ${word.chinese.replace("\n", " ").trim()}"
    }
}

internal fun MatchCardUiModel.toSnapshot(): ActiveMatchCardSnapshot {
    return ActiveMatchCardSnapshot(
        id = id,
        wordId = wordId,
        text = text,
        pronunciation = pronunciation,
        type = type.name,
        isMatched = isMatched,
        isSelected = isSelected,
        feedback = feedback.name
    )
}

internal fun ActiveMatchCardSnapshot.toUiModel(): MatchCardUiModel {
    return MatchCardUiModel(
        id = id,
        wordId = wordId,
        text = text,
        pronunciation = pronunciation,
        type = MatchCardType.valueOf(type),
        isMatched = isMatched,
        isSelected = isSelected,
        feedback = MatchCardFeedback.valueOf(feedback)
    )
}
