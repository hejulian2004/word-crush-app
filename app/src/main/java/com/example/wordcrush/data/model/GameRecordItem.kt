package com.example.wordcrush.data.model

data class GameRecordItem(
    val id: Int = 0,
    val gameType: Int,
    val time: String,
    val score: Int,
    val learnedWords: List<String>
) {
    val gameTypeLabel: String
        get() = if (gameType == 0) "Match Challenge" else "Timed Match"

    val wordProgress: List<RecordWordProgress>
        get() = RecordWordProgressCodec.decodeAll(learnedWords)
}
