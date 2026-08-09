package com.example.wordcrush.data.api

import com.example.wordcrush.data.model.ApiResponse
import com.example.wordcrush.data.model.DailyTargetRequest
import com.example.wordcrush.data.model.LearningCatalogResponse
import com.example.wordcrush.data.model.LearningStateResponse
import com.example.wordcrush.data.model.LearningSyncRequest
import com.example.wordcrush.data.model.LearningSyncResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface LearningApi {
    @GET("api/learning/catalog")
    suspend fun getCatalog(
        @Query("query") query: String? = null,
        @Query("mastered") mastered: Boolean? = null,
        @Query("ids") ids: List<Int>? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 1000
    ): Response<ApiResponse<LearningCatalogResponse>>

    @GET("api/learning/state")
    suspend fun getState(): Response<ApiResponse<LearningStateResponse>>

    @GET("api/learning/plan")
    suspend fun getPlan(): Response<ApiResponse<LearningStateResponse>>

    @PUT("api/learning/settings/daily-target")
    suspend fun updateDailyTarget(
        @Body request: DailyTargetRequest
    ): Response<ApiResponse<LearningStateResponse>>

    @POST("api/learning/sync")
    suspend fun sync(
        @Body request: LearningSyncRequest
    ): Response<ApiResponse<LearningSyncResponse>>
}
