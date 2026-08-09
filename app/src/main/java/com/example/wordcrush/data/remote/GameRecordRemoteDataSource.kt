package com.example.wordcrush.data.remote

import com.example.wordcrush.data.api.AuthenticatedGameApi
import com.example.wordcrush.data.api.PublicGameApi
import com.example.wordcrush.data.model.DeleteGameRecordRequest
import com.example.wordcrush.data.model.RankingRequest
import com.example.wordcrush.data.model.RemoteGameRecord
import com.example.wordcrush.data.model.RemoteRankingItem
import com.example.wordcrush.data.model.SaveGameRecordRequest
import com.example.wordcrush.data.model.UsernameRequest
import com.example.wordcrush.data.network.ApiCallExecutor
import com.example.wordcrush.data.network.ApiPayload
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRecordRemoteDataSource @Inject constructor(
    private val publicApi: PublicGameApi,
    private val authenticatedApi: AuthenticatedGameApi,
    private val executor: ApiCallExecutor
) {
    suspend fun getTopRankings(request: RankingRequest): ApiPayload<List<RemoteRankingItem>> =
        executor.execute { publicApi.getTopRankings(request) }

    suspend fun saveGameRecord(request: SaveGameRecordRequest): ApiPayload<Unit> =
        executor.execute { authenticatedApi.saveGameRecord(request) }

    suspend fun deleteGameRecord(request: DeleteGameRecordRequest): ApiPayload<Unit> =
        executor.execute { authenticatedApi.deleteGameRecord(request) }

    suspend fun getAllGameRecords(request: UsernameRequest): ApiPayload<List<RemoteGameRecord>> =
        executor.execute { authenticatedApi.getAllGameRecords(request) }
}
