package com.example.wordcrush.data.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

object NetworkConfig {
    const val API_BASE_URL = "https://txy.hejulian.org/word-crush/"
    const val AUDIO_BASE_URL = "https://dict.youdao.com/"
    const val WEBSOCKET_BASE_URL = "wss://txy.hejulian.org/word-crush/"

    fun websocketUrl(path: String): HttpUrl {
        val normalizedPath = path.trim().trimStart('/')
        // OkHttp's WebSocket request URL is represented as http/https and is
        // upgraded to ws/wss by the WebSocket transport internally.
        val httpBaseUrl = WEBSOCKET_BASE_URL
            .replaceFirst("wss://", "https://")
        return httpBaseUrl.toHttpUrl().newBuilder()
            .addPathSegments(normalizedPath)
            .build()
    }
}
