package com.example.wordcrush.data.model

data class RankingItem(
    val username: String,
    val score: Int,
    val time: String,
    val avatarUrl: String,
    val avatarVersion: Long = 0L
)
