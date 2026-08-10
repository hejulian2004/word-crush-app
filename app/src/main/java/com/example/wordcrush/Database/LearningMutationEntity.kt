package com.example.wordcrush.Database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "LEARNING_MUTATIONS",
    indices = [Index(value = ["mutation_id"], unique = true)]
)
data class LearningMutationEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "mutation_id")
    var mutationId: String = "",

    @ColumnInfo(name = "word_id")
    var wordId: Int? = null,

    var operation: String = "",

    @ColumnInfo(name = "master_count")
    var masterCount: Int? = null,

    @ColumnInfo(name = "daily_target")
    var dailyTarget: Int? = null,

    @ColumnInfo(name = "client_at")
    var clientAt: String = "",

    @ColumnInfo(name = "created_at")
    var createdAt: Long = 0L
)
