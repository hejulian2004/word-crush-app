package com.example.wordcrush.Database.GameRecord

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
abstract class GameRecordDao {
    @Insert
    abstract fun insertGameRecord(gameRecordEntity: GameRecordEntity)

    @Insert
    abstract fun insertAllGameRecord(gameRecordEntities: List<GameRecordEntity>)

    @Update
    abstract fun update(gameRecordEntity: GameRecordEntity)

    @Update
    abstract fun updateAll(gameRecordEntities: List<GameRecordEntity>)

    @Query("SELECT * FROM GAME_RECORD WHERE username = :username ORDER BY time DESC, id DESC")
    abstract fun getAllGameRecords(username: String): List<GameRecordEntity>

    @Query("SELECT gameType, score FROM GAME_RECORD WHERE username = :username")
    abstract fun getGameTypeAndScore(username: String): List<GameTypeScore>

    @Query("DELETE FROM GAME_RECORD WHERE username = :username AND gameType = :gameType AND score = :score AND time = :time")
    abstract fun deleteRecordByGameTypeScoreTime(
        username: String,
        gameType: Int,
        score: Int,
        time: String
    ): Int

    @Query("DELETE FROM GAME_RECORD WHERE username = :username")
    abstract fun deleteAllRecordsByUsername(username: String)

    @Transaction
    open fun replaceRecordsForUsername(username: String, records: List<GameRecordEntity>) {
        deleteAllRecordsByUsername(username)
        if (records.isNotEmpty()) {
            insertAllGameRecord(records)
        }
    }

    @Query("DELETE FROM GAME_RECORD")
    abstract fun deleteAllRecords()

    @Insert
    abstract fun insertAllRecords(wordEntities: List<GameRecordEntity>)
}
