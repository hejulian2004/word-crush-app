package com.example.wordcrush.data.api

import com.example.wordcrush.data.model.ApiResponse
import com.example.wordcrush.data.model.LoginRequest
import com.example.wordcrush.data.model.RegisterRequest
import com.example.wordcrush.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PublicAccountApi {
    @POST("api/user/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<UserResponse>>

    @POST("api/user/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<UserResponse>>
}
