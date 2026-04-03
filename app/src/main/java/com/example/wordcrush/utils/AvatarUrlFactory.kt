package com.example.wordcrush.utils

import android.net.Uri

class AvatarUrlFactory(
    private val baseUrl: String
) {
    fun create(username: String, avatarVersion: Long = 0L): String {
        val normalizedBase = if (baseUrl.endsWith("/")) baseUrl.dropLast(1) else baseUrl
        val encodedUsername = Uri.encode(username)
        return if (avatarVersion > 0L) {
            "$normalizedBase/api/user/avatar/$encodedUsername?v=$avatarVersion"
        } else {
            "$normalizedBase/api/user/avatar/$encodedUsername"
        }
    }
}
