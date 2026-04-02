package com.example.wordcrush.utils

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class AppStateManager @Inject constructor(
    @ApplicationContext context: android.content.Context
) {
    var domain: String = "http://192.168.201.21:8080"
        private set

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _token = MutableStateFlow("")
    val token: StateFlow<String> = _token.asStateFlow()

    private val _uid = MutableStateFlow("")
    val uid: StateFlow<String> = _uid.asStateFlow()

    private val _avatarUrl = MutableStateFlow("")
    val avatarUrl: StateFlow<String> = _avatarUrl.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun setUserInfo(username: String, token: String, uid: String) {
        _username.value = username
        _token.value = token
        _uid.value = uid
        _isLoggedIn.value = true
    }

    fun setAvatarUrl(url: String) {
        _avatarUrl.value = url
    }

    fun clearUserInfo() {
        _username.value = ""
        _token.value = ""
        _uid.value = ""
        _avatarUrl.value = ""
        _isLoggedIn.value = false
    }

    fun getFullUrl(path: String): String = "$domain$path"
}
