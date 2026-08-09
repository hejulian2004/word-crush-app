package com.example.wordcrush.data.network

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

    data object Offline : NetworkException("Network connection is unavailable.")

    data object Timeout : NetworkException("Network request timed out.")

    data class Serialization(
        val original: Throwable
    ) : NetworkException("Unable to read server response.", original)
}
