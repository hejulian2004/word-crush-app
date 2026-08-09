package com.example.wordcrush.data.network

import com.example.wordcrush.data.model.ApiResponse
import com.wordcrush.api.ApiCode
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

data class ApiPayload<T>(
    val code: Int,
    val data: T?,
    val message: String
)

@Singleton
class ApiCallExecutor @Inject constructor(
    private val gson: Gson
) {
    suspend fun <T> execute(
        call: suspend () -> Response<ApiResponse<T>>
    ): ApiPayload<T> {
        val response = try {
            call()
        } catch (error: CancellationException) {
            throw error
        } catch (error: SocketTimeoutException) {
            throw NetworkException.Timeout
        } catch (error: IOException) {
            throw NetworkException.Offline
        } catch (error: Exception) {
            throw NetworkException.Serialization(error)
        }

        if (!response.isSuccessful || response.code() != ApiCode.SUCCESS.value()) {
            throw response.toNetworkException()
        }

        val body = response.body()
            ?: throw NetworkException.Serialization(IllegalStateException("Empty response body"))
        if (!body.isSuccess()) {
            throw NetworkException.Server(
                httpStatusCode = response.code(),
                code = body.code,
                detail = body.msg.ifBlank { "Request failed." }
            )
        }
        return ApiPayload(
            code = body.code,
            data = body.data,
            message = body.msg.ifBlank { "success" }
        )
    }

    private fun <T> Response<ApiResponse<T>>.toNetworkException(): NetworkException {
        val errorText = errorBody()?.use { it.string() }.orEmpty().trim()
        val error = runCatching {
            gson.fromJson(errorText, ErrorPayload::class.java)
        }.getOrNull()
        val responseCode = error?.code?.takeIf { it > 0 } ?: code()
        val detail = error?.msg?.takeIf { it.isNotBlank() }
            ?: errorText.takeIf { it.isNotBlank() }
            ?: message().takeIf { it.isNotBlank() }
            ?: "Request failed."
        return NetworkException.Server(code(), responseCode, detail)
    }

    private data class ErrorPayload(
        @SerializedName("code") val code: Int = 0,
        @SerializedName("msg") val msg: String = ""
    )
}

fun <T> ApiPayload<T>.requireData(fallbackMessage: String = "Response data is missing."): T {
    return data ?: throw IllegalStateException(message.ifBlank { fallbackMessage })
}
