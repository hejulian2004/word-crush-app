package com.example.wordcrush.Database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "LEARNING_MUTATIONS",
        indices = {@Index(value = {"mutation_id"}, unique = true)}
)
public class LearningMutationEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "mutation_id")
    private String mutationId;

    @ColumnInfo(name = "word_id")
    private Integer wordId;

    private String operation;

    @ColumnInfo(name = "master_count")
    private Integer masterCount;

    @ColumnInfo(name = "daily_target")
    private Integer dailyTarget;

    @ColumnInfo(name = "client_at")
    private String clientAt;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public LearningMutationEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMutationId() {
        return mutationId;
    }

    public void setMutationId(String mutationId) {
        this.mutationId = mutationId;
    }

    public Integer getWordId() {
        return wordId;
    }

    public void setWordId(Integer wordId) {
        this.wordId = wordId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Integer getMasterCount() {
        return masterCount;
    }

    public void setMasterCount(Integer masterCount) {
        this.masterCount = masterCount;
    }

    public Integer getDailyTarget() {
        return dailyTarget;
    }

    public void setDailyTarget(Integer dailyTarget) {
        this.dailyTarget = dailyTarget;
    }

    public String getClientAt() {
        return clientAt;
    }

    public void setClientAt(String clientAt) {
        this.clientAt = clientAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
