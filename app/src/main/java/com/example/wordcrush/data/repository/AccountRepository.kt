package com.example.wordcrush.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.wordcrush.data.api.AccountApi
import com.example.wordcrush.data.cache.AvatarCacheStore
import com.example.wordcrush.data.local.PreferenceManager
import com.example.wordcrush.data.model.ApiResponse
import com.example.wordcrush.data.model.AvatarUploadResponse
import com.example.wordcrush.data.model.LoginRequest
import com.example.wordcrush.data.model.RegisterRequest
import com.example.wordcrush.data.model.UserResponse
import com.example.wordcrush.utils.AppStateManager
import com.example.wordcrush.utils.AvatarUrlFactory
import com.example.wordcrush.utils.LogUtils
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

@Singleton
class AccountRepository @Inject constructor(
    private val accountApi: AccountApi,
    private val preferenceManager: PreferenceManager,
    private val appStateManager: AppStateManager,
    private val avatarCacheStore: AvatarCacheStore,
    @ApplicationContext private val context: Context
) {
    private companion object {
        const val MAX_AVATAR_BYTES = 1024 * 1024
        const val MAX_BITMAP_EDGE = 1280
        const val MIN_BITMAP_EDGE = 320
    }

    private val gson = Gson()

    suspend fun login(username: String, password: String): Result<String> {
        return runCatching {
            val response = accountApi.login(LoginRequest(username, password))
            val apiResponse = requireSuccessfulBody(response)
            val user = apiResponse.data ?: throw IllegalStateException(apiResponse.msg.ifBlank { "Login failed." })
            persistUserSession(user)
            LogUtils.d("Login success: ${user.username}")
            apiResponse.msg
        }.onFailure { error ->
            LogUtils.e("Login error", error)
        }
    }

    suspend fun checkToken(token: String): Result<String> {
        return runCatching {
            val response = accountApi.checkToken(token)
            val apiResponse = requireSuccessfulBody(response)
            val user = apiResponse.data ?: throw IllegalStateException(apiResponse.msg.ifBlank { "Session validation failed." })
            persistUserSession(user)
            LogUtils.d("Token validated for: ${user.username}")
            apiResponse.msg
        }.onFailure { error ->
            LogUtils.e("Check token error", error)
        }
    }

    suspend fun register(username: String, password: String): Result<String> {
        return runCatching {
            val response = accountApi.register(RegisterRequest(username, password))
            val apiResponse = requireSuccessfulBody(response)
            LogUtils.d("Register success: $username")
            apiResponse.msg
        }.onFailure { error ->
            LogUtils.e("Register error", error)
        }
    }

    suspend fun changePassword(username: String, oldPassword: String, newPassword: String): Result<String> {
        return runCatching {
            val response = accountApi.changePassword(username, oldPassword, newPassword)
            val apiResponse = requireSuccessfulBody(response)
            LogUtils.d("Password changed for: $username")
            apiResponse.msg
        }.onFailure { error ->
            LogUtils.e("Change password error", error)
        }
    }

    suspend fun uploadAvatar(username: String, uri: Uri): Result<String> {
        return runCatching {
            val response = accountApi.uploadAvatar(username, createAvatarPart(uri))
            val apiResponse = requireSuccessfulBody(response)
            val avatarResponse = apiResponse.data ?: AvatarUploadResponse(
                username = username,
                avatarUrl = "/api/user/avatar/$username",
                avatarVersion = System.currentTimeMillis()
            )
            val avatarUrl = resolveAvatarUrl(avatarResponse)
            preferenceManager.saveAvatarUrl(avatarUrl)
            LogUtils.d("Avatar uploaded for: $username")
            avatarUrl
        }.onFailure { error ->
            LogUtils.e("Upload avatar error", error)
        }
    }

    suspend fun logout() {
        preferenceManager.clear()
        avatarCacheStore.clear()
        LogUtils.d("Logged out")
    }

    private suspend fun persistUserSession(user: UserResponse) {
        preferenceManager.saveUserInfo(
            token = user.token,
            username = user.username,
            uid = user.uid
        )
    }

    private fun createAvatarPart(uri: Uri): MultipartBody.Part {
        val bitmap = decodeAvatarBitmap(uri)
        val bytes = compressAvatarBitmap(bitmap)
        val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", resolveFileName(uri), requestBody)
    }

    private fun decodeAvatarBitmap(uri: Uri): Bitmap {
        val contentResolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val boundsStream = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Unable to read avatar file.")
        boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IllegalStateException("Unsupported avatar image.")
        }

        val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight)
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decodeStream = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Unable to decode avatar file.")
        return decodeStream.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        } ?: throw IllegalStateException("Unsupported avatar image.")
    }

    private fun calculateInSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        var currentWidth = width
        var currentHeight = height
        while (currentWidth > MAX_BITMAP_EDGE || currentHeight > MAX_BITMAP_EDGE) {
            currentWidth /= 2
            currentHeight /= 2
            sampleSize *= 2
        }
        return sampleSize.coerceAtLeast(1)
    }

    private fun compressAvatarBitmap(bitmap: Bitmap): ByteArray {
        var currentBitmap = scaleBitmapIfNeeded(bitmap)
        var bytes = compressAsJpeg(currentBitmap)

        while (bytes.size > MAX_AVATAR_BYTES && currentBitmap.width > MIN_BITMAP_EDGE && currentBitmap.height > MIN_BITMAP_EDGE) {
            val nextWidth = (currentBitmap.width * 0.8f).toInt().coerceAtLeast(MIN_BITMAP_EDGE)
            val nextHeight = (currentBitmap.height * 0.8f).toInt().coerceAtLeast(MIN_BITMAP_EDGE)
            currentBitmap = Bitmap.createScaledBitmap(currentBitmap, nextWidth, nextHeight, true)
            bytes = compressAsJpeg(currentBitmap)
        }

        return bytes
    }

    private fun scaleBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        val maxEdge = maxOf(bitmap.width, bitmap.height)
        if (maxEdge <= MAX_BITMAP_EDGE) {
            return bitmap
        }
        val scale = MAX_BITMAP_EDGE.toFloat() / maxEdge.toFloat()
        val width = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val height = (bitmap.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun compressAsJpeg(bitmap: Bitmap): ByteArray {
        val output = ByteArrayOutputStream()
        val qualities = intArrayOf(90, 82, 74, 66, 58, 50, 42, 34)
        var lastBytes = ByteArray(0)
        for (quality in qualities) {
            output.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
            lastBytes = output.toByteArray()
            if (lastBytes.size <= MAX_AVATAR_BYTES) {
                return lastBytes
            }
        }
        return lastBytes
    }

    private fun resolveFileName(uri: Uri): String {
        val baseName = uri.lastPathSegment
            ?.substringAfterLast('/')
            ?.substringBeforeLast('.')
            ?.takeIf { it.isNotBlank() }
            ?: "avatar"
        return "$baseName.jpg"
    }

    private fun resolveAvatarUrl(response: AvatarUploadResponse): String {
        if (response.avatarUrl.startsWith("http://") || response.avatarUrl.startsWith("https://")) {
            return response.avatarUrl
        }
        if (response.avatarVersion > 0L) {
            return avatarCacheStore.resolve(response.username, response.avatarVersion)
        }
        val normalizedBase = if (appStateManager.domain.endsWith("/")) {
            appStateManager.domain.dropLast(1)
        } else {
            appStateManager.domain
        }
        return "$normalizedBase${response.avatarUrl}"
    }

    private fun <T> requireSuccessfulBody(response: Response<ApiResponse<T>>): ApiResponse<T> {
        val body = response.body()
        if (response.isSuccessful && body != null) {
            if (body.isSuccess()) {
                return body
            }
            throw IllegalStateException(body.msg.ifBlank { "Request failed." })
        }
        throw IllegalStateException(extractErrorMessage(response) ?: "Network request failed")
    }

    private fun extractErrorMessage(response: Response<*>): String? {
        val errorText = response.errorBody()?.use { it.string() }?.trim().orEmpty()
        if (errorText.isBlank()) {
            return null
        }
        return runCatching {
            gson.fromJson(errorText, ErrorResponse::class.java).msg
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: errorText
    }

    private data class ErrorResponse(
        val code: Int = 0,
        val msg: String = ""
    )
}
