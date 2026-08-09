package com.example.wordcrush.data.network

import com.example.wordcrush.data.model.ApiResponse
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

data class ApiPayload<T>(
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

        if (!response.isSuccessful) {
            throw response.toNetworkException()
        }

        val body = response.body()
            ?: throw NetworkException.Serialization(IllegalStateException("Empty response body"))
        if (!body.isSuccess()) {
            throw NetworkException.Business(body.code, body.msg.ifBlank { "Request failed." })
        }
        return ApiPayload(body.data, body.msg)
    }

    private fun <T> Response<ApiResponse<T>>.toNetworkException(): NetworkException {
        if (code() == 401) {
            return NetworkException.Unauthorized
        }

        val errorText = errorBody()?.use { it.string() }.orEmpty().trim()
        val error = runCatching {
            gson.fromJson(errorText, ErrorPayload::class.java)
        }.getOrNull()
        val detail = error?.msg?.takeIf { it.isNotBlank() }
            ?: errorText.takeIf { it.isNotBlank() }
            ?: message().takeIf { it.isNotBlank() }
        return NetworkException.Http(code(), detail)
    }

    private data class ErrorPayload(
        @SerializedName("code") val code: Int = 0,
        @SerializedName("msg") val msg: String = ""
    )
}

fun <T> ApiPayload<T>.requireData(fallbackMessage: String = "Response data is missing."): T {
    return data ?: throw IllegalStateException(message.ifBlank { fallbackMessage })
}
