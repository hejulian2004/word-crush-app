package com.example.wordcrush.Database.Word

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "WORDS")
data class WordEntity(
    @PrimaryKey
    var id: Int = 0,
    var english: String = "",
    var pronunciation: String = "",
    var chinese: String = "",
    var isMaster: Boolean = false,
    var masterCount: Int = 0
)
