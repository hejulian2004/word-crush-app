package com.example.wordcrush.Database.Word;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WordDao {
    // 插入单个WordEntity
    @Insert
    void insert(WordEntity wordEntity);

    // 批量插入
    @Insert
    void insertAll(List<WordEntity> wordEntities);

    // 更新WordEntity
    @Update
    void update(WordEntity wordEntity);

    @Update
    void updateAll(List<WordEntity> wordEntityList);

    // 查询所有的单词
    @Query("SELECT * FROM WORDS")
    List<WordEntity> getAllWords();

    // 根据ID查询WordEntity
    @Query("SELECT * FROM WORDS WHERE id = :id")
    WordEntity getWordById(int id);

    @Query("SELECT * FROM WORDS WHERE english = :english")
    WordEntity getWordByEnglish(String english);

    // 删除单个WordEntity
    @Query("DELETE FROM WORDS WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM WORDS WHERE english LIKE '%' || :search || '%' OR chinese LIKE '%' || :search || '%' OR pronunciation LIKE '%' || :search || '%'")
    List<WordEntity> searchWords(String search);

    @Query("SELECT * FROM WORDS WHERE isMaster = :isMastered")
    List<WordEntity> searchWords(Boolean isMastered);

}
