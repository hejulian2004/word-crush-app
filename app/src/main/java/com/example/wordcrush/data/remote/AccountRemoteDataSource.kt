package com.example.wordcrush.data.remote

import com.example.wordcrush.data.api.AuthenticatedAccountApi
import com.example.wordcrush.data.api.PublicAccountApi
import com.example.wordcrush.data.model.LoginRequest
import com.example.wordcrush.data.model.RegisterRequest
import com.example.wordcrush.data.model.UserResponse
import com.example.wordcrush.data.model.AvatarUploadResponse
import com.example.wordcrush.data.network.ApiCallExecutor
import com.example.wordcrush.data.network.ApiPayload
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRemoteDataSource @Inject constructor(
    private val publicApi: PublicAccountApi,
    private val authenticatedApi: AuthenticatedAccountApi,
    private val executor: ApiCallExecutor
) {
    suspend fun login(request: LoginRequest): ApiPayload<UserResponse> =
        executor.execute { publicApi.login(request) }

    suspend fun register(request: RegisterRequest): ApiPayload<UserResponse> =
        executor.execute { publicApi.register(request) }

    suspend fun checkToken(): ApiPayload<UserResponse> =
        executor.execute { authenticatedApi.checkToken() }

    suspend fun changePassword(
        username: String,
        oldPassword: String,
        newPassword: String
    ): ApiPayload<Unit> = executor.execute {
        authenticatedApi.changePassword(username, oldPassword, newPassword)
    }

    suspend fun uploadAvatar(
        username: String,
        file: MultipartBody.Part
    ): ApiPayload<AvatarUploadResponse> = executor.execute {
        authenticatedApi.uploadAvatar(username, file)
    }
}
