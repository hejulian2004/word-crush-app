package com.example.wordcrush.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordcrush.data.network.NetworkConfig
import com.example.wordcrush.domain.usecase.ChangePasswordUseCase
import com.example.wordcrush.domain.usecase.GetDailyLearningPlanUseCase
import com.example.wordcrush.domain.usecase.GetScoreSummaryUseCase
import com.example.wordcrush.domain.usecase.LogoutUseCase
import com.example.wordcrush.domain.usecase.ObserveSessionUseCase
import com.example.wordcrush.domain.usecase.SaveDailyTargetUseCase
import com.example.wordcrush.domain.usecase.SyncGameRecordsUseCase
import com.example.wordcrush.domain.usecase.UploadAvatarUseCase
import com.example.wordcrush.ui.architecture.UdfStore
import com.example.wordcrush.utils.AvatarUrlFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed interface HomeAction {
    data object Refresh : HomeAction
    data object Logout : HomeAction
    data class OldPasswordChanged(val value: String) : HomeAction
    data class NewPasswordChanged(val value: String) : HomeAction
    data class ConfirmPasswordChanged(val value: String) : HomeAction
    data object ShowPasswordDialog : HomeAction
    data object DismissPasswordDialog : HomeAction
    data object SubmitPasswordChange : HomeAction
    data class UploadAvatar(val uri: Uri) : HomeAction
    data object SyncCloudData : HomeAction
    data class DailyTargetChanged(val value: String) : HomeAction
    data object SaveDailyTarget : HomeAction
    data object OpenRecords : HomeAction
}

sealed interface HomeEffect {
    data class ShowMessage(val message: String) : HomeEffect
    data object NavigateToLogin : HomeEffect
    data object OpenRecords : HomeEffect
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val getDailyLearningPlanUseCase: GetDailyLearningPlanUseCase,
    private val getScoreSummaryUseCase: GetScoreSummaryUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val saveDailyTargetUseCase: SaveDailyTargetUseCase,
    private val syncGameRecordsUseCase: SyncGameRecordsUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase
) : ViewModel() {
    private val store = UdfStore<HomeUiState, HomeEffect>(HomeUiState())
    val uiState = store.uiState
    val effect = store.effect

    private val currentState: HomeUiState
        get() = store.currentState

    private fun updateState(transform: (HomeUiState) -> HomeUiState) = store.updateState(transform)
    private fun emitEffect(effect: HomeEffect) = store.emitEffect(effect)
    private fun launchAction(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    init {
        launchAction {
            observeSessionUseCase().collectLatest { session ->
                val avatarUrl = session.avatarUrl.ifBlank {
                    session.username.takeIf { it.isNotBlank() }
                        ?.let { AvatarUrlFactory(NetworkConfig.API_BASE_URL).create(it) }
                        .orEmpty()
                }
                updateState { it.copy(username = session.username, avatarUrl = avatarUrl) }
            }
        }
        onAction(HomeAction.Refresh)
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.Refresh -> refresh()
            HomeAction.Logout -> logout()
            is HomeAction.OldPasswordChanged -> updateState { it.copy(oldPassword = action.value) }
            is HomeAction.NewPasswordChanged -> updateState { it.copy(newPassword = action.value) }
            is HomeAction.ConfirmPasswordChanged -> updateState { it.copy(confirmPassword = action.value) }
            HomeAction.ShowPasswordDialog -> updateState { it.copy(showPasswordDialog = true) }
            HomeAction.DismissPasswordDialog -> updateState {
                it.copy(
                    showPasswordDialog = false,
                    oldPassword = "",
                    newPassword = "",
                    confirmPassword = ""
                )
            }
            HomeAction.SubmitPasswordChange -> changePassword()
            is HomeAction.UploadAvatar -> uploadAvatar(action.uri)
            HomeAction.SyncCloudData -> syncCloudData()
            is HomeAction.DailyTargetChanged -> updateState { it.copy(dailyTargetInput = action.value) }
            HomeAction.SaveDailyTarget -> saveDailyTarget()
            HomeAction.OpenRecords -> emitEffect(HomeEffect.OpenRecords)
        }
    }

    private fun refresh() {
        launchAction {
            val plan = getDailyLearningPlanUseCase()
            val summary = getScoreSummaryUseCase()
            updateState {
                it.copy(
                    dailyTarget = plan.dailyTarget,
                    dailyTargetInput = plan.dailyTarget.toString(),
                    todayWordCount = plan.todayTotalCount,
                    completedTodayCount = plan.completedCount,
                    allWordsMastered = plan.allWordsMastered,
                    dailyCompleted = plan.isDailyCompleted,
                    canIncreaseDailyTarget = plan.canIncreaseDailyTarget,
                    breakthroughScore = summary.breakthroughScore,
                    timeLimitScore = summary.timeLimitScore,
                    error = null
                )
            }
        }
    }

    private fun logout() {
        launchAction {
            logoutUseCase()
            emitEffect(HomeEffect.NavigateToLogin)
        }
    }

    private fun changePassword() {
        val state = currentState
        launchAction {
            updateState { it.copy(isLoading = true) }
            val result = changePasswordUseCase(
                state.oldPassword,
                state.newPassword,
                state.confirmPassword
            )
            updateState {
                it.copy(
                    isLoading = false,
                    showPasswordDialog = false,
                    oldPassword = "",
                    newPassword = "",
                    confirmPassword = ""
                )
            }
            emitEffect(HomeEffect.ShowMessage(result.getOrElse { error ->
                error.message ?: "Unable to update password."
            }))
        }
    }

    private fun uploadAvatar(uri: Uri) {
        launchAction {
            updateState { it.copy(isUploadingAvatar = true) }
            uploadAvatarUseCase(uri)
                .onSuccess { emitEffect(HomeEffect.ShowMessage("Avatar updated.")) }
                .onFailure { error ->
                    emitEffect(HomeEffect.ShowMessage(error.message ?: "Avatar upload failed."))
                }
            updateState { it.copy(isUploadingAvatar = false) }
        }
    }

    private fun syncCloudData() {
        launchAction {
            updateState { it.copy(isLoading = true) }
            syncGameRecordsUseCase()
                .onSuccess { result ->
                    refreshDataAfterAction()
                    val message = buildString {
                        append("Cloud data synced.")
                        if (result.uploadedCount > 0) {
                            append(" Uploaded ")
                            append(result.uploadedCount)
                            append(" local record")
                            if (result.uploadedCount > 1) append("s")
                            append(".")
                        }
                    }
                    emitEffect(HomeEffect.ShowMessage(message))
                }
                .onFailure { error ->
                    emitEffect(HomeEffect.ShowMessage(error.message ?: "Cloud sync failed."))
                }
            updateState { it.copy(isLoading = false) }
        }
    }

    private fun saveDailyTarget() {
        val input = currentState.dailyTargetInput
        launchAction {
            saveDailyTargetUseCase(input)
                .onSuccess {
                    val plan = getDailyLearningPlanUseCase()
                    updateState {
                        it.copy(
                            dailyTarget = plan.dailyTarget,
                            dailyTargetInput = plan.dailyTarget.toString(),
                            todayWordCount = plan.todayTotalCount,
                            completedTodayCount = plan.completedCount,
                            allWordsMastered = plan.allWordsMastered,
                            dailyCompleted = plan.isDailyCompleted,
                            canIncreaseDailyTarget = plan.canIncreaseDailyTarget
                        )
                    }
                    emitEffect(HomeEffect.ShowMessage("Daily learning count updated."))
                }
                .onFailure { error ->
                    emitEffect(HomeEffect.ShowMessage(
                        error.message ?: "Please enter a daily learning count greater than 0."
                    ))
                }
        }
    }

    private suspend fun refreshDataAfterAction() {
        val plan = getDailyLearningPlanUseCase()
        val summary = getScoreSummaryUseCase()
        updateState {
            it.copy(
                dailyTarget = plan.dailyTarget,
                dailyTargetInput = plan.dailyTarget.toString(),
                todayWordCount = plan.todayTotalCount,
                completedTodayCount = plan.completedCount,
                allWordsMastered = plan.allWordsMastered,
                dailyCompleted = plan.isDailyCompleted,
                canIncreaseDailyTarget = plan.canIncreaseDailyTarget,
                breakthroughScore = summary.breakthroughScore,
                timeLimitScore = summary.timeLimitScore
            )
        }
    }

    override fun onCleared() {
        store.close()
        super.onCleared()
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
    val dailyTarget: Int = DEFAULT_DAILY_WORD_TARGET,
    val dailyTargetInput: String = DEFAULT_DAILY_WORD_TARGET.toString(),
    val todayWordCount: Int = 0,
    val completedTodayCount: Int = 0,
    val allWordsMastered: Boolean = false,
    val dailyCompleted: Boolean = false,
    val canIncreaseDailyTarget: Boolean = false,
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val showPasswordDialog: Boolean = false
)
