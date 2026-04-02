package com.example.wordcrush.data.repository

import com.example.wordcrush.Database.GameRecord.GameRecordDao
import com.example.wordcrush.Database.GameRecord.GameRecordEntity
import com.example.wordcrush.data.api.GameRecordApi
import com.example.wordcrush.data.local.PreferenceManager
import com.example.wordcrush.data.model.DeleteGameRecordRequest
import com.example.wordcrush.data.model.GameRecordItem
import com.example.wordcrush.data.model.RankingItem
import com.example.wordcrush.data.model.RankingRequest
import com.example.wordcrush.data.model.RemoteGameRecord
import com.example.wordcrush.data.model.SaveGameRecordRequest
import com.example.wordcrush.data.model.ScoreSummary
import com.example.wordcrush.data.model.UsernameRequest
import com.example.wordcrush.utils.LogUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

@Singleton
class GameRecordRepository @Inject constructor(
    private val gameRecordDao: GameRecordDao,
    private val gameRecordApi: GameRecordApi,
    private val preferenceManager: PreferenceManager
) {

    suspend fun saveLocalRecord(
        gameType: Int,
        score: Int,
        learnedWords: List<String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val username = currentUsername() ?: throw IllegalStateException("No logged-in user found.")
            val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss.SSS"))
            val entity = GameRecordEntity().apply {
                this.username = username
                this.gameType = gameType
                this.score = score
                this.time = time
                this.learnedWords = learnedWords
            }
            gameRecordDao.insertGameRecord(entity)
        }
    }

    suspend fun getLocalRecords(): List<GameRecordItem> = withContext(Dispatchers.IO) {
        val username = currentUsername() ?: return@withContext emptyList()
        gameRecordDao.getAllGameRecords(username).map { entity -> entity.toGameRecordItem() }
    }

    suspend fun getRanking(gameType: Int, limit: Int): Result<List<RankingItem>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = gameRecordApi.getTopRankings(RankingRequest(gameType, limit))
            val body = response.body()
            if (!response.isSuccessful || body == null || !body.isSuccess()) {
                throw IllegalStateException("Unable to fetch ranking data.")
            }
            body.message.orEmpty().map { item ->
                RankingItem(
                    username = item.username,
                    score = item.score,
                    time = item.time
                )
            }
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
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val username = currentUsername() ?: throw IllegalStateException("No logged-in user found.")
            val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss.SSS"))
            val entity = GameRecordEntity().apply {
                this.username = username
                this.gameType = gameType
                this.score = score
                this.time = time
                this.learnedWords = learnedWords
            }
            gameRecordDao.insertGameRecord(entity)

            val response = gameRecordApi.saveGameRecord(
                SaveGameRecordRequest(
                    username = username,
                    gameType = gameType,
                    score = score,
                    time = time,
                    learnedWords = learnedWords
                )
            )
            if (!response.isSuccessful || response.body() == null || !response.body()!!.isSuccess()) {
                LogUtils.w("Remote record sync failed after local save")
            }
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

            val response = gameRecordApi.deleteGameRecord(
                DeleteGameRecordRequest(
                    username = username,
                    gameType = record.gameType,
                    score = record.score,
                    time = record.time
                )
            )
            if (!response.isSuccessful || response.body() == null || !response.body()!!.isSuccess()) {
                LogUtils.w("Remote record deletion failed after local delete")
            }
        }
    }

    suspend fun syncFromCloud(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val username = currentUsername() ?: throw IllegalStateException("No logged-in user found.")
            val response = gameRecordApi.getAllGameRecords(UsernameRequest(username))
            val body = response.body()
            if (!response.isSuccessful || body == null || !body.isSuccess()) {
                throw IllegalStateException("Cloud sync failed.")
            }

            val remoteEntities = body.message.orEmpty().map { remote -> remote.toGameRecordEntity() }
            val localEntities = gameRecordDao.getAllGameRecords(username)
            if (localEntities.isSameRecordSetAs(remoteEntities)) {
                return@runCatching
            }

            gameRecordDao.deleteAllRecordsByUsername(username)
            if (remoteEntities.isNotEmpty()) {
                gameRecordDao.insertAllGameRecord(remoteEntities)
            }
        }
    }

    private suspend fun currentUsername(): String? {
        return preferenceManager.usernameFlow.firstOrNull()?.takeIf { it.isNotBlank() }
    }
}

private data class RecordSnapshot(
    val username: String,
    val gameType: Int,
    val score: Int,
    val time: String,
    val learnedWords: List<String>
)

private fun List<GameRecordEntity>.isSameRecordSetAs(other: List<GameRecordEntity>): Boolean {
    return this.toSnapshots() == other.toSnapshots()
}

private fun List<GameRecordEntity>.toSnapshots(): List<RecordSnapshot> {
    return map { entity ->
        RecordSnapshot(
            username = entity.username,
            gameType = entity.gameType,
            score = entity.score,
            time = entity.time,
            learnedWords = entity.learnedWords.orEmpty()
        )
    }.sortedBy { snapshot ->
        "${snapshot.username}|${snapshot.gameType}|${snapshot.score}|${snapshot.time}|${snapshot.learnedWords.joinToString(",")}"
    }
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

private fun RemoteGameRecord.toGameRecordEntity(): GameRecordEntity {
    return GameRecordEntity().apply {
        username = this@toGameRecordEntity.username
        gameType = this@toGameRecordEntity.gameType
        score = this@toGameRecordEntity.score
        time = this@toGameRecordEntity.time
        learnedWords = this@toGameRecordEntity.learnedWords
    }
}
