package com.example.wordcrush.data.cache

import com.example.wordcrush.data.network.NetworkConfig
import com.example.wordcrush.utils.AvatarUrlFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvatarCacheStore @Inject constructor() {
    private companion object {
        const val MAX_CACHED_USERS = 50
    }

    private val entries = object : LinkedHashMap<String, CachedAvatar>(MAX_CACHED_USERS, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CachedAvatar>?): Boolean {
            return size > MAX_CACHED_USERS
        }
    }

    @Synchronized
    fun resolve(username: String, avatarVersion: Long): String {
        if (username.isBlank() || avatarVersion <= 0L) {
            entries.remove(username)
            return ""
        }

        val cached = entries[username]
        if (cached != null && cached.avatarVersion == avatarVersion) {
            return cached.url
        }

        val url = AvatarUrlFactory(NetworkConfig.API_BASE_URL).create(username, avatarVersion)
        entries[username] = CachedAvatar(username, avatarVersion, url)
        return url
    }

    @Synchronized
    fun clear() {
        entries.clear()
    }

    private data class CachedAvatar(
        val username: String,
        val avatarVersion: Long,
        val url: String
    )
}
