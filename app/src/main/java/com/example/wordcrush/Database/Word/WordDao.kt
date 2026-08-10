package com.example.wordcrush.Database.Word

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WordDao {
    // 插入单个WordEntity
    @Insert
    fun insert(wordEntity: WordEntity)

    // 批量插入
    @Insert
    fun insertAll(wordEntities: List<WordEntity>)

    // 更新WordEntity
    @Update
    fun update(wordEntity: WordEntity)

    @Update
    fun updateAll(wordEntityList: List<WordEntity>)

    // 查询所有的单词
    @Query("SELECT * FROM WORDS")
    fun getAllWords(): List<WordEntity>

    // 根据ID查询WordEntity
    @Query("SELECT * FROM WORDS WHERE id = :id")
    fun getWordById(id: Int): WordEntity?

    @Query("SELECT * FROM WORDS WHERE id IN (:ids)")
    fun getWordsByIds(ids: Set<Int>): List<WordEntity>

    @Query("SELECT * FROM WORDS WHERE english = :english")
    fun getWordByEnglish(english: String): WordEntity?

    // 删除单个WordEntity
    @Query("DELETE FROM WORDS WHERE id = :id")
    fun deleteById(id: Int)

    @Query("SELECT * FROM WORDS WHERE english LIKE '%' || :search || '%' OR chinese LIKE '%' || :search || '%' OR pronunciation LIKE '%' || :search || '%'")
    fun searchWords(search: String): List<WordEntity>

    @Query("SELECT * FROM WORDS WHERE isMaster = :isMastered")
    fun searchWords(isMastered: Boolean): List<WordEntity>

    @Query("DELETE FROM WORDS WHERE id NOT IN (:ids)")
    fun deleteNotInIds(ids: List<Int>)

    @Query("DELETE FROM WORDS")
    fun deleteAll()
}
