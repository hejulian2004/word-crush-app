package com.example.wordcrush.utils

class AvatarUrlFactory(
    private val baseUrl: String
) {
    fun create(username: String): String {
        val normalizedBase = if (baseUrl.endsWith("/")) baseUrl.dropLast(1) else baseUrl
        return "$normalizedBase/static/avatars/$username.jpg?t=${System.currentTimeMillis()}"
    }
}
