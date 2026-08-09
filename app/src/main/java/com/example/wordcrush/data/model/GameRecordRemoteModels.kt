package com.example.wordcrush.data.model

import com.google.gson.annotations.SerializedName

data class RankingRequest(
    @SerializedName("gameType")
    val gameType: Int,
    @SerializedName("topN")
    val topN: Int
)

data class UsernameRequest(
    @SerializedName("username")
    val username: String
)

data class DeleteGameRecordRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("gameType")
    val gameType: Int,
    @SerializedName("score")
    val score: Int,
    @SerializedName("time")
    val time: String
)

data class SaveGameRecordRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("gameType")
    val gameType: Int,
    @SerializedName("score")
    val score: Int,
    @SerializedName("time")
    val time: String,
    @SerializedName("learnedWords")
    val learnedWords: List<String>
)

data class RemoteRankingItem(
    @SerializedName("username")
    val username: String,
    @SerializedName("score")
    val score: Int,
    @SerializedName("time")
    val time: String,
    @SerializedName("avatarVersion")
    val avatarVersion: Long = 0L
)

data class RemoteGameRecord(
    @SerializedName("username")
    val username: String,
    @SerializedName("gameType")
    val gameType: Int,
    @SerializedName("score")
    val score: Int,
    @SerializedName("time")
    val time: String,
    @SerializedName("learnedWords")
    val learnedWords: List<String> = emptyList()
)
