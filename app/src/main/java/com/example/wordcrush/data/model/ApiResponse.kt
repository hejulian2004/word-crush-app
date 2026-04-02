package com.example.wordcrush.data.model

import com.google.gson.annotations.SerializedName

/**
 * 统一的 API 响应模型
 */
data class ApiResponse<T>(
    @SerializedName("code")
    val code: Int = 0,
    @SerializedName("msg")
    val msg: String = "",
    @SerializedName("data")
    val data: T? = null
) {
    fun isSuccess(): Boolean = code == 200
}

/**
 * 登录请求
 */
data class LoginRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)

/**
 * 注册请求
 */
data class RegisterRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)

/**
 * 用户响应数据
 */
data class UserResponse(
    @SerializedName("username")
    val username: String,
    @SerializedName("uid")
    val uid: String,
    @SerializedName("token")
    val token: String
)
