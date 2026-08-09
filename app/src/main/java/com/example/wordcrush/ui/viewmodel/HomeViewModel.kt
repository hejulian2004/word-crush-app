package com.example.wordcrush.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordcrush.data.local.PreferenceManager
import com.example.wordcrush.data.repository.AccountRepository
import com.example.wordcrush.data.repository.GameRecordRepository
import com.example.wordcrush.data.repository.WordRepository
import com.example.wordcrush.data.network.NetworkConfig
import com.example.wordcrush.data.session.SessionManager
import com.example.wordcrush.utils.AvatarUrlFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val gameRecordRepository: GameRecordRepository,
    private val wordRepository: WordRepository,
    private val preferenceManager: PreferenceManager,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<SessionNavigationEvent?>(null)
    val navigationEvent: StateFlow<SessionNavigationEvent?> = _navigationEvent.asStateFlow()

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            sessionManager.username.collectLatest { username ->
                _uiState.value = _uiState.value.copy(username = username)
            }
        }
        viewModelScope.launch {
            sessionManager.avatarUrl.collectLatest { avatarUrl ->
                _uiState.value = _uiState.value.copy(avatarUrl = avatarUrl)
            }
        }
        viewModelScope.launch {
            if (_uiState.value.avatarUrl.isBlank()) {
                val username = sessionManager.currentUsername.orEmpty()
                if (username.isNotBlank()) {
                    _uiState.value = _uiState.value.copy(
                        avatarUrl = AvatarUrlFactory(NetworkConfig.API_BASE_URL).create(username)
                    )
                }
            }
        }
        refreshDailyPlan()
        refreshScoreSummary()
    }

    fun logout() {
        viewModelScope.launch {
            accountRepository.logout()
            _navigationEvent.value = SessionNavigationEvent.NavigateToLogin
        }
    }

    fun resetNavigationEvent() {
        _navigationEvent.value = null
    }

    fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            when {
                oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() ->
                    _messageEvent.emit("Please fill in all password fields.")
                newPassword != confirmPassword ->
                    _messageEvent.emit("New passwords do not match.")
                newPassword.length < 6 ->
                    _messageEvent.emit("New password must be at least 6 characters.")
                else -> {
                    val username = sessionManager.currentUsername.orEmpty()
                    if (username.isBlank()) {
                        _messageEvent.emit("No logged-in user found.")
                        return@launch
                    }
                    _uiState.value = _uiState.value.copy(isLoading = true)
                    val result = accountRepository.changePassword(username, oldPassword, newPassword)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _messageEvent.emit(result.getOrElse { it.message ?: "Unable to update password." })
                }
            }
        }
    }

    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            val username = sessionManager.currentUsername.orEmpty()
            if (username.isBlank()) {
                _messageEvent.emit("No logged-in user found.")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isUploadingAvatar = true)
            accountRepository.uploadAvatar(username, uri)
                .onSuccess {
                    _messageEvent.emit("Avatar updated.")
                }
                .onFailure { error ->
                    _messageEvent.emit(error.message ?: "Avatar upload failed.")
                }
            _uiState.value = _uiState.value.copy(isUploadingAvatar = false)
        }
    }

    fun getAllScore() {
        viewModelScope.launch {
            refreshScoreSummary()
        }
    }

    fun syncCloudData() {
        viewModelScope.launch {
            gameRecordRepository.syncFromCloud()
                .onSuccess { result ->
                    refreshScoreSummary()
                    val message = buildString {
                        append("Cloud data synced.")
                        if (result.uploadedCount > 0) {
                            append(" Uploaded ")
                            append(result.uploadedCount)
                            append(" local record")
                            if (result.uploadedCount > 1) {
                                append("s")
                            }
                            append(".")
                        }
                    }
                    _messageEvent.emit(message)
                }
                .onFailure { error ->
                    _messageEvent.emit(error.message ?: "Cloud sync failed.")
                }
        }
    }

    fun updateDailyTargetInput(input: String) {
        _uiState.value = _uiState.value.copy(dailyTargetInput = input)
    }

    fun saveDailyTarget() {
        viewModelScope.launch {
            val target = _uiState.value.dailyTargetInput.toIntOrNull()
            if (target == null || target <= 0) {
                _messageEvent.emit("Please enter a daily learning count greater than 0.")
                return@launch
            }

            preferenceManager.saveDailyWordTarget(target)
            refreshDailyPlan()
            _messageEvent.emit("Daily learning count updated.")
        }
    }

    private fun refreshDailyPlan() {
        viewModelScope.launch {
            val plan = wordRepository.getDailyLearningPlan()
            _uiState.value = _uiState.value.copy(
                dailyTarget = plan.dailyTarget,
                dailyTargetInput = plan.dailyTarget.toString(),
                todayWordCount = plan.todayTotalCount,
                completedTodayCount = plan.completedCount,
                allWordsMastered = plan.allWordsMastered,
                dailyCompleted = plan.isDailyCompleted,
                canIncreaseDailyTarget = plan.canIncreaseDailyTarget
            )
        }
    }

    private fun refreshScoreSummary() {
        viewModelScope.launch {
            val summary = gameRecordRepository.getScoreSummary()
            _uiState.value = _uiState.value.copy(
                breakthroughScore = summary.breakthroughScore,
                timeLimitScore = summary.timeLimitScore
            )
        }
    }
}

data class HomeUiState(
    val username: String = "",
    val avatarUrl: String = "",
    val isLoading: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val error: String? = null,
    val breakthroughScore: Int = 0,
    val timeLimitScore: Int = 0,
    val dailyTarget: Int = PreferenceManager.DEFAULT_DAILY_WORD_TARGET,
    val dailyTargetInput: String = PreferenceManager.DEFAULT_DAILY_WORD_TARGET.toString(),
    val todayWordCount: Int = 0,
    val completedTodayCount: Int = 0,
    val allWordsMastered: Boolean = false,
    val dailyCompleted: Boolean = false,
    val canIncreaseDailyTarget: Boolean = false
)
