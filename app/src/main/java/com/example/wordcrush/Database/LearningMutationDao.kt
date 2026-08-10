package com.example.wordcrush.Database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LearningMutationDao {
    @Insert
    fun insert(mutation: LearningMutationEntity): Long

    @Query("SELECT * FROM LEARNING_MUTATIONS ORDER BY id ASC LIMIT :limit")
    fun getPending(limit: Int): List<LearningMutationEntity>

    @Query("SELECT COUNT(*) FROM LEARNING_MUTATIONS")
    fun countPending(): Int

    @Delete
    fun deleteAll(mutations: List<LearningMutationEntity>)

    @Query("DELETE FROM LEARNING_MUTATIONS WHERE id = :id")
    fun deleteById(id: Long)
}
