package com.example.wordcrush.domain.usecase

import com.example.wordcrush.data.model.DailyLearningPlan
import com.example.wordcrush.data.model.ScoreSummary
import com.example.wordcrush.data.model.WordItem
import com.example.wordcrush.data.repository.GameRecordRepository
import com.example.wordcrush.data.repository.LearningRepository
import javax.inject.Inject

class GetDailyLearningPlanUseCase @Inject constructor(
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(): DailyLearningPlan = learningRepository.getDailyLearningPlan()
}

class SaveDailyTargetUseCase @Inject constructor(
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(input: String): Result<Int> = learningRepository.saveDailyTarget(input)
}

class BootstrapLearningDataUseCase @Inject constructor(
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(): Result<Unit> = learningRepository.bootstrap()
}

class SyncLearningMutationsUseCase @Inject constructor(
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(): Result<Unit> = learningRepository.syncPending()
}

class GetPendingLearningMutationsUseCase @Inject constructor(
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(): Int = learningRepository.pendingMutationCount()
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
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(query: String, filter: WordFilter): List<WordItem> {
        val baseWords = when (filter) {
            WordFilter.ALL -> learningRepository.getWords()
            WordFilter.MASTERED -> learningRepository.getWordsByMastered(true)
            WordFilter.UNMASTERED -> learningRepository.getWordsByMastered(false)
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
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(wordId: Int, isMastered: Boolean): WordItem? =
        learningRepository.updateWordMastered(wordId, isMastered)
}

enum class WordFilter {
    ALL,
    MASTERED,
    UNMASTERED
}
