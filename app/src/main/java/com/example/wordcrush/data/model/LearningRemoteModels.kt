package com.example.wordcrush.data.model

import com.example.wordcrush.constants.AppConstants

import com.google.gson.annotations.SerializedName

data class LearningWordResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("english") val english: String,
    @SerializedName("pronunciation") val pronunciation: String,
    @SerializedName("chinese") val chinese: String,
    @SerializedName("contentVersion") val contentVersion: Long = 1L,
    @SerializedName("masterCount") val masterCount: Int = 0,
    @SerializedName("mastered") val mastered: Boolean = false
)

data class LearningCatalogResponse(
    @SerializedName("items") val items: List<LearningWordResponse> = emptyList(),
    @SerializedName("page") val page: Int = 0,
    @SerializedName("size") val size: Int = 0,
    @SerializedName("total") val total: Long = 0L,
    @SerializedName("catalogVersion") val catalogVersion: Long = 1L
)

data class LearningProgressResponse(
    @SerializedName("wordId") val wordId: Int,
    @SerializedName("masterCount") val masterCount: Int = 0,
    @SerializedName("mastered") val mastered: Boolean = false,
    @SerializedName("version") val version: Long = 0L,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class LearningStateResponse(
    @SerializedName("dailyTarget") val dailyTarget: Int = AppConstants.Learning.DEFAULT_DAILY_WORD_TARGET,
    @SerializedName("planDate") val planDate: String = "",
    @SerializedName("todayWordIds") val todayWordIds: List<Int> = emptyList(),
    @SerializedName("todayWords") val todayWords: List<LearningWordResponse> = emptyList(),
    @SerializedName("completedCount") val completedCount: Int = 0,
    @SerializedName("availableUnmasteredCount") val availableUnmasteredCount: Int = 0,
    @SerializedName("allWordsMastered") val allWordsMastered: Boolean = false,
    @SerializedName("dailyCompleted") val dailyCompleted: Boolean = false,
    @SerializedName("canIncreaseDailyTarget") val canIncreaseDailyTarget: Boolean = false,
    @SerializedName("syncVersion") val syncVersion: Long = 0L,
    @SerializedName("progress") val progress: List<LearningProgressResponse> = emptyList()
)

data class DailyTargetRequest(
    @SerializedName("dailyTarget") val dailyTarget: Int
)

data class LearningMutationRequest(
    @SerializedName("mutationId") val mutationId: String,
    @SerializedName("wordId") val wordId: Int? = null,
    @SerializedName("operation") val operation: String,
    @SerializedName("masterCount") val masterCount: Int? = null,
    @SerializedName("dailyTarget") val dailyTarget: Int? = null,
    @SerializedName("clientAt") val clientAt: String? = null
)

data class LearningSyncRequest(
    @SerializedName("mutations") val mutations: List<LearningMutationRequest>
)

data class LearningSyncResponse(
    @SerializedName("acceptedMutationIds") val acceptedMutationIds: List<String> = emptyList(),
    @SerializedName("state") val state: LearningStateResponse? = null
)
