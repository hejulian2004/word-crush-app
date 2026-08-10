package com.example.wordcrush.Database.GameRecord

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "GAME_RECORD")
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var gameType: Int = 0,
    var score: Int = 0,
    var time: String = "",
    var username: String = "",
    var learnedWords: List<String>? = null
)
