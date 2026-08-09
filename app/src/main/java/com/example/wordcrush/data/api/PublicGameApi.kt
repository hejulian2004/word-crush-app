package com.example.wordcrush.data.api

import com.example.wordcrush.data.model.ApiResponse
import com.example.wordcrush.data.model.RankingRequest
import com.example.wordcrush.data.model.RemoteRankingItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PublicGameApi {
    @POST("api/getTopNRecord")
    suspend fun getTopRankings(
        @Body request: RankingRequest
    ): Response<ApiResponse<List<RemoteRankingItem>>>
}
