package com.example.wordcrush.data.model

import com.wordcrush.api.ApiCode
import com.google.gson.annotations.SerializedName

/**
 * 缁熶竴鐨?API 鍝嶅簲妯″瀷
 */
data class ApiResponse<T>(
    @SerializedName("code")
    val code: Int = 0,
    @SerializedName("msg")
    val msg: String = "",
    @SerializedName("data")
    val data: T? = null
) {
    fun isSuccess(): Boolean = code == ApiCode.SUCCESS.value()
}

/**
 * 鐧诲綍璇锋眰
 */
data class LoginRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)

/**
 * 娉ㄥ唽璇锋眰
 */
data class RegisterRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)

/**
 * 鐢ㄦ埛鍝嶅簲鏁版嵁
 */
data class UserResponse(
    @SerializedName("username")
    val username: String,
    @SerializedName("uid")
    val uid: String,
    @SerializedName("token")
    val token: String
)

data class AvatarUploadResponse(
    @SerializedName("username")
    val username: String,
    @SerializedName("avatarUrl")
    val avatarUrl: String,
    @SerializedName("avatarVersion")
    val avatarVersion: Long = 0L
)
