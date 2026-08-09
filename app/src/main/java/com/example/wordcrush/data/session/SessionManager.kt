package com.example.wordcrush.data.session

import com.example.wordcrush.constants.AppStrings
import com.example.wordcrush.data.local.PreferenceManager
import com.example.wordcrush.data.model.UserResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class AuthSession(
    val token: String,
    val username: String,
    val uid: String,
    val avatarUrl: String = ""
)

@Singleton
class SessionManager @Inject constructor(
    private val preferenceManager: PreferenceManager
) {
    private val persistenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val restoreMutex = Mutex()
    private var restored = false

    private val _session = MutableStateFlow<AuthSession?>(null)
    val session: StateFlow<AuthSession?> = _session.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _uid = MutableStateFlow("")
    val uid: StateFlow<String> = _uid.asStateFlow()

    private val _avatarUrl = MutableStateFlow("")
    val avatarUrl: StateFlow<String> = _avatarUrl.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _sessionExpiredEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val sessionExpiredEvent: SharedFlow<String> = _sessionExpiredEvent.asSharedFlow()

    val currentToken: String?
        get() = _session.value?.token?.takeIf { it.isNotBlank() }

    val currentUsername: String?
        get() = _session.value?.username?.takeIf { it.isNotBlank() }

    suspend fun restore() {
        restoreMutex.withLock {
            if (restored) {
                return
            }
            preferenceManager.readSession()?.let { stored ->
                applySession(
                    AuthSession(
                        token = stored.token,
                        username = stored.username,
                        uid = stored.uid,
                        avatarUrl = stored.avatarUrl
                    )
                )
            }
            restored = true
        }
    }

    suspend fun establish(user: UserResponse) {
        val session = AuthSession(
            token = user.token,
            username = user.username,
            uid = user.uid,
            avatarUrl = _avatarUrl.value
        )
        preferenceManager.saveSession(
            token = session.token,
            username = session.username,
            uid = session.uid,
            avatarUrl = session.avatarUrl
        )
        applySession(session)
        restored = true
    }

    fun setAvatarUrl(url: String) {
        _avatarUrl.value = url
        _session.value = _session.value?.copy(avatarUrl = url)
        persistenceScope.launch {
            preferenceManager.saveAvatarUrl(url)
        }
    }

    suspend fun clear() {
        preferenceManager.clearSession()
        clearMemory()
    }

    fun invalidateFromNetwork(message: String = AppStrings.Errors.SESSION_EXPIRED) {
        val hadSession = synchronized(this) {
            if (_session.value == null && _isLoggedIn.value.not()) {
                false
            } else {
                clearMemory()
                true
            }
        }
        if (!hadSession) {
            return
        }
        persistenceScope.launch {
            preferenceManager.clearSession()
        }
        _sessionExpiredEvent.tryEmit(message)
    }

    private fun applySession(session: AuthSession) {
        _session.value = session
        _username.value = session.username
        _uid.value = session.uid
        _avatarUrl.value = session.avatarUrl
        _isLoggedIn.value = session.token.isNotBlank()
    }

    private fun clearMemory() {
        _session.value = null
        _username.value = ""
        _uid.value = ""
        _avatarUrl.value = ""
        _isLoggedIn.value = false
    }
}
