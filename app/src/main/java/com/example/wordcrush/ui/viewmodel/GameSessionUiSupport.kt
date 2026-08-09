package com.example.wordcrush.ui.viewmodel

import com.example.wordcrush.data.model.DailyLearningPlan
import com.example.wordcrush.data.model.ActiveMatchCardSnapshot
import com.example.wordcrush.data.model.WordItem
import com.example.wordcrush.constants.AppStrings
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
            allWordsMastered -> AppStrings.Learning.ALL_WORDS_LEARNED
            isDailyCompleted -> AppStrings.Learning.DAILY_WORDS_DONE
            else -> AppStrings.Learning.TODAY_FIXED_SET
        },
        message = when {
            allWordsMastered -> AppStrings.Learning.ALL_WORDS_BOOK_COMPLETE
            isDailyCompleted && canIncreaseDailyTarget ->
                AppStrings.Learning.DAILY_COMPLETE_INCREASE_HINT
            isDailyCompleted -> AppStrings.Learning.DAILY_COMPLETE
            todayTotalCount == 0 -> AppStrings.Learning.NO_STUDY_WORDS
            else -> AppStrings.Learning.fixedPlan(todayTotalCount)
        }
    )
}

internal fun DailyLearningPlan.toCompletionMessage(): String {
    return when {
        allWordsMastered -> AppStrings.Learning.ALL_WORDS_COMPLETE
        canIncreaseDailyTarget -> AppStrings.Learning.DAILY_COMPLETE_CONTINUE_HINT
        else -> AppStrings.Learning.DAILY_COMPLETE
    }
}

internal fun buildLearnedWordSummaries(words: List<WordItem>): List<String> {
    return words.map { word ->
        AppStrings.chineseSummary(word.english, word.chinese)
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
