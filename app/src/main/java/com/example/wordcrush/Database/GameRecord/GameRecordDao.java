package com.example.wordcrush.Database.GameRecord;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface GameRecordDao {
    @Insert
    void insertGameRecord(GameRecordEntity gameRecordEntity);

    @Insert
    void insertAllGameRecord(List<GameRecordEntity> gameRecordEntities);

    @Update
    void update(GameRecordEntity gameRecordEntity);

    @Update
    void updateAll(List<GameRecordEntity> gameRecordEntities);

    @Query("SELECT * FROM GAME_RECORD WHERE username = :username ORDER BY time DESC, id DESC")
    List<GameRecordEntity> getAllGameRecords(String username);

    @Query("SELECT gameType, score FROM GAME_RECORD WHERE username = :username")
    List<GameTypeScore> getGameTypeAndScore(String username);

    @Query("DELETE FROM GAME_RECORD WHERE username = :username AND gameType = :gameType AND score = :score AND time = :time")
    int deleteRecordByGameTypeScoreTime(String username, int gameType, int score, String time);

    @Query("DELETE FROM GAME_RECORD WHERE username = :username")
    void deleteAllRecordsByUsername(String username);

    @Transaction
    default void replaceRecordsForUsername(String username, List<GameRecordEntity> records) {
        deleteAllRecordsByUsername(username);
        if (records != null && !records.isEmpty()) {
            insertAllGameRecord(records);
        }
    }

    @Query("DELETE FROM GAME_RECORD")
    void deleteAllRecords();

    @Insert
    void insertAllRecords(List<GameRecordEntity> wordEntities);
}

