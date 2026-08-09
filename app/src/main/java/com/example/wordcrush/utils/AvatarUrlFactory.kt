package com.example.wordcrush.utils

import android.net.Uri
import com.example.wordcrush.data.network.ApiPaths

class AvatarUrlFactory(
    private val baseUrl: String,
    private val encodeUsername: (String) -> String = { value -> Uri.encode(value) }
) {
    fun create(username: String, avatarVersion: Long = 0L): String {
        val normalizedBase = if (baseUrl.endsWith("/")) baseUrl.dropLast(1) else baseUrl
        val encodedUsername = encodeUsername(username)
        val avatarPath = "${ApiPaths.Account.AVATAR}/$encodedUsername"
        return if (avatarVersion > 0L) {
            "$normalizedBase/$avatarPath?v=$avatarVersion"
        } else {
            "$normalizedBase/$avatarPath"
        }
    }
}
