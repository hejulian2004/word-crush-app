package com.example.wordcrush.data.api

import com.example.wordcrush.data.model.DeleteGameRecordRequest
import com.example.wordcrush.data.model.LegacyApiResponse
import com.example.wordcrush.data.model.RankingRequest
import com.example.wordcrush.data.model.RemoteGameRecord
import com.example.wordcrush.data.model.RemoteRankingItem
import com.example.wordcrush.data.model.SaveGameRecordRequest
import com.example.wordcrush.data.model.UsernameRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GameRecordApi {

    @POST("/api/getTopNRecord")
    suspend fun getTopRankings(
        @Body request: RankingRequest
    ): Response<LegacyApiResponse<List<RemoteRankingItem>>>

    @POST("/api/addGameRecord")
    suspend fun saveGameRecord(
        @Body request: SaveGameRecordRequest
    ): Response<LegacyApiResponse<String>>

    @POST("/api/deleteGameRecord")
    suspend fun deleteGameRecord(
        @Body request: DeleteGameRecordRequest
    ): Response<LegacyApiResponse<String>>

    @POST("/api/getAllGameRecord")
    suspend fun getAllGameRecords(
        @Body request: UsernameRequest
    ): Response<LegacyApiResponse<List<RemoteGameRecord>>>
}
