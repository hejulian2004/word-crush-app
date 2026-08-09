package com.example.wordcrush.data.remote

import com.example.wordcrush.data.api.LearningApi
import com.example.wordcrush.data.model.DailyTargetRequest
import com.example.wordcrush.data.model.LearningCatalogResponse
import com.example.wordcrush.data.model.LearningStateResponse
import com.example.wordcrush.data.model.LearningSyncRequest
import com.example.wordcrush.data.model.LearningSyncResponse
import com.example.wordcrush.data.network.ApiCallExecutor
import com.example.wordcrush.data.network.ApiPayload
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LearningRemoteDataSource @Inject constructor(
    private val api: LearningApi,
    private val executor: ApiCallExecutor
) {
    suspend fun getCatalog(
        query: String? = null,
        mastered: Boolean? = null,
        ids: List<Int>? = null,
        page: Int = 0,
        size: Int = 1000
    ): ApiPayload<LearningCatalogResponse> = executor.execute {
        api.getCatalog(query, mastered, ids, page, size)
    }

    suspend fun getState(): ApiPayload<LearningStateResponse> = executor.execute { api.getState() }

    suspend fun getPlan(): ApiPayload<LearningStateResponse> = executor.execute { api.getPlan() }

    suspend fun updateDailyTarget(target: Int): ApiPayload<LearningStateResponse> = executor.execute {
        api.updateDailyTarget(DailyTargetRequest(target))
    }

    suspend fun sync(request: LearningSyncRequest): ApiPayload<LearningSyncResponse> = executor.execute {
        api.sync(request)
    }
}
