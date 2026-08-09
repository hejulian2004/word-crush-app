package com.example.wordcrush.data.repository

import android.content.Context
import com.example.wordcrush.Database.Word.WordDao
import com.example.wordcrush.Database.Word.WordEntity
import com.example.wordcrush.constants.AppConstants
import com.example.wordcrush.data.local.PreferenceManager
import com.example.wordcrush.data.model.DailyLearningPlan
import com.example.wordcrush.data.model.LearningProgressResponse
import com.example.wordcrush.data.model.LearningWordResponse
import com.example.wordcrush.data.model.WordItem
import com.opencsv.CSVReader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wordDao: WordDao,
    private val preferenceManager: PreferenceManager
) {
    private val cacheMutex = Mutex()
    private var cachedWords: List<WordItem>? = null

    suspend fun getWords(): List<WordItem> = withContext(Dispatchers.IO) {
        getCachedWords()
    }

    suspend fun upsertRemoteCatalog(words: List<LearningWordResponse>) = withContext(Dispatchers.IO) {
        if (words.isEmpty()) return@withContext
        val existing = wordDao.getAllWords().associateBy { it.id }
        val inserts = words.filterNot { existing.containsKey(it.id) }.map { remote ->
            WordEntity().apply {
                id = remote.id
                english = remote.english
                pronunciation = remote.pronunciation
                chinese = remote.chinese
                setMaster(false)
                masterCount = 0
            }
        }
        if (inserts.isNotEmpty()) {
            wordDao.insertAll(inserts)
        }
        val updates = words.mapNotNull { remote ->
            existing[remote.id]?.apply {
                english = remote.english
                pronunciation = remote.pronunciation
                chinese = remote.chinese
            }
        }
        if (updates.isNotEmpty()) {
            wordDao.updateAll(updates)
        }
        cacheMutex.withLock {
            cachedWords = wordDao.getAllWords().map { it.toWordItem() }
        }
    }

    suspend fun applyRemoteProgress(progress: List<LearningProgressResponse>) = withContext(Dispatchers.IO) {
        if (progress.isEmpty()) return@withContext
        val changed = progress.mapNotNull { remote ->
            wordDao.getWordById(remote.wordId)?.apply {
                masterCount = remote.masterCount.coerceIn(0, AppConstants.Learning.REQUIRED_CORRECT_MATCHES)
                setMaster(remote.mastered || masterCount >= AppConstants.Learning.REQUIRED_CORRECT_MATCHES)
            }
        }
        if (changed.isNotEmpty()) {
            wordDao.updateAll(changed)
            cacheMutex.withLock {
                val changedMap = changed.associate { it.id to it.toWordItem() }
                cachedWords = (cachedWords ?: wordDao.getAllWords().map { it.toWordItem() })
                    .map { changedMap[it.id] ?: it }
            }
        }
    }

    suspend fun searchWords(query: String): List<WordItem> = withContext(Dispatchers.IO) {
        val normalized = query.trim()
        val words = getCachedWords()
        if (normalized.isBlank()) {
            words
        } else {
            words.filter { word ->
                word.english.contains(normalized, ignoreCase = true) ||
                    word.chinese.contains(normalized, ignoreCase = true) ||
                    word.pronunciation.contains(normalized, ignoreCase = true)
            }
        }
    }

    suspend fun getWordsByMastered(isMastered: Boolean): List<WordItem> = withContext(Dispatchers.IO) {
        getCachedWords().filter { it.isMastered == isMastered }
    }

    suspend fun getWordsByIds(ids: List<Int>): List<WordItem> = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) {
            return@withContext emptyList()
        }
        val idSet = ids.toSet()
        getCachedWords().filter { it.id in idSet }
    }

    suspend fun updateWordMastered(wordId: Int, isMastered: Boolean): WordItem? = withContext(Dispatchers.IO) {
        val entity = wordDao.getWordById(wordId) ?: return@withContext null
        val targetMasterCount = if (isMastered) AppConstants.Learning.REQUIRED_CORRECT_MATCHES else 0
        if (entity.isMaster() == isMastered && entity.masterCount == targetMasterCount) {
            return@withContext updateCachedWord(entity.toWordItem())
        }
        entity.setMaster(isMastered)
        entity.masterCount = targetMasterCount
        wordDao.update(entity)
        updateCachedWord(
            updatedWord = entity.toWordItem(),
            fallbackOriginal = entity.toWordItem()
        )
    }

    suspend fun recordCorrectMatch(wordId: Int): WordItem? = withContext(Dispatchers.IO) {
        val entity = wordDao.getWordById(wordId) ?: return@withContext null
        val newCount = (entity.masterCount + 1).coerceAtMost(AppConstants.Learning.REQUIRED_CORRECT_MATCHES)
        val shouldBeMastered = newCount >= AppConstants.Learning.REQUIRED_CORRECT_MATCHES
        if (entity.masterCount == newCount && entity.isMaster() == shouldBeMastered) {
            return@withContext updateCachedWord(entity.toWordItem())
        }
        entity.masterCount = newCount
        entity.setMaster(shouldBeMastered)
        wordDao.update(entity)
        updateCachedWord(entity.toWordItem())
    }

    suspend fun recordIncorrectMatch(wordIds: Set<Int>) = withContext(Dispatchers.IO) {
        if (wordIds.isEmpty()) {
            return@withContext
        }

        val updatedEntities = wordDao.getWordsByIds(wordIds)
        if (updatedEntities.isEmpty()) {
            return@withContext
        }

        val changedEntities = updatedEntities.filter { entity ->
            entity.masterCount != 0 || entity.isMaster()
        }.onEach { entity ->
            entity.masterCount = 0
            entity.setMaster(false)
        }
        if (changedEntities.isEmpty()) {
            return@withContext
        }
        wordDao.updateAll(changedEntities)

        val updatedWords = changedEntities.map { it.toWordItem() }
        if (updatedWords.isNotEmpty()) {
            updateCachedWords(updatedWords)
        }
    }

    suspend fun getDailyLearningPlan(): DailyLearningPlan = withContext(Dispatchers.IO) {
        val words = getCachedWords()
        val unmasteredWords = words.filterNot { it.isMastered }
        val dailyTarget = preferenceManager.getDailyWordTarget()
            .coerceAtLeast(AppConstants.Learning.MIN_DAILY_WORD_TARGET)

        if (unmasteredWords.isEmpty()) {
            preferenceManager.clearDailyPlan()
            return@withContext DailyLearningPlan(
                dailyTarget = dailyTarget,
                todayWordIds = emptyList(),
                todayWords = emptyList(),
                activeWords = emptyList(),
                completedCount = 0,
                availableUnmasteredCount = 0,
                allWordsMastered = true,
                isDailyCompleted = true,
                canIncreaseDailyTarget = false
            )
        }

        val todayKey = todayKey()
        val savedDate = preferenceManager.getDailyPlanDate()
        val savedIds = preferenceManager.getDailyPlanWordIds()
        val todayIds = when {
            savedDate != todayKey -> {
                buildNewTodayWordIds(unmasteredWords, dailyTarget)
            }
            savedIds.isEmpty() -> {
                buildNewTodayWordIds(unmasteredWords, dailyTarget)
            }
            dailyTarget > savedIds.size -> {
                extendTodayWordIds(savedIds, unmasteredWords, dailyTarget)
            }
            else -> {
                savedIds
            }
        }.distinct()

        if (savedDate != todayKey || savedIds != todayIds) {
            preferenceManager.saveDailyPlan(todayKey, todayIds)
        }

        val wordById = words.associateBy { it.id }
        val todayWords = todayIds.mapNotNull(wordById::get)
        val activeWords = todayWords.filterNot { it.isMastered }
        val completedCount = todayWords.size - activeWords.size

        DailyLearningPlan(
            dailyTarget = dailyTarget,
            todayWordIds = todayIds,
            todayWords = todayWords,
            activeWords = activeWords,
            completedCount = completedCount,
            availableUnmasteredCount = unmasteredWords.size,
            allWordsMastered = false,
            isDailyCompleted = activeWords.isEmpty(),
            canIncreaseDailyTarget = activeWords.isEmpty() && unmasteredWords.size > 0
        )
    }

    suspend fun resetAllProgress() = withContext(Dispatchers.IO) {
        val entities = wordDao.getAllWords()
        if (entities.isEmpty()) {
            preferenceManager.clearDailyPlan()
            cacheMutex.withLock {
                cachedWords = emptyList()
            }
            return@withContext
        }

        val changedEntities = entities.filter { entity ->
            entity.masterCount != 0 || entity.isMaster()
        }.onEach { entity ->
            entity.masterCount = 0
            entity.setMaster(false)
        }
        if (changedEntities.isNotEmpty()) {
            wordDao.updateAll(changedEntities)
        }
        preferenceManager.clearDailyPlan()
        cacheMutex.withLock {
            cachedWords = entities.map { it.toWordItem() }
        }
    }

    private suspend fun getCachedWords(): List<WordItem> {
        cachedWords?.let { return it }
        return cacheMutex.withLock {
            cachedWords?.let { return@withLock it }
            ensureSeeded()
            wordDao.getAllWords().map { it.toWordItem() }.also { loadedWords ->
                cachedWords = loadedWords
            }
        }
    }

    private suspend fun updateCachedWord(updatedWord: WordItem, fallbackOriginal: WordItem = updatedWord): WordItem {
        return cacheMutex.withLock {
            val currentWords = cachedWords ?: emptyList()
            val hasExisting = currentWords.any { it.id == updatedWord.id }
            val updatedWords = if (hasExisting) {
                currentWords.map { word ->
                    if (word.id == updatedWord.id) updatedWord else word
                }
            } else {
                currentWords + fallbackOriginal
            }
            cachedWords = updatedWords
            updatedWords.firstOrNull { it.id == updatedWord.id } ?: updatedWord
        }
    }

    private suspend fun updateCachedWords(updatedWords: List<WordItem>) {
        cacheMutex.withLock {
            val updatedMap = updatedWords.associateBy { it.id }
            cachedWords = (cachedWords ?: emptyList()).map { word ->
                updatedMap[word.id] ?: word
            }
        }
    }

    private fun buildNewTodayWordIds(unmasteredWords: List<WordItem>, dailyTarget: Int): List<Int> {
        return unmasteredWords
            .shuffled()
            .take(dailyTarget)
            .map { it.id }
    }

    private fun extendTodayWordIds(
        savedIds: List<Int>,
        unmasteredWords: List<WordItem>,
        dailyTarget: Int
    ): List<Int> {
        val additionalIds = unmasteredWords
            .map { it.id }
            .filterNot { savedIds.contains(it) }
            .shuffled()
            .take(dailyTarget - savedIds.size)
        return savedIds + additionalIds
    }

    private fun todayKey(): String {
        return SimpleDateFormat(AppConstants.WordBook.DATE_FORMAT, Locale.US).format(Date())
    }

    private fun ensureSeeded() {
        if (wordDao.getAllWords().isNotEmpty()) {
            return
        }

        context.assets.open(AppConstants.WordBook.ASSET_FILE_NAME).use { inputStream ->
            CSVReader(InputStreamReader(inputStream)).use { reader ->
                val entities = reader.readAll()
                    .mapNotNull { row ->
                        if (
                            row.size != AppConstants.WordBook.CSV_COLUMN_COUNT ||
                            row[0] == AppConstants.WordBook.CSV_SEQUENCE_HEADER
                        ) {
                            null
                        } else {
                            WordEntity().apply {
                                id = row[0].toIntOrNull() ?: return@mapNotNull null
                                english = row[1]
                                pronunciation = row[2]
                                chinese = row[3]
                                setMaster(false)
                                masterCount = 0
                            }
                        }
                    }
                if (entities.isNotEmpty()) {
                    wordDao.insertAll(entities)
                }
            }
        }
    }
}

private fun WordEntity.toWordItem(): WordItem {
    return WordItem(
        id = id,
        english = english,
        pronunciation = pronunciation,
        chinese = chinese,
        isMastered = isMaster(),
        masterCount = masterCount
    )
}
