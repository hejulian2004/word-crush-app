package com.example.wordcrush.data.repository

import com.example.wordcrush.data.model.ActiveGameSession
import com.example.wordcrush.data.local.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class ActiveGameSessionManager @Inject constructor(
    private val gameRecordRepository: GameRecordRepository,
    private val preferenceManager: PreferenceManager
) {
    private val gson = Gson()
    private val sessionListType = object : TypeToken<List<ActiveGameSession>>() {}.type
    private val mutex = Mutex()
    private val sessions = linkedMapOf<Int, ActiveGameSession>()
    private var hasLoadedPersistedSessions = false

    suspend fun updateSession(session: ActiveGameSession) {
        mutex.withLock {
            ensureLoadedLocked()
            if (sessions[session.gameType] == session) {
                return
            }
            sessions[session.gameType] = session
            persistSessionsLocked()
        }
    }

    suspend fun getSession(gameType: Int): ActiveGameSession? {
        return mutex.withLock {
            ensureLoadedLocked()
            sessions[gameType]
        }
    }

    suspend fun clearSession(gameType: Int) {
        mutex.withLock {
            ensureLoadedLocked()
            if (!sessions.containsKey(gameType)) {
                return
            }
            sessions.remove(gameType)
            persistSessionsLocked()
        }
    }

    suspend fun persistActiveSessionsLocally() {
        val activeSessions = mutex.withLock {
            ensureLoadedLocked()
            sessions.values.toList().also {
                sessions.clear()
                persistSessionsLocked()
            }
        }
        activeSessions.forEach { session ->
            gameRecordRepository.saveLocalRecord(
                gameType = session.gameType,
                score = session.score,
                learnedWords = session.learnedWords
            )
        }
    }

    private suspend fun ensureLoadedLocked() {
        if (hasLoadedPersistedSessions) {
            return
        }
        val json = preferenceManager.getActiveGameSessionsJson()
        val restoredSessions = runCatching {
            gson.fromJson<List<ActiveGameSession>>(json, sessionListType)
        }.getOrNull().orEmpty()
        sessions.clear()
        restoredSessions.forEach { session ->
            sessions[session.gameType] = session
        }
        hasLoadedPersistedSessions = true
    }

    private suspend fun persistSessionsLocked() {
        if (sessions.isEmpty()) {
            preferenceManager.clearActiveGameSessions()
            return
        }
        preferenceManager.saveActiveGameSessionsJson(
            gson.toJson(sessions.values.toList(), sessionListType)
        )
    }
}
