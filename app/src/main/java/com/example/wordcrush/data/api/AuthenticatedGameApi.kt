package com.example.wordcrush.data.api

import com.example.wordcrush.data.model.ApiResponse
import com.example.wordcrush.data.model.DeleteGameRecordRequest
import com.example.wordcrush.data.model.RemoteGameRecord
import com.example.wordcrush.data.model.SaveGameRecordRequest
import com.example.wordcrush.data.model.UsernameRequest
import com.example.wordcrush.data.network.ApiPaths
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthenticatedGameApi {
    @POST(ApiPaths.GameRecord.ADD)
    suspend fun saveGameRecord(
        @Body request: SaveGameRecordRequest
    ): Response<ApiResponse<Unit>>

    @POST(ApiPaths.GameRecord.DELETE)
    suspend fun deleteGameRecord(
        @Body request: DeleteGameRecordRequest
    ): Response<ApiResponse<Unit>>

    @POST(ApiPaths.GameRecord.ALL)
    suspend fun getAllGameRecords(
        @Body request: UsernameRequest
    ): Response<ApiResponse<List<RemoteGameRecord>>>
}
