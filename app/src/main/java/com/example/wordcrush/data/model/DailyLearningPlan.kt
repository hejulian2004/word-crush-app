package com.example.wordcrush.data.model

data class DailyLearningPlan(
    val dailyTarget: Int,
    val todayWordIds: List<Int>,
    val todayWords: List<WordItem>,
    val activeWords: List<WordItem>,
    val completedCount: Int,
    val availableUnmasteredCount: Int,
    val allWordsMastered: Boolean,
    val isDailyCompleted: Boolean,
    val canIncreaseDailyTarget: Boolean
) {
    val todayTotalCount: Int
        get() = todayWords.size

    val remainingCount: Int
        get() = activeWords.size
}
