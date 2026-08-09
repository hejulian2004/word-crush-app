package com.example.wordcrush.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.wordcrush.constants.AppConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = AppConstants.Preferences.DATA_STORE_NAME
)

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey(AppConstants.Preferences.TOKEN_KEY)
        private val USERNAME_KEY = stringPreferencesKey(AppConstants.Preferences.USERNAME_KEY)
        private val UID_KEY = stringPreferencesKey(AppConstants.Preferences.UID_KEY)
        private val AVATAR_URL_KEY = stringPreferencesKey(AppConstants.Preferences.AVATAR_URL_KEY)
        private val DAILY_WORD_TARGET_KEY = intPreferencesKey(AppConstants.Preferences.DAILY_WORD_TARGET_KEY)
        private val DAILY_PLAN_DATE_KEY = stringPreferencesKey(AppConstants.Preferences.DAILY_PLAN_DATE_KEY)
        private val DAILY_PLAN_WORD_IDS_KEY = stringPreferencesKey(AppConstants.Preferences.DAILY_PLAN_WORD_IDS_KEY)
        private val ACTIVE_GAME_SESSIONS_KEY = stringPreferencesKey(AppConstants.Preferences.ACTIVE_GAME_SESSIONS_KEY)
        private val LEARNING_MIGRATION_COMPLETED_KEY = booleanPreferencesKey(
            AppConstants.Preferences.LEARNING_MIGRATION_COMPLETED_KEY
        )
    }

    data class PersistedSession(
        val token: String,
        val username: String,
        val uid: String,
        val avatarUrl: String
    )

    val dailyWordTargetFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DAILY_WORD_TARGET_KEY] ?: AppConstants.Learning.DEFAULT_DAILY_WORD_TARGET
    }

    suspend fun readSession(): PersistedSession? {
        val preferences = context.dataStore.data.first()
        val token = preferences[TOKEN_KEY].orEmpty()
        if (token.isBlank()) {
            return null
        }
        return PersistedSession(
            token = token,
            username = preferences[USERNAME_KEY].orEmpty(),
            uid = preferences[UID_KEY].orEmpty(),
            avatarUrl = preferences[AVATAR_URL_KEY].orEmpty()
        )
    }

    suspend fun saveSession(token: String, username: String, uid: String, avatarUrl: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USERNAME_KEY] = username
            preferences[UID_KEY] = uid
            if (avatarUrl.isBlank()) {
                preferences.remove(AVATAR_URL_KEY)
            } else {
                preferences[AVATAR_URL_KEY] = avatarUrl
            }
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USERNAME_KEY)
            preferences.remove(UID_KEY)
            preferences.remove(AVATAR_URL_KEY)
        }
    }

    suspend fun saveAvatarUrl(url: String) {
        context.dataStore.edit { preferences ->
            if (url.isBlank()) {
                preferences.remove(AVATAR_URL_KEY)
            } else {
                preferences[AVATAR_URL_KEY] = url
            }
        }
    }

    suspend fun getDailyWordTarget(): Int {
        return dailyWordTargetFlow.firstOrNull() ?: AppConstants.Learning.DEFAULT_DAILY_WORD_TARGET
    }

    suspend fun saveDailyWordTarget(target: Int) {
        val normalizedTarget = target.coerceAtLeast(AppConstants.Learning.MIN_DAILY_WORD_TARGET)
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
                .split(AppConstants.Preferences.LIST_SEPARATOR)
                .mapNotNull { value -> value.trim().toIntOrNull() }
        }.firstOrNull().orEmpty()
    }

    suspend fun saveDailyPlan(date: String, wordIds: List<Int>) {
        if (getDailyPlanDate() == date && getDailyPlanWordIds() == wordIds) {
            return
        }
        context.dataStore.edit { preferences ->
            preferences[DAILY_PLAN_DATE_KEY] = date
            preferences[DAILY_PLAN_WORD_IDS_KEY] = wordIds.joinToString(AppConstants.Preferences.LIST_SEPARATOR)
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

    suspend fun isLearningMigrationCompleted(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[LEARNING_MIGRATION_COMPLETED_KEY] ?: false
        }.firstOrNull() ?: false
    }

    suspend fun markLearningMigrationCompleted() {
        context.dataStore.edit { preferences ->
            preferences[LEARNING_MIGRATION_COMPLETED_KEY] = true
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

}
