package com.example.wordcrush.Database.GameRecordDatabase;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.wordcrush.Database.WordDatabse.WordEntity;

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

    @Query("SELECT * FROM GAME_RECORD ORDER BY ID DESC")
    List<GameRecordEntity> getAllGameRecords();
}
