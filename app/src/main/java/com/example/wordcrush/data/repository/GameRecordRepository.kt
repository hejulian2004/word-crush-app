package com.example.wordcrush.data.repository

import com.example.wordcrush.Database.GameRecord.GameRecordDao
import com.example.wordcrush.Database.GameRecord.GameRecordEntity
import com.example.wordcrush.data.cache.AvatarCacheStore
import com.example.wordcrush.data.remote.GameRecordRemoteDataSource
import com.example.wordcrush.data.session.SessionManager
import com.example.wordcrush.data.model.DeleteGameRecordRequest
import com.example.wordcrush.data.model.GameRecordItem
import com.example.wordcrush.data.model.RankingItem
import com.example.wordcrush.data.model.RankingRequest
import com.example.wordcrush.data.model.RemoteGameRecord
import com.example.wordcrush.data.model.RemoteRankingItem
import com.example.wordcrush.data.model.SaveGameRecordRequest
import com.example.wordcrush.data.model.ScoreSummary
import com.example.wordcrush.data.model.UsernameRequest
import com.example.wordcrush.utils.LogUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class GameRecordRepository @Inject constructor(
    private val gameRecordDao: GameRecordDao,
    private val remoteDataSource: GameRecordRemoteDataSource,
    private val sessionManager: SessionManager,
    private val avatarCacheStore: AvatarCacheStore
) {
    private companion object {
        const val MAX_RANKING_CACHE_ENTRIES = 6
        val GAME_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss.SSS")
    }

    private val rankingCache = object : LinkedHashMap<String, RankingCacheEntry>(MAX_RANKING_CACHE_ENTRIES, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, RankingCacheEntry>?): Boolean {
            return size > MAX_RANKING_CACHE_ENTRIES
        }
    }

    suspend fun saveLocalRecord(
        gameType: Int,
        score: Int,
        learnedWords: List<String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val username = currentUsername() ?: throw IllegalStateException("No logged-in user found.")
            val time = LocalDateTime.now().format(GAME_TIME_FORMATTER)
            gameRecordDao.insertGameRecord(
                buildLocalEntity(
                    username = username,
                    gameType = gameType,
                    score = score,
                    time = time,
                    learnedWords = learnedWords
                )
            )
        }
    }

    suspend fun getLocalRecords(): List<GameRecordItem> = withContext(Dispatchers.IO) {
        val username = currentUsername() ?: return@withContext emptyList()
        gameRecordDao.getAllGameRecords(username).map { entity -> entity.toGameRecordItem() }
    }

    suspend fun getCachedRanking(gameType: Int, limit: Int): List<RankingItem> = withContext(Dispatchers.IO) {
        synchronized(rankingCache) {
            rankingCache[rankingCacheKey(gameType, limit)]?.items
        }.orEmpty().toRankingItems()
    }

    suspend fun getRanking(gameType: Int, limit: Int): Result<List<RankingItem>> = withContext(Dispatchers.IO) {
        runCatching {
            val cacheKey = rankingCacheKey(gameType, limit)
            val cachedEntry = synchronized(rankingCache) { rankingCache[cacheKey] }
            val remoteItems = runCatching {
                remoteDataSource.getTopRankings(RankingRequest(gameType, limit)).data.orEmpty()
            }.getOrElse { error ->
                cachedEntry?.items?.let { return@runCatching it.toRankingItems() }
                throw error
            }
            val signature = remoteItems.toSignature()
            val effectiveItems = synchronized(rankingCache) {
                val current = rankingCache[cacheKey]
                if (current != null && current.signature == signature) {
                    current.items
                } else {
                    rankingCache[cacheKey] = RankingCacheEntry(signature, remoteItems)
                    remoteItems
                }
            }
            effectiveItems.toRankingItems()
        }
    }

    suspend fun getScoreSummary(): ScoreSummary = withContext(Dispatchers.IO) {
        val username = currentUsername() ?: return@withContext ScoreSummary(0, 0)
        val scores = gameRecordDao.getGameTypeAndScore(username)
        var breakthrough = 0
        var timeLimit = 0
        scores.forEach { score ->
            if (score.gameType == 0) breakthrough += score.score else timeLimit += score.score
        }
        ScoreSummary(breakthrough, timeLimit)
    }

    suspend fun saveRecord(
        gameType: Int,
        score: Int,
        learnedWords: List<String>
    ): Result<SaveRecordResult> = withContext(Dispatchers.IO) {
        runCatching {
            val username = currentUsername() ?: throw IllegalStateException("No logged-in user found.")
            val time = LocalDateTime.now().format(GAME_TIME_FORMATTER)
            val entity = buildLocalEntity(
                username = username,
                gameType = gameType,
                score = score,
                time = time,
                learnedWords = learnedWords
            )
            gameRecordDao.insertGameRecord(entity)

            val syncedToCloud = runCatching {
                uploadRecordToCloud(entity.toSaveRequest())
            }.getOrElse { error ->
                LogUtils.w("Remote record sync failed after local save: ${error.message}")
                false
            }

            SaveRecordResult(
                syncedToCloud = syncedToCloud,
                savedLocally = true
            )
        }
    }

    suspend fun deleteRecord(record: GameRecordItem): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val username = currentUsername() ?: throw IllegalStateException("No logged-in user found.")
            val deleted = gameRecordDao.deleteRecordByGameTypeScoreTime(
                username,
                record.gameType,
                record.score,
                record.time
            )
            if (deleted <= 0) {
                throw IllegalStateException("Record was not found.")
            }

            remoteDataSource.deleteGameRecord(
                DeleteGameRecordRequest(
                    username = username,
                    gameType = record.gameType,
                    score = record.score,
                    time = record.time
                )
            )
            Unit
        }
    }

    suspend fun syncFromCloud(): Result<CloudSyncResult> = withContext(Dispatchers.IO) {
        runCatching {
            val username = currentUsername() ?: throw IllegalStateException("No logged-in user found.")
            val localEntities = gameRecordDao.getAllGameRecords(username)
            var remoteEntities = fetchRemoteEntities(username)
            val remoteSnapshots = remoteEntities.toSnapshots().toSet()
            val missingLocalEntities = localEntities.filter { entity ->
                entity.toSnapshot() !in remoteSnapshots
            }

            var uploadedCount = 0
            missingLocalEntities.forEach { entity ->
                if (!uploadRecordToCloud(entity.toSaveRequest())) {
                    throw IllegalStateException("Cloud sync failed while uploading local records.")
                }
                uploadedCount++
            }

            if (uploadedCount > 0) {
                remoteEntities = fetchRemoteEntities(username)
            }

            val localSnapshots = localEntities.toSnapshots()
            val remoteSnapshotsAfterUpload = remoteEntities.toSnapshots()
            val replacedLocalDatabase = localSnapshots != remoteSnapshotsAfterUpload
            if (replacedLocalDatabase) {
                gameRecordDao.replaceRecordsForUsername(username, remoteEntities)
            }

            CloudSyncResult(
                uploadedCount = uploadedCount,
                replacedLocalDatabase = replacedLocalDatabase,
                localRecordCount = remoteEntities.size
            )
        }
    }

    private suspend fun fetchRemoteEntities(username: String): List<GameRecordEntity> {
        return remoteDataSource.getAllGameRecords(UsernameRequest(username))
            .data
            .orEmpty()
            .map { remote -> remote.toGameRecordEntity() }
    }

    private suspend fun uploadRecordToCloud(request: SaveGameRecordRequest): Boolean {
        return runCatching {
            remoteDataSource.saveGameRecord(request)
            true
        }.getOrDefault(false)
    }

    private fun buildLocalEntity(
        username: String,
        gameType: Int,
        score: Int,
        time: String,
        learnedWords: List<String>
    ): GameRecordEntity {
        return GameRecordEntity().apply {
            this.username = username
            this.gameType = gameType
            this.score = score
            this.time = time
            this.learnedWords = learnedWords
        }
    }

    private fun rankingCacheKey(gameType: Int, limit: Int): String {
        return "$gameType:$limit"
    }

    private fun List<RemoteRankingItem>.toSignature(): String {
        return joinToString(separator = "|") { item ->
            "${item.username}:${item.score}:${item.time}:${item.avatarVersion}"
        }
    }

    private fun List<RemoteRankingItem>.toRankingItems(): List<RankingItem> {
        return map { item ->
            RankingItem(
                username = item.username,
                score = item.score,
                time = item.time,
                avatarUrl = avatarCacheStore.resolve(item.username, item.avatarVersion),
                avatarVersion = item.avatarVersion
            )
        }
    }

    private suspend fun currentUsername(): String? {
        return sessionManager.currentUsername
    }
}

data class SaveRecordResult(
    val syncedToCloud: Boolean,
    val savedLocally: Boolean
)

data class CloudSyncResult(
    val uploadedCount: Int,
    val replacedLocalDatabase: Boolean,
    val localRecordCount: Int
)

private data class RankingCacheEntry(
    val signature: String,
    val items: List<RemoteRankingItem>
)

private data class RecordSnapshot(
    val username: String,
    val gameType: Int,
    val score: Int,
    val time: String,
    val learnedWords: List<String>
)

private fun List<GameRecordEntity>.toSnapshots(): List<RecordSnapshot> {
    return map { entity ->
        entity.toSnapshot()
    }.sortedBy { snapshot ->
        "${snapshot.username}|${snapshot.gameType}|${snapshot.score}|${snapshot.time}|${snapshot.learnedWords.joinToString(",")}"
    }
}

private fun GameRecordEntity.toSnapshot(): RecordSnapshot {
    return RecordSnapshot(
        username = username,
        gameType = gameType,
        score = score,
        time = time,
        learnedWords = learnedWords.orEmpty()
    )
}

private fun GameRecordEntity.toGameRecordItem(): GameRecordItem {
    return GameRecordItem(
        id = id,
        gameType = gameType,
        time = time,
        score = score,
        learnedWords = learnedWords.orEmpty()
    )
}

private fun GameRecordEntity.toSaveRequest(): SaveGameRecordRequest {
    return SaveGameRecordRequest(
        username = username,
        gameType = gameType,
        score = score,
        time = time,
        learnedWords = learnedWords.orEmpty()
    )
}

private fun RemoteGameRecord.toGameRecordEntity(): GameRecordEntity {
    return GameRecordEntity().apply {
        username = this@toGameRecordEntity.username
        gameType = this@toGameRecordEntity.gameType
        score = this@toGameRecordEntity.score
        time = this@toGameRecordEntity.time
        learnedWords = this@toGameRecordEntity.learnedWords
    }
}
