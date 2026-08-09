package com.example.wordcrush.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordcrush.data.model.ActiveGameSession
import com.example.wordcrush.data.model.DailyLearningPlan
import com.example.wordcrush.data.model.RecordWordProgress
import com.example.wordcrush.data.model.RecordWordProgressCodec
import com.example.wordcrush.data.model.WordItem
import com.example.wordcrush.data.network.NetworkConfig
import com.example.wordcrush.domain.game.MatchGameAction
import com.example.wordcrush.domain.game.MatchGameCommand
import com.example.wordcrush.domain.game.MatchGameReducer
import com.example.wordcrush.domain.game.MatchGameState
import com.example.wordcrush.domain.usecase.ClearActiveGameSessionUseCase
import com.example.wordcrush.domain.usecase.CountdownUseCase
import com.example.wordcrush.domain.usecase.GetDailyLearningPlanUseCase
import com.example.wordcrush.domain.usecase.GetWordsByIdsUseCase
import com.example.wordcrush.domain.usecase.ObserveSessionUseCase
import com.example.wordcrush.domain.usecase.PersistActiveGameSessionUseCase
import com.example.wordcrush.domain.usecase.RecordCorrectMatchUseCase
import com.example.wordcrush.domain.usecase.RecordIncorrectMatchUseCase
import com.example.wordcrush.domain.usecase.RestoreActiveGameSessionUseCase
import com.example.wordcrush.domain.usecase.SaveGameRecordUseCase
import com.example.wordcrush.domain.usecase.UpdateWordMasteryUseCase
import com.example.wordcrush.ui.architecture.UdfStore
import com.example.wordcrush.ui.model.MatchCardUiModel
import com.example.wordcrush.ui.model.MatchMode
import com.example.wordcrush.ui.model.buildMatchRound
import com.example.wordcrush.utils.AvatarUrlFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed interface MatchAction {
    data class ModeSelected(val mode: MatchMode) : MatchAction
    data object StartOrRestart : MatchAction
    data object RequestStop : MatchAction
    data object DismissStop : MatchAction
    data object ConfirmStop : MatchAction
    data class CardClicked(val index: Int) : MatchAction
    data class PlayAudio(val wordId: Int) : MatchAction
    data object MarkLatestWordUnremembered : MatchAction
    data object ClearGameOver : MatchAction
    data object OpenRanking : MatchAction
}

sealed interface MatchEffect {
    data class ShowMessage(val message: String) : MatchEffect
    data class PlayAudio(val word: String, val type: Int = 1) : MatchEffect
    data class OpenRanking(val gameType: Int) : MatchEffect
}

data class MatchSessionUiState(
    val isLoading: Boolean = true,
    val avatarUrl: String = "",
    val score: Int = 0,
    val hearts: Int = 5,
    val remainingSeconds: Int = 180,
    val hasStarted: Boolean = false,
    val gameVisible: Boolean = false,
    val isResolvingPair: Boolean = false,
    val cards: List<MatchCardUiModel> = emptyList(),
    val dailyTarget: Int = DEFAULT_DAILY_WORD_TARGET,
    val todayWordCount: Int = 0,
    val masteredTodayCount: Int = 0,
    val statusTitle: String = "Ready to start",
    val statusMessage: String = "Tap start to load today's study words.",
    val latestMatchedWord: WordItem? = null,
    val gameOverMessage: String? = null,
    val gameOverScore: Int? = null,
    val learnedWordSummaries: List<String> = emptyList()
)

data class MatchUiState(
    val selectedMode: MatchMode = MatchMode.CLASSIC,
    val showStopConfirmDialog: Boolean = false,
    val classic: MatchSessionUiState = MatchSessionUiState(),
    val timed: MatchSessionUiState = MatchSessionUiState()
) {
    fun session(mode: MatchMode): MatchSessionUiState = when (mode) {
        MatchMode.CLASSIC -> classic
        MatchMode.TIMED -> timed
    }
}

private class GameRuntime(val mode: MatchMode) {
    var engine: MatchGameState = MatchGameState()
    var words: List<WordItem> = emptyList()
    var roundWords: Map<Int, WordItem> = emptyMap()
    var cursor: Int = 0
    val recordWordProgress = linkedMapOf<String, RecordWordProgress>()
    val learnedWordDetails = mutableListOf<WordItem>()
    var timerEndElapsedRealtime: Long = 0L
    var timerJob: Job? = null
}

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val clearActiveGameSessionUseCase: ClearActiveGameSessionUseCase,
    private val countdownUseCase: CountdownUseCase,
    private val getDailyLearningPlanUseCase: GetDailyLearningPlanUseCase,
    private val getWordsByIdsUseCase: GetWordsByIdsUseCase,
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val persistActiveGameSessionUseCase: PersistActiveGameSessionUseCase,
    private val recordCorrectMatchUseCase: RecordCorrectMatchUseCase,
    private val recordIncorrectMatchUseCase: RecordIncorrectMatchUseCase,
    private val restoreActiveGameSessionUseCase: RestoreActiveGameSessionUseCase,
    private val saveGameRecordUseCase: SaveGameRecordUseCase,
    private val updateWordMasteryUseCase: UpdateWordMasteryUseCase
) : ViewModel() {
    private val store = UdfStore<MatchUiState, MatchEffect>(MatchUiState())
    val uiState = store.uiState
    val effect = store.effect

    private val runtimes = MatchMode.entries.associateWith(::GameRuntime)

    private val currentState: MatchUiState
        get() = store.currentState

    init {
        viewModelScope.launch {
            observeSessionUseCase().collectLatest { session ->
                val avatarUrl = session.avatarUrl.ifBlank {
                    session.username.takeIf { it.isNotBlank() }
                        ?.let { AvatarUrlFactory(NetworkConfig.API_BASE_URL).create(it) }
                        .orEmpty()
                }
                updateAllSessions { it.copy(avatarUrl = avatarUrl) }
            }
        }
        MatchMode.entries.forEach { mode ->
            viewModelScope.launch { restoreOrInitialize(mode) }
        }
    }

    fun onAction(action: MatchAction) {
        when (action) {
            is MatchAction.ModeSelected -> updateState {
                if (it.session(action.mode).hasStarted) it else it.copy(selectedMode = action.mode)
            }
            MatchAction.StartOrRestart -> prepareGame(currentState.selectedMode)
            MatchAction.RequestStop -> updateState { it.copy(showStopConfirmDialog = true) }
            MatchAction.DismissStop -> updateState { it.copy(showStopConfirmDialog = false) }
            MatchAction.ConfirmStop -> {
                updateState { it.copy(showStopConfirmDialog = false) }
                endGameEarly(currentState.selectedMode)
            }
            is MatchAction.CardClicked -> reduce(
                currentState.selectedMode,
                MatchGameAction.CardClicked(action.index)
            )
            is MatchAction.PlayAudio -> playAudio(currentState.selectedMode, action.wordId)
            MatchAction.MarkLatestWordUnremembered -> markLatestWordUnremembered(currentState.selectedMode)
            MatchAction.ClearGameOver -> reduce(
                currentState.selectedMode,
                MatchGameAction.ClearGameOver
            )
            MatchAction.OpenRanking -> emitEffect(
                MatchEffect.OpenRanking(currentState.selectedMode.gameType)
            )
        }
    }

    private fun runtime(mode: MatchMode): GameRuntime = runtimes.getValue(mode)

    private fun updateState(transform: (MatchUiState) -> MatchUiState) = store.updateState(transform)

    private fun emitEffect(effect: MatchEffect) = store.emitEffect(effect)

    private fun updateAllSessions(transform: (MatchSessionUiState) -> MatchSessionUiState) {
        updateState { state ->
            state.copy(
                classic = transform(state.classic),
                timed = transform(state.timed)
            )
        }
    }

    private fun updateSession(mode: MatchMode, transform: (MatchSessionUiState) -> MatchSessionUiState) {
        updateState { state ->
            when (mode) {
                MatchMode.CLASSIC -> state.copy(classic = transform(state.classic))
                MatchMode.TIMED -> state.copy(timed = transform(state.timed))
            }
        }
    }

    private fun syncEngineToUi(mode: MatchMode) {
        val runtime = runtime(mode)
        val latestWord = runtime.engine.latestMatchedWordId?.let { wordId ->
            runtime.roundWords[wordId]
                ?: runtime.learnedWordDetails.firstOrNull { it.id == wordId }
        }
        updateSession(mode) {
            it.copy(
                isLoading = false,
                score = runtime.engine.score,
                hearts = runtime.engine.hearts,
                remainingSeconds = runtime.engine.remainingSeconds,
                hasStarted = runtime.engine.hasStarted,
                gameVisible = runtime.engine.gameVisible,
                isResolvingPair = runtime.engine.isResolvingPair,
                cards = runtime.engine.cards,
                latestMatchedWord = latestWord,
                gameOverMessage = runtime.engine.gameOverMessage,
                gameOverScore = runtime.engine.gameOverScore,
                learnedWordSummaries = buildLearnedWordSummaries(runtime.learnedWordDetails)
            )
        }
    }

    private fun updatePlanSummary(mode: MatchMode, plan: DailyLearningPlan) {
        val presentation = plan.toPresentation()
        updateSession(mode) {
            it.copy(
                dailyTarget = plan.dailyTarget,
                todayWordCount = plan.todayTotalCount,
                masteredTodayCount = plan.completedCount,
                statusTitle = presentation.title,
                statusMessage = presentation.message
            )
        }
    }

    private suspend fun restoreOrInitialize(mode: MatchMode) {
        val runtime = runtime(mode)
        val session = restoreActiveGameSessionUseCase(mode.gameType)
        val avatarUrl = currentState.session(mode).avatarUrl
        if (session != null && session.hasStarted) {
            restoreSession(mode, runtime, session)
            return
        }

        val plan = getDailyLearningPlanUseCase()
        runtime.words = plan.activeWords
        updateSession(mode) { it.copy(isLoading = false, avatarUrl = avatarUrl) }
        updatePlanSummary(mode, plan)
    }

    private suspend fun restoreSession(
        mode: MatchMode,
        runtime: GameRuntime,
        session: ActiveGameSession
    ) {
        val relatedWordIds = (
            session.roundWordIds +
                session.learnedWordIds +
                listOfNotNull(session.latestMatchedWordId)
            ).distinct()
        val wordsById = getWordsByIdsUseCase(relatedWordIds).associateBy { it.id }
        val plan = getDailyLearningPlanUseCase()
        runtime.words = plan.activeWords
        runtime.roundWords = session.roundWordIds.mapNotNull(wordsById::get).associateBy { it.id }
        runtime.cursor = session.cursor
        runtime.recordWordProgress.clear()
        RecordWordProgressCodec.decodeAll(session.learnedWords).forEach { progress ->
            runtime.recordWordProgress[progress.english] = progress
        }
        runtime.learnedWordDetails.clear()
        runtime.learnedWordDetails += session.learnedWordIds.mapNotNull(wordsById::get)
        runtime.timerEndElapsedRealtime = session.timerEndElapsedRealtime ?: 0L
        runtime.engine = MatchGameState(
            score = session.score,
            hearts = session.hearts,
            remainingSeconds = session.remainingSeconds ?: 180,
            hasStarted = session.hasStarted,
            gameVisible = session.gameVisible,
            cards = session.cards.map { it.toUiModel() },
            latestMatchedWordId = session.latestMatchedWordId
        )
        updateSession(mode) { it.copy(isLoading = false) }
        updatePlanSummary(mode, plan)
        syncEngineToUi(mode)

        if (mode == MatchMode.TIMED && session.gameVisible && session.hasStarted) {
            if (runtime.timerEndElapsedRealtime <= android.os.SystemClock.elapsedRealtime()) {
                finishGame(mode, "Time is up.")
            } else {
                startTimer(mode)
            }
        }
    }

    private fun prepareGame(mode: MatchMode) {
        val runtime = runtime(mode)
        runtime.timerJob?.cancel()
        runtime.timerEndElapsedRealtime = 0L
        runtime.recordWordProgress.clear()
        runtime.learnedWordDetails.clear()
        runtime.roundWords = emptyMap()
        runtime.cursor = 0
        viewModelScope.launch {
            val plan = getDailyLearningPlanUseCase()
            runtime.words = plan.activeWords
            updatePlanSummary(mode, plan)
            if (plan.allWordsMastered || plan.activeWords.isEmpty()) {
                runtime.engine = MatchGameState(remainingSeconds = 180)
                syncEngineToUi(mode)
                emitEffect(MatchEffect.ShowMessage(plan.toCompletionMessage()))
                return@launch
            }

            runtime.engine = MatchGameState(
                remainingSeconds = 180,
                hasStarted = true,
                gameVisible = true
            )
            syncEngineToUi(mode)
            if (mode == MatchMode.TIMED) {
                runtime.timerEndElapsedRealtime =
                    android.os.SystemClock.elapsedRealtime() + 180_000L
            }
            startNextRound(mode)
            if (mode == MatchMode.TIMED) startTimer(mode)
        }
    }

    private suspend fun startNextRound(mode: MatchMode) {
        val runtime = runtime(mode)
        val plan = getDailyLearningPlanUseCase()
        runtime.words = plan.activeWords
        updatePlanSummary(mode, plan)
        val round = buildMatchRound(runtime.words, runtime.cursor)
        if (round == null) {
            finishGame(mode, plan.toCompletionMessage())
            return
        }
        runtime.roundWords = round.words.associateBy { it.id }
        runtime.cursor = round.nextCursor
        runtime.engine = runtime.engine.copy(cards = round.cards, isResolvingPair = false)
        syncEngineToUi(mode)
        persistSession(mode)
    }

    private fun reduce(mode: MatchMode, action: MatchGameAction) {
        val runtime = runtime(mode)
        val reduction = MatchGameReducer.reduce(runtime.engine, action)
        runtime.engine = reduction.state
        syncEngineToUi(mode)
        reduction.commands.forEach { command -> executeCommand(mode, command) }
    }

    private fun executeCommand(mode: MatchMode, command: MatchGameCommand) {
        when (command) {
            MatchGameCommand.Persist -> persistSession(mode)
            is MatchGameCommand.CorrectMatch -> handleCorrectMatch(mode, command.wordId)
            is MatchGameCommand.IncorrectMatch -> handleIncorrectMatch(mode, command.wordIds)
            MatchGameCommand.StartNextRound -> viewModelScope.launch { startNextRound(mode) }
            is MatchGameCommand.SaveGame -> saveFinishedGame(mode, command.score)
        }
    }

    private fun handleCorrectMatch(mode: MatchMode, wordId: Int) {
        viewModelScope.launch {
            val runtime = runtime(mode)
            val matchedWord = runtime.roundWords[wordId] ?: return@launch
            val updatedWord = recordCorrectMatchUseCase(matchedWord.id)
            updateRecordedWordProgress(runtime, matchedWord, updatedWord)
            if (updatedWord != null && updatedWord.isMastered && !matchedWord.isMastered) {
                runtime.learnedWordDetails += updatedWord
            }
            runtime.engine = MatchGameReducer.reduce(
                runtime.engine,
                MatchGameAction.SetLatestMatchedWord(updatedWord?.id ?: matchedWord.id)
            ).state
            syncEngineToUi(mode)
            persistSession(mode)
            delay(260)
            reduce(mode, MatchGameAction.ResolveCorrectPair(wordId))
        }
    }

    private fun handleIncorrectMatch(mode: MatchMode, wordIds: Set<Int>) {
        viewModelScope.launch {
            recordIncorrectMatchUseCase(wordIds)
            persistSession(mode)
            delay(700)
            reduce(mode, MatchGameAction.ResolveMismatch)
        }
    }

    private fun startTimer(mode: MatchMode) {
        val runtime = runtime(mode)
        runtime.timerJob?.cancel()
        if (runtime.timerEndElapsedRealtime <= 0L) {
            runtime.timerEndElapsedRealtime =
                android.os.SystemClock.elapsedRealtime() + runtime.engine.remainingSeconds * 1_000L
        }
        runtime.timerJob = viewModelScope.launch {
            countdownUseCase(runtime.timerEndElapsedRealtime).collectLatest { seconds ->
                reduce(mode, MatchGameAction.TimerTick(seconds))
            }
        }
    }

    private fun saveFinishedGame(mode: MatchMode, score: Int) {
        val runtime = runtime(mode)
        runtime.timerJob?.cancel()
        runtime.timerEndElapsedRealtime = 0L
        viewModelScope.launch {
            clearActiveGameSessionUseCase(mode.gameType)
            updatePlanSummary(mode, getDailyLearningPlanUseCase())
            saveGameRecordUseCase(mode.gameType, score, serializeRecordWordProgress(runtime))
        }
    }

    private fun endGameEarly(mode: MatchMode) {
        val runtime = runtime(mode)
        runtime.timerJob?.cancel()
        runtime.timerEndElapsedRealtime = 0L
        val score = runtime.engine.score
        val learned = serializeRecordWordProgress(runtime)
        val summaries = buildLearnedWordSummaries(runtime.learnedWordDetails)
        resetForNextGame(mode, summaries)
        viewModelScope.launch {
            val result = saveGameRecordUseCase(mode.gameType, score, learned)
            val message = result.getOrNull()?.let { saveResult ->
                if (saveResult.syncedToCloud) {
                    "Current ${if (mode == MatchMode.CLASSIC) "classic" else "timed"} game has been saved and synced to cloud."
                } else {
                    "Current ${if (mode == MatchMode.CLASSIC) "classic" else "timed"} game is saved locally. Cloud upload failed."
                }
            } ?: "Current ${if (mode == MatchMode.CLASSIC) "classic" else "timed"} game is saved locally. Cloud upload failed."
            emitEffect(MatchEffect.ShowMessage(message))
        }
    }

    private fun resetForNextGame(mode: MatchMode, summaries: List<String> = emptyList()) {
        val runtime = runtime(mode)
        runtime.recordWordProgress.clear()
        runtime.learnedWordDetails.clear()
        runtime.roundWords = emptyMap()
        runtime.engine = MatchGameState(
            remainingSeconds = 180
        )
        syncEngineToUi(mode)
        updateSession(mode) { it.copy(learnedWordSummaries = summaries) }
        viewModelScope.launch {
            clearActiveGameSessionUseCase(mode.gameType)
            updatePlanSummary(mode, getDailyLearningPlanUseCase())
        }
    }

    private fun finishGame(mode: MatchMode, message: String) {
        val runtime = runtime(mode)
        val reduction = MatchGameReducer.reduce(runtime.engine, MatchGameAction.Finish(message))
        runtime.engine = reduction.state
        syncEngineToUi(mode)
        reduction.commands.forEach { command -> executeCommand(mode, command) }
    }

    private fun playAudio(mode: MatchMode, wordId: Int) {
        val word = runtime(mode).roundWords[wordId] ?: return
        emitEffect(MatchEffect.PlayAudio(word.english))
    }

    private fun markLatestWordUnremembered(mode: MatchMode) {
        val runtime = runtime(mode)
        val latestWordId = runtime.engine.latestMatchedWordId ?: return
        val latestWord = runtime.roundWords[latestWordId]
            ?: runtime.learnedWordDetails.firstOrNull { it.id == latestWordId }
            ?: return
        viewModelScope.launch {
            updateWordMasteryUseCase(latestWord.id, false)
            downgradeRecordedWordProgress(runtime, latestWord)
            runtime.learnedWordDetails.removeAll { it.id == latestWord.id }
            runtime.engine = MatchGameReducer.reduce(
                runtime.engine,
                MatchGameAction.SetLatestMatchedWord(null)
            ).state
            updatePlanSummary(mode, getDailyLearningPlanUseCase())
            syncEngineToUi(mode)
            persistSession(mode)
            emitEffect(MatchEffect.ShowMessage("${latestWord.english} marked as learning again."))
        }
    }

    private fun persistSession(mode: MatchMode) {
        val runtime = runtime(mode)
        viewModelScope.launch {
            persistActiveGameSessionUseCase(
                ActiveGameSession(
                    gameType = mode.gameType,
                    score = runtime.engine.score,
                    learnedWords = serializeRecordWordProgress(runtime),
                    hearts = runtime.engine.hearts,
                    cards = runtime.engine.cards.map { it.toSnapshot() },
                    roundWordIds = runtime.roundWords.values.map { it.id },
                    cursor = runtime.cursor,
                    hasStarted = runtime.engine.hasStarted,
                    gameVisible = runtime.engine.gameVisible,
                    latestMatchedWordId = runtime.engine.latestMatchedWordId,
                    learnedWordIds = runtime.learnedWordDetails.map { it.id }.distinct(),
                    remainingSeconds = if (mode == MatchMode.TIMED) {
                        runtime.engine.remainingSeconds
                    } else {
                        null
                    },
                    timerEndElapsedRealtime = if (mode == MatchMode.TIMED) {
                        runtime.timerEndElapsedRealtime
                    } else {
                        null
                    }
                )
            )
        }
    }

    private fun updateRecordedWordProgress(
        runtime: GameRuntime,
        matchedWord: WordItem,
        updatedWord: WordItem?
    ) {
        val previous = runtime.recordWordProgress[matchedWord.english]
        val nextCorrectCount = ((previous?.correctCount ?: 0) + 1).coerceAtMost(3)
        runtime.recordWordProgress[matchedWord.english] = RecordWordProgress(
            english = matchedWord.english,
            correctCount = nextCorrectCount,
            isLearned = updatedWord?.isMastered == true || previous?.isLearned == true
        )
    }

    private fun downgradeRecordedWordProgress(runtime: GameRuntime, word: WordItem) {
        val current = runtime.recordWordProgress[word.english] ?: return
        runtime.recordWordProgress[word.english] = current.copy(
            correctCount = current.correctCount.coerceAtMost(2).coerceAtLeast(1),
            isLearned = false
        )
    }

    private fun serializeRecordWordProgress(runtime: GameRuntime): List<String> =
        RecordWordProgressCodec.encodeAll(runtime.recordWordProgress.values)

    override fun onCleared() {
        runtimes.values.forEach { it.timerJob?.cancel() }
        store.close()
        super.onCleared()
    }
}
