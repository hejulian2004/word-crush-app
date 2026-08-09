package com.example.wordcrush.domain.usecase

import com.example.wordcrush.data.local.PreferenceManager
import com.example.wordcrush.data.model.DailyLearningPlan
import com.example.wordcrush.data.model.ScoreSummary
import com.example.wordcrush.data.model.WordItem
import com.example.wordcrush.data.repository.GameRecordRepository
import com.example.wordcrush.data.repository.WordRepository
import javax.inject.Inject

class GetDailyLearningPlanUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(): DailyLearningPlan = wordRepository.getDailyLearningPlan()
}

class SaveDailyTargetUseCase @Inject constructor(
    private val preferenceManager: PreferenceManager
) {
    suspend operator fun invoke(input: String): Result<Int> {
        val target = input.toIntOrNull()
        if (target == null || target <= 0) {
            return Result.failure(
                IllegalArgumentException("Please enter a daily learning count greater than 0.")
            )
        }
        preferenceManager.saveDailyWordTarget(target)
        return Result.success(target)
    }
}

class GetScoreSummaryUseCase @Inject constructor(
    private val gameRecordRepository: GameRecordRepository
) {
    suspend operator fun invoke(): ScoreSummary = gameRecordRepository.getScoreSummary()
}

class SyncGameRecordsUseCase @Inject constructor(
    private val gameRecordRepository: GameRecordRepository
) {
    suspend operator fun invoke() = gameRecordRepository.syncFromCloud()
}

class SearchWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(query: String, filter: WordFilter): List<WordItem> {
        val baseWords = when (filter) {
            WordFilter.ALL -> wordRepository.getWords()
            WordFilter.MASTERED -> wordRepository.getWordsByMastered(true)
            WordFilter.UNMASTERED -> wordRepository.getWordsByMastered(false)
        }
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return baseWords
        return baseWords.filter { word ->
            word.english.contains(normalizedQuery, ignoreCase = true) ||
                word.chinese.contains(normalizedQuery, ignoreCase = true) ||
                word.pronunciation.contains(normalizedQuery, ignoreCase = true)
        }
    }
}

class UpdateWordMasteryUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(wordId: Int, isMastered: Boolean): WordItem? =
        wordRepository.updateWordMastered(wordId, isMastered)
}

enum class WordFilter {
    ALL,
    MASTERED,
    UNMASTERED
}
