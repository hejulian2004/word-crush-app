package com.example.wordcrush.data.model

import com.example.wordcrush.constants.AppStrings

data class GameRecordItem(
    val id: Int = 0,
    val gameType: Int,
    val time: String,
    val score: Int,
    val learnedWords: List<String>
) {
    val gameTypeLabel: String
        get() = AppStrings.Records.modeTitle(gameType == 0)

    val wordProgress: List<RecordWordProgress>
        get() = RecordWordProgressCodec.decodeAll(learnedWords)
}
