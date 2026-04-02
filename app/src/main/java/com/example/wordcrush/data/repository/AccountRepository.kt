package com.example.wordcrush.data.repository

import com.example.wordcrush.data.api.AccountApi
import com.example.wordcrush.data.local.PreferenceManager
import com.example.wordcrush.data.model.LoginRequest
import com.example.wordcrush.data.model.RegisterRequest
import com.example.wordcrush.utils.LogUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountApi: AccountApi,
    private val preferenceManager: PreferenceManager
) {
    private companion object {
        const val ADMIN_USERNAME = "admin"
        const val ADMIN_PASSWORD = "123456"
        const val ADMIN_TOKEN = "local_admin_token"
        const val ADMIN_UID = "0"
    }

    suspend fun login(username: String, password: String): Result<String> {
        return if (username == ADMIN_USERNAME && password == ADMIN_PASSWORD) {
            preferenceManager.saveUserInfo(
                token = ADMIN_TOKEN,
                username = ADMIN_USERNAME,
                uid = ADMIN_UID
            )
            LogUtils.d("Local admin login success: $ADMIN_USERNAME")
            Result.success("Admin login success")
        } else {
            LogUtils.e("Local admin login failed for: $username")
            Result.failure(IllegalStateException("Invalid admin username or password"))
        }
    }

    suspend fun checkToken(token: String): Result<String> {
        return if (token == ADMIN_TOKEN) {
            preferenceManager.saveUserInfo(
                token = ADMIN_TOKEN,
                username = ADMIN_USERNAME,
                uid = ADMIN_UID
            )
            LogUtils.d("Local admin token validated")
            Result.success("Admin session valid")
        } else {
            LogUtils.e("Local admin token validation failed")
            Result.failure(IllegalStateException("Invalid local admin session"))
        }
    }

    suspend fun register(username: String, password: String): Result<String> {
        return try {
            val response = accountApi.register(RegisterRequest(username, password))
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.isSuccess()) {
                    LogUtils.d("Register success")
                    Result.success(apiResponse.msg)
                } else {
                    LogUtils.e("Register failed: ${apiResponse.msg}")
                    Result.failure(IllegalStateException(apiResponse.msg))
                }
            } else {
                LogUtils.e("Register request failed")
                Result.failure(IllegalStateException("Network request failed"))
            }
        } catch (error: Exception) {
            LogUtils.e("Register error", error)
            Result.failure(error)
        }
    }

    suspend fun changePassword(username: String, oldPassword: String, newPassword: String): Result<String> {
        return try {
            val response = accountApi.changePassword(username, oldPassword, newPassword)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.isSuccess()) {
                    LogUtils.d("Password changed")
                    Result.success(apiResponse.msg)
                } else {
                    LogUtils.e("Change password failed: ${apiResponse.msg}")
                    Result.failure(IllegalStateException(apiResponse.msg))
                }
            } else {
                LogUtils.e("Change password request failed")
                Result.failure(IllegalStateException("Network request failed"))
            }
        } catch (error: Exception) {
            LogUtils.e("Change password error", error)
            Result.failure(error)
        }
    }

    suspend fun logout() {
        preferenceManager.clear()
        LogUtils.d("Logged out")
    }
}
