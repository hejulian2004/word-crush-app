package com.example.wordcrush.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface LearningMutationDao {
    @Insert
    long insert(LearningMutationEntity mutation);

    @Query("SELECT * FROM LEARNING_MUTATIONS ORDER BY id ASC LIMIT :limit")
    List<LearningMutationEntity> getPending(int limit);

    @Query("SELECT COUNT(*) FROM LEARNING_MUTATIONS")
    int countPending();

    @Delete
    void deleteAll(List<LearningMutationEntity> mutations);

    @Query("DELETE FROM LEARNING_MUTATIONS WHERE id = :id")
    void deleteById(long id);
}
