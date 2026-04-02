package com.example.wordcrush.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "word_crush_preferences")

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val DEFAULT_DAILY_WORD_TARGET = 30
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val UID_KEY = stringPreferencesKey("uid")
        private val AVATAR_URL_KEY = stringPreferencesKey("avatar_url")
        private val DAILY_WORD_TARGET_KEY = intPreferencesKey("daily_word_target")
        private val DAILY_PLAN_DATE_KEY = stringPreferencesKey("daily_plan_date")
        private val DAILY_PLAN_WORD_IDS_KEY = stringPreferencesKey("daily_plan_word_ids")
        private val ACTIVE_GAME_SESSIONS_KEY = stringPreferencesKey("active_game_sessions")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    val usernameFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USERNAME_KEY]
    }

    val uidFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[UID_KEY]
    }

    val avatarUrlFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AVATAR_URL_KEY]
    }

    val dailyWordTargetFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DAILY_WORD_TARGET_KEY] ?: DEFAULT_DAILY_WORD_TARGET
    }

    suspend fun saveUserInfo(token: String, username: String, uid: String) {
        val currentToken = tokenFlow.firstOrNull()
        val currentUsername = usernameFlow.firstOrNull()
        val currentUid = uidFlow.firstOrNull()
        if (currentToken == token && currentUsername == username && currentUid == uid) {
            return
        }
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USERNAME_KEY] = username
            preferences[UID_KEY] = uid
        }
    }

    suspend fun saveToken(token: String) {
        if (tokenFlow.firstOrNull() == token) {
            return
        }
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun saveAvatarUrl(url: String) {
        if (avatarUrlFlow.firstOrNull() == url) {
            return
        }
        context.dataStore.edit { preferences ->
            preferences[AVATAR_URL_KEY] = url
        }
    }

    suspend fun getDailyWordTarget(): Int {
        return dailyWordTargetFlow.firstOrNull() ?: DEFAULT_DAILY_WORD_TARGET
    }

    suspend fun saveDailyWordTarget(target: Int) {
        val normalizedTarget = target.coerceAtLeast(1)
        if (getDailyWordTarget() == normalizedTarget) {
            return
        }
        context.dataStore.edit { preferences ->
            preferences[DAILY_WORD_TARGET_KEY] = normalizedTarget
        }
    }

    suspend fun getDailyPlanDate(): String {
        return context.dataStore.data.map { preferences ->
            preferences[DAILY_PLAN_DATE_KEY].orEmpty()
        }.firstOrNull().orEmpty()
    }

    suspend fun getDailyPlanWordIds(): List<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[DAILY_PLAN_WORD_IDS_KEY]
                .orEmpty()
                .split(",")
                .mapNotNull { value -> value.trim().toIntOrNull() }
        }.firstOrNull().orEmpty()
    }

    suspend fun saveDailyPlan(date: String, wordIds: List<Int>) {
        if (getDailyPlanDate() == date && getDailyPlanWordIds() == wordIds) {
            return
        }
        context.dataStore.edit { preferences ->
            preferences[DAILY_PLAN_DATE_KEY] = date
            preferences[DAILY_PLAN_WORD_IDS_KEY] = wordIds.joinToString(",")
        }
    }

    suspend fun clearDailyPlan() {
        if (getDailyPlanDate().isBlank() && getDailyPlanWordIds().isEmpty()) {
            return
        }
        context.dataStore.edit { preferences ->
            preferences.remove(DAILY_PLAN_DATE_KEY)
            preferences.remove(DAILY_PLAN_WORD_IDS_KEY)
        }
    }

    suspend fun getActiveGameSessionsJson(): String {
        return context.dataStore.data.map { preferences ->
            preferences[ACTIVE_GAME_SESSIONS_KEY].orEmpty()
        }.firstOrNull().orEmpty()
    }

    suspend fun saveActiveGameSessionsJson(json: String) {
        if (getActiveGameSessionsJson() == json) {
            return
        }
        context.dataStore.edit { preferences ->
            if (json.isBlank()) {
                preferences.remove(ACTIVE_GAME_SESSIONS_KEY)
            } else {
                preferences[ACTIVE_GAME_SESSIONS_KEY] = json
            }
        }
    }

    suspend fun clearActiveGameSessions() {
        if (getActiveGameSessionsJson().isBlank()) {
            return
        }
        context.dataStore.edit { preferences ->
            preferences.remove(ACTIVE_GAME_SESSIONS_KEY)
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return context.dataStore.data.map { preferences ->
            !preferences[TOKEN_KEY].isNullOrEmpty()
        }.firstOrNull() == true
    }
}
