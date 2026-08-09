package com.example.wordcrush.data.api

import com.example.wordcrush.data.model.ApiResponse
import com.example.wordcrush.data.model.AvatarUploadResponse
import com.example.wordcrush.data.model.UserResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface AuthenticatedAccountApi {
    @GET("api/user/checkToken")
    suspend fun checkToken(): Response<ApiResponse<UserResponse>>

    @POST("api/user/changePassword")
    suspend fun changePassword(
        @Query("username") username: String,
        @Query("oldPassword") oldPassword: String,
        @Query("newPassword") newPassword: String
    ): Response<ApiResponse<Unit>>

    @Multipart
    @POST("api/user/avatar")
    suspend fun uploadAvatar(
        @Query("username") username: String,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<AvatarUploadResponse>>
}
