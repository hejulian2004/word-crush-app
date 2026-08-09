package com.example.wordcrush.data.network

import com.example.wordcrush.constants.AppStrings
import java.io.IOException

sealed class NetworkException(
    message: String,
    cause: Throwable? = null
) : IOException(message, cause) {
    data class Server(
        val httpStatusCode: Int,
        val code: Int,
        val detail: String
    ) : NetworkException(detail)

    data object Offline : NetworkException(AppStrings.Errors.NETWORK_OFFLINE)

    data object Timeout : NetworkException(AppStrings.Errors.NETWORK_TIMEOUT)

    data class Serialization(
        val original: Throwable
    ) : NetworkException(AppStrings.Errors.NETWORK_SERIALIZATION, original)
}
