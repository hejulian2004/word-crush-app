package com.example.wordcrush.data.model

data class WordItem(
    val id: Int,
    val english: String,
    val pronunciation: String,
    val chinese: String,
    val isMastered: Boolean,
    val masterCount: Int
)
