package com.example.wordcrush.data.repository

import com.example.wordcrush.Database.LearningMutationDao
import com.example.wordcrush.Database.LearningMutationEntity
import com.example.wordcrush.constants.AppConstants
import com.example.wordcrush.constants.AppStrings
import com.example.wordcrush.data.local.PreferenceManager
import com.example.wordcrush.data.model.DailyLearningPlan
import com.example.wordcrush.data.model.LearningMutationRequest
import com.example.wordcrush.data.model.LearningStateResponse
import com.example.wordcrush.data.model.LearningWordResponse
import com.example.wordcrush.data.model.LearningSyncRequest
import com.example.wordcrush.data.model.WordItem
import com.example.wordcrush.data.network.requireData
import com.example.wordcrush.data.remote.LearningRemoteDataSource
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
class LearningRepository @Inject constructor(
    private val wordRepository: WordRepository,
    private val remoteDataSource: LearningRemoteDataSource,
    private val mutationDao: LearningMutationDao,
    private val preferenceManager: PreferenceManager
) {
    private val syncMutex = Mutex()
    private var catalogLoaded = false

    suspend fun bootstrap(): Result<Unit> = syncMutex.withLock {
        runCatching {
            val localWords = wordRepository.getWords()
            refreshCatalogLocked()
            if (!preferenceManager.isLearningMigrationCompleted()) {
                localWords
                    .filter { it.masterCount > 0 || it.isMastered }
                    .forEach { word ->
                        enqueueLocked(
                            wordId = word.id,
                            operation = AppConstants.Learning.IMPORT_SNAPSHOT,
                            masterCount = word.masterCount.coerceIn(
                                0,
                                AppConstants.Learning.REQUIRED_CORRECT_MATCHES
                            )
                        )
                    }
                syncLocked().getOrThrow()
                preferenceManager.markLearningMigrationCompleted()
            } else {
                syncLocked().getOrThrow()
            }
            val state = remoteDataSource.getState().requireData()
            applyState(state)
            Unit
        }
    }

    suspend fun getWords(): List<WordItem> {
        ensureCatalogLoaded()
        return wordRepository.getWords()
    }

    suspend fun searchWords(query: String): List<WordItem> {
        val normalized = query.trim()
        return getWords().filter { word ->
            normalized.isBlank() ||
                word.english.contains(normalized, ignoreCase = true) ||
                word.chinese.contains(normalized, ignoreCase = true) ||
                word.pronunciation.contains(normalized, ignoreCase = true)
        }
    }

    suspend fun getWordsByMastered(isMastered: Boolean): List<WordItem> =
        getWords().filter { it.isMastered == isMastered }

    suspend fun getWordsByIds(ids: List<Int>): List<WordItem> {
        ensureCatalogLoaded()
        return wordRepository.getWordsByIds(ids)
    }

    suspend fun getDailyLearningPlan(): DailyLearningPlan {
        ensureCatalogLoaded()
        syncPending()
        val state = runCatching { remoteDataSource.getState().requireData() }.getOrNull()
        return if (state != null) {
            applyState(state)
            state.toDailyLearningPlan()
        } else {
            wordRepository.getDailyLearningPlan()
        }
    }

    suspend fun saveDailyTarget(input: String): Result<Int> {
        val target = input.toIntOrNull()
        if (
            target == null ||
            target < AppConstants.Learning.MIN_DAILY_WORD_TARGET ||
            target > AppConstants.Learning.MAX_DAILY_WORD_TARGET
        ) {
            return Result.failure(
                IllegalArgumentException(AppStrings.Errors.DAILY_TARGET_RANGE)
            )
        }
        preferenceManager.saveDailyWordTarget(target)
        syncMutex.withLock {
            enqueueLocked(
                operation = AppConstants.Learning.UPDATE_DAILY_TARGET,
                dailyTarget = target
            )
        }
        syncPending()
        return Result.success(target)
    }

    suspend fun updateWordMastered(wordId: Int, isMastered: Boolean): WordItem? {
        val updated = wordRepository.updateWordMastered(wordId, isMastered)
        if (updated != null) {
            syncMutex.withLock {
                enqueueLocked(
                    wordId = wordId,
                    operation = if (isMastered) {
                        AppConstants.Learning.IMPORT_SNAPSHOT
                    } else {
                        AppConstants.Learning.MARK_UNREMEMBERED
                    },
                    masterCount = if (isMastered) {
                        AppConstants.Learning.REQUIRED_CORRECT_MATCHES
                    } else {
                        0
                    }
                )
            }
        }
        syncPending()
        return updated
    }

    suspend fun recordCorrectMatch(wordId: Int): WordItem? {
        val updated = wordRepository.recordCorrectMatch(wordId)
        if (updated != null) {
            syncMutex.withLock {
                enqueueLocked(wordId = wordId, operation = AppConstants.Learning.CORRECT_MATCH)
            }
        }
        return updated
    }

    suspend fun recordIncorrectMatch(wordIds: Set<Int>) {
        wordRepository.recordIncorrectMatch(wordIds)
        if (wordIds.isEmpty()) return
        syncMutex.withLock {
            wordIds.forEach { wordId ->
                enqueueLocked(
                    wordId = wordId,
                    operation = AppConstants.Learning.MARK_UNREMEMBERED
                )
            }
        }
    }

    suspend fun syncPending(): Result<Unit> = syncMutex.withLock { syncLocked() }

    suspend fun pendingMutationCount(): Int = withContext(Dispatchers.IO) {
        mutationDao.countPending()
    }

    private suspend fun ensureCatalogLoaded() {
        if (catalogLoaded) return
        syncMutex.withLock {
            if (catalogLoaded) return@withLock
            runCatching { refreshCatalogLocked() }
        }
    }

    private suspend fun refreshCatalogLocked() {
        var page = 0
        val remoteWords = mutableListOf<LearningWordResponse>()
        while (true) {
            val catalog = remoteDataSource.getCatalog(
                page = page,
                size = AppConstants.Learning.CATALOG_PAGE_SIZE
            ).requireData()
            remoteWords += catalog.items
            if (
                catalog.items.isEmpty() ||
                remoteWords.size >= catalog.total ||
                catalog.items.size < AppConstants.Learning.CATALOG_PAGE_SIZE
            ) break
            page++
        }
        wordRepository.replaceRemoteCatalog(remoteWords)
        catalogLoaded = true
    }

    private suspend fun syncLocked(): Result<Unit> {
        return runCatching {
            while (true) {
                val pending = withContext(Dispatchers.IO) {
                    mutationDao.getPending(AppConstants.Learning.SYNC_BATCH_SIZE)
                }
                if (pending.isEmpty()) return@runCatching

                val response = remoteDataSource.sync(
                    LearningSyncRequest(pending.map { it.toRequest() })
                ).requireData()
                val accepted = response.acceptedMutationIds.toSet()
                val acceptedEntities = pending.filter { it.mutationId in accepted }
                if (acceptedEntities.isEmpty()) return@runCatching

                withContext(Dispatchers.IO) { mutationDao.deleteAll(acceptedEntities) }
                response.state?.let { applyState(it) }
                if (acceptedEntities.size < pending.size) return@runCatching
            }
        }
    }

    private suspend fun applyState(state: LearningStateResponse): DailyLearningPlan {
        wordRepository.mergeRemoteWords(state.todayWords)
        wordRepository.applyRemoteProgress(state.progress)
        preferenceManager.saveDailyWordTarget(state.dailyTarget)
        preferenceManager.saveDailyPlan(state.planDate, state.todayWordIds)
        return state.toDailyLearningPlan()
    }

    private suspend fun enqueueLocked(
        wordId: Int? = null,
        operation: String,
        masterCount: Int? = null,
        dailyTarget: Int? = null
    ) = withContext(Dispatchers.IO) {
        val mutation = LearningMutationEntity().apply {
            mutationId = UUID.randomUUID().toString()
            this.wordId = wordId
            this.operation = operation
            this.masterCount = masterCount
            this.dailyTarget = dailyTarget
            clientAt = Instant.now().toString()
            createdAt = System.currentTimeMillis()
        }
        mutationDao.insert(mutation)
    }

    private fun LearningMutationEntity.toRequest(): LearningMutationRequest =
        LearningMutationRequest(
            mutationId = mutationId,
            wordId = wordId,
            operation = operation,
            masterCount = masterCount,
            dailyTarget = dailyTarget,
            clientAt = clientAt
        )
}

private fun LearningStateResponse.toDailyLearningPlan(): DailyLearningPlan {
    val todayWords = todayWords.map { word ->
        WordItem(
            id = word.id,
            english = word.english,
            pronunciation = word.pronunciation,
            chinese = word.chinese,
            isMastered = word.mastered,
            masterCount = word.masterCount
        )
    }
    return DailyLearningPlan(
        dailyTarget = dailyTarget,
        todayWordIds = todayWordIds,
        todayWords = todayWords,
        activeWords = todayWords.filterNot { it.isMastered },
        completedCount = completedCount,
        availableUnmasteredCount = availableUnmasteredCount,
        allWordsMastered = allWordsMastered,
        isDailyCompleted = dailyCompleted,
        canIncreaseDailyTarget = canIncreaseDailyTarget
    )
}
