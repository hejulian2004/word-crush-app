package com.example.wordcrush.data.api

import com.example.wordcrush.data.model.ApiResponse
import com.example.wordcrush.data.model.DailyTargetRequest
import com.example.wordcrush.data.model.LearningCatalogResponse
import com.example.wordcrush.data.model.LearningStateResponse
import com.example.wordcrush.data.model.LearningSyncRequest
import com.example.wordcrush.data.model.LearningSyncResponse
import com.example.wordcrush.data.network.ApiPaths
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface LearningApi {
    @GET(ApiPaths.Learning.CATALOG)
    suspend fun getCatalog(
        @Query("query") query: String? = null,
        @Query("mastered") mastered: Boolean? = null,
        @Query("ids") ids: List<Int>? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 1000
    ): Response<ApiResponse<LearningCatalogResponse>>

    @GET(ApiPaths.Learning.STATE)
    suspend fun getState(): Response<ApiResponse<LearningStateResponse>>

    @GET(ApiPaths.Learning.PLAN)
    suspend fun getPlan(): Response<ApiResponse<LearningStateResponse>>

    @PUT(ApiPaths.Learning.DAILY_TARGET)
    suspend fun updateDailyTarget(
        @Body request: DailyTargetRequest
    ): Response<ApiResponse<LearningStateResponse>>

    @POST(ApiPaths.Learning.SYNC)
    suspend fun sync(
        @Body request: LearningSyncRequest
    ): Response<ApiResponse<LearningSyncResponse>>
}
