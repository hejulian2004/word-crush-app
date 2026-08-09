package com.example.wordcrush.domain.usecase

import android.net.Uri
import com.example.wordcrush.data.repository.AccountRepository
import com.example.wordcrush.data.session.SessionManager
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class SessionSnapshot(
    val username: String = "",
    val uid: String = "",
    val avatarUrl: String = "",
    val isLoggedIn: Boolean = false
)

class LoginUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<String> {
        if (username.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Username and password are required."))
        }
        return accountRepository.login(username.trim(), password.trim())
    }
}

class RegisterUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(username: String, password: String, confirmPassword: String): Result<String> {
        when {
            username.isBlank() -> return Result.failure(IllegalArgumentException("Username is required."))
            password.isBlank() || confirmPassword.isBlank() ->
                return Result.failure(IllegalArgumentException("Password is required."))
            password != confirmPassword ->
                return Result.failure(IllegalArgumentException("Passwords do not match."))
            password.length < 6 ->
                return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }
        return accountRepository.register(username.trim(), password.trim())
    }
}

class ValidateSessionUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): Result<Boolean> {
        return runCatching {
            sessionManager.restore()
            if (sessionManager.currentToken.isNullOrBlank()) {
                false
            } else {
                accountRepository.checkToken().getOrThrow()
                true
            }
        }
    }
}

class RestoreSessionUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): Boolean {
        sessionManager.restore()
        return sessionManager.isLoggedIn.value
    }
}

class LogoutUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke() {
        accountRepository.logout()
    }
}

class ChangePasswordUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<String> {
        when {
            oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() ->
                return Result.failure(IllegalArgumentException("Please fill in all password fields."))
            newPassword != confirmPassword ->
                return Result.failure(IllegalArgumentException("New passwords do not match."))
            newPassword.length < 6 ->
                return Result.failure(IllegalArgumentException("New password must be at least 6 characters."))
        }

        val username = sessionManager.currentUsername.orEmpty()
        if (username.isBlank()) {
            return Result.failure(IllegalStateException("No logged-in user found."))
        }
        return accountRepository.changePassword(username, oldPassword, newPassword)
    }
}

class UploadAvatarUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(uri: Uri): Result<String> {
        val username = sessionManager.currentUsername.orEmpty()
        if (username.isBlank()) {
            return Result.failure(IllegalStateException("No logged-in user found."))
        }
        return accountRepository.uploadAvatar(username, uri)
    }
}

class ObserveSessionUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    operator fun invoke(): Flow<SessionSnapshot> {
        return combine(
            sessionManager.username,
            sessionManager.uid,
            sessionManager.avatarUrl,
            sessionManager.isLoggedIn
        ) { username, uid, avatarUrl, isLoggedIn ->
            SessionSnapshot(username, uid, avatarUrl, isLoggedIn)
        }
    }

    fun expiredMessages(): Flow<String> = sessionManager.sessionExpiredEvent
}
