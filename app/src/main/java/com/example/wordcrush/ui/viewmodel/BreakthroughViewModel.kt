package com.example.wordcrush.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordcrush.data.model.ActiveGameSession
import com.example.wordcrush.data.model.DailyLearningPlan
import com.example.wordcrush.data.local.PreferenceManager
import com.example.wordcrush.data.model.RecordWordProgress
import com.example.wordcrush.data.model.RecordWordProgressCodec
import com.example.wordcrush.data.model.WordItem
import com.example.wordcrush.data.repository.ActiveGameSessionManager
import com.example.wordcrush.data.repository.GameRecordRepository
import com.example.wordcrush.data.repository.WordRepository
import com.example.wordcrush.ui.model.MatchCardUiModel
import com.example.wordcrush.ui.model.MatchCardFeedback
import com.example.wordcrush.ui.model.MatchGameEvent
import com.example.wordcrush.ui.model.buildMatchRound
import com.example.wordcrush.utils.AppStateManager
import com.example.wordcrush.utils.AvatarUrlFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class BreakthroughViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val gameRecordRepository: GameRecordRepository,
    private val activeGameSessionManager: ActiveGameSessionManager,
    private val preferenceManager: PreferenceManager,
    private val appStateManager: AppStateManager
) : ViewModel() {
    private companion object {
        const val GAME_TYPE = 0
    }

    private val _uiState = MutableStateFlow(BreakthroughUiState(isLoading = true))
    val uiState: StateFlow<BreakthroughUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<MatchGameEvent>()
    val event: SharedFlow<MatchGameEvent> = _event.asSharedFlow()

    private var words: List<WordItem> = emptyList()
    private var roundWords: Map<Int, WordItem> = emptyMap()
    private var cursor = 0
    private val recordWordProgress = linkedMapOf<String, RecordWordProgress>()
    private val learnedWordDetails = mutableListOf<WordItem>()

    init {
        viewModelScope.launch {
            val username = preferenceManager.usernameFlow.firstOrNull().orEmpty()
            val avatarUrl = if (username.isBlank()) "" else AvatarUrlFactory(appStateManager.domain).create(username)
            restoreOrInitialize(avatarUrl)
        }
    }

    fun onCardClicked(index: Int) {
        if (_uiState.value.isResolvingPair) return
        val cards = _uiState.value.cards.toMutableList()
        val current = cards.getOrNull(index) ?: return
        if (current.isMatched) return

        val selectedIndex = cards.indexOfFirst { it.isSelected && !it.isMatched }
        if (selectedIndex == -1) {
            cards[index] = current.copy(isSelected = true)
            _uiState.value = _uiState.value.copy(cards = cards)
            publishSessionAsync()
            return
        }

        if (selectedIndex == index) {
            cards[index] = current.copy(isSelected = false)
            _uiState.value = _uiState.value.copy(cards = cards)
            publishSessionAsync()
            return
        }

        val selected = cards[selectedIndex]
        if (selected.wordId == current.wordId && selected.type != current.type) {
            cards[selectedIndex] = selected.copy(
                isSelected = false,
                isMatched = true,
                feedback = MatchCardFeedback.MATCHED
            )
            cards[index] = current.copy(
                isSelected = false,
                isMatched = true,
                feedback = MatchCardFeedback.MATCHED
            )

            val matchedWord = roundWords[current.wordId] ?: return
            val newScore = _uiState.value.score + 1
            val newHearts = if (newScore % 6 == 0) minOf(5, _uiState.value.hearts + 1) else _uiState.value.hearts
            _uiState.value = _uiState.value.copy(
                cards = cards,
                score = newScore,
                hearts = newHearts,
                isResolvingPair = true
            )

            viewModelScope.launch {
                val updatedWord = wordRepository.recordCorrectMatch(matchedWord.id)
                updateRecordedWordProgress(matchedWord, updatedWord)
                if (updatedWord != null && updatedWord.isMastered && !matchedWord.isMastered) {
                    learnedWordDetails += updatedWord
                }
                _uiState.value = _uiState.value.copy(latestMatchedWord = updatedWord ?: matchedWord)
                publishSession()
                delay(260)
                val remainingCards = cards.filterNot { it.wordId == matchedWord.id }
                if (remainingCards.isEmpty()) {
                    _uiState.value = _uiState.value.copy(cards = emptyList(), isResolvingPair = false)
                    startNextRound()
                } else {
                    _uiState.value = _uiState.value.copy(cards = remainingCards, isResolvingPair = false)
                    publishSession()
                }
            }
        } else if (selected.type == current.type) {
            cards[selectedIndex] = selected.copy(isSelected = false)
            cards[index] = current.copy(isSelected = true)
            _uiState.value = _uiState.value.copy(cards = cards)
            publishSessionAsync()
        } else {
            cards[selectedIndex] = selected.copy(isSelected = false, feedback = MatchCardFeedback.MISMATCH)
            cards[index] = current.copy(isSelected = false, feedback = MatchCardFeedback.MISMATCH)
            val hearts = _uiState.value.hearts - 1
            _uiState.value = _uiState.value.copy(cards = cards, hearts = hearts, isResolvingPair = true)
            viewModelScope.launch {
                wordRepository.recordIncorrectMatch(setOf(selected.wordId, current.wordId))
                if (_uiState.value.latestMatchedWord?.id in setOf(selected.wordId, current.wordId)) {
                    _uiState.value = _uiState.value.copy(latestMatchedWord = null)
                }
                publishSession()
                delay(700)
                if (hearts <= 0) {
                    finishGame()
                } else {
                    val clearedCards = cards.map { it.copy(feedback = MatchCardFeedback.NONE) }
                    _uiState.value = _uiState.value.copy(cards = clearedCards, isResolvingPair = false)
                    publishSession()
                }
            }
        }
    }

    fun restartGame() {
        viewModelScope.launch {
            prepareGame()
        }
    }

    fun resetForNextGame(clearLearnedSummaries: Boolean = true) {
        recordWordProgress.clear()
        learnedWordDetails.clear()
        _uiState.value = _uiState.value.copy(
            hasStarted = false,
            gameVisible = false,
            cards = emptyList(),
            hearts = 5,
            score = 0,
            isResolvingPair = false,
            statusTitle = "Ready to start",
            statusMessage = "Tap start to load today's study words.",
            latestMatchedWord = null,
            gameOverMessage = null,
            gameOverScore = null,
            learnedWordSummaries = if (clearLearnedSummaries) emptyList() else _uiState.value.learnedWordSummaries
        )
        viewModelScope.launch {
            activeGameSessionManager.clearSession(GAME_TYPE)
        }
        refreshPlanSummary()
    }

    fun endGameEarly() {
        viewModelScope.launch {
            val score = _uiState.value.score
            val learned = serializeRecordWordProgress()
            val learnedSummaries = buildLearnedWordSummaries(learnedWordDetails)
            _uiState.value = _uiState.value.copy(learnedWordSummaries = learnedSummaries)
            resetForNextGame(clearLearnedSummaries = false)
            val result = gameRecordRepository.saveRecord(GAME_TYPE, score, learned)
            val message = result.getOrNull()?.let { saveResult ->
                if (saveResult.syncedToCloud) {
                    "Current classic game has been saved and synced to cloud."
                } else {
                    "Current classic game is saved locally. Cloud upload failed."
                }
            } ?: "Current classic game is saved locally. Cloud upload failed."
            _event.emit(MatchGameEvent.Message(message))
        }
    }

    fun playAudioForWord(wordId: Int) {
        val word = roundWords[wordId] ?: return
        viewModelScope.launch {
            _event.emit(MatchGameEvent.PlayAudio(word.english))
        }
    }

    fun clearGameOverDialog() {
        _uiState.value = _uiState.value.copy(
            gameOverMessage = null,
            gameOverScore = null
        )
    }

    fun markLatestWordUnremembered() {
        val latestWord = _uiState.value.latestMatchedWord ?: return
        viewModelScope.launch {
            wordRepository.updateWordMastered(latestWord.id, false)
            downgradeRecordedWordProgress(latestWord)
            learnedWordDetails.removeAll { it.id == latestWord.id }
            _uiState.value = _uiState.value.copy(latestMatchedWord = null)
            updatePlanSummary(wordRepository.getDailyLearningPlan())
            publishSession()
            _event.emit(MatchGameEvent.Message("${latestWord.english} marked as learning again."))
        }
    }

    private suspend fun startNextRound() {
        val plan = wordRepository.getDailyLearningPlan()
        words = plan.activeWords
        updatePlanSummary(plan)
        val round = buildMatchRound(words, cursor)
        if (round == null) {
            finishGame(plan.toCompletionMessage())
            return
        }

        roundWords = round.words.associateBy { it.id }
        cursor = round.nextCursor
        _uiState.value = _uiState.value.copy(cards = round.cards, isResolvingPair = false)
        publishSession()
    }

    private suspend fun prepareGame() {
        val plan = wordRepository.getDailyLearningPlan()
        recordWordProgress.clear()
        learnedWordDetails.clear()
        cursor = 0
        updatePlanSummary(plan)
        if (plan.allWordsMastered || plan.activeWords.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                hasStarted = false,
                gameVisible = false,
                score = 0,
                hearts = 5,
                cards = emptyList(),
                isResolvingPair = false
            )
            _event.emit(MatchGameEvent.Message(plan.toCompletionMessage()))
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            hasStarted = true,
            gameVisible = true,
            score = 0,
            hearts = 5,
            cards = emptyList(),
            isResolvingPair = false,
            latestMatchedWord = null,
            gameOverMessage = null,
            gameOverScore = null,
            learnedWordSummaries = emptyList()
        )
        words = plan.activeWords
        startNextRound()
    }

    private fun updatePlanSummary(plan: DailyLearningPlan) {
        val presentation = plan.toPresentation()
        _uiState.value = _uiState.value.copy(
            dailyTarget = plan.dailyTarget,
            todayWordCount = plan.todayTotalCount,
            masteredTodayCount = plan.completedCount,
            statusTitle = presentation.title,
            statusMessage = presentation.message
        )
    }

    private fun finishGame(message: String = "Game over.") {
        viewModelScope.launch {
            val score = _uiState.value.score
            val learned = serializeRecordWordProgress()
            val learnedSummaries = buildLearnedWordSummaries(learnedWordDetails)
            activeGameSessionManager.clearSession(GAME_TYPE)
            _uiState.value = _uiState.value.copy(
                hasStarted = false,
                gameVisible = false,
                cards = emptyList(),
                isResolvingPair = false,
                latestMatchedWord = null,
                gameOverMessage = message,
                gameOverScore = score,
                learnedWordSummaries = learnedSummaries
            )
            refreshPlanSummary()
            gameRecordRepository.saveRecord(GAME_TYPE, score, learned)
        }
    }

    private fun refreshPlanSummary() {
        viewModelScope.launch {
            updatePlanSummary(wordRepository.getDailyLearningPlan())
        }
    }

    private suspend fun restoreOrInitialize(avatarUrl: String) {
        val restoredSession = activeGameSessionManager.getSession(GAME_TYPE)
        if (restoredSession != null && restoredSession.hasStarted) {
            restoreSession(restoredSession, avatarUrl)
            return
        }

        val plan = wordRepository.getDailyLearningPlan()
        words = plan.activeWords
        _uiState.value = _uiState.value.copy(avatarUrl = avatarUrl, isLoading = false)
        updatePlanSummary(plan)
    }

    private suspend fun restoreSession(session: ActiveGameSession, avatarUrl: String) {
        val relatedWordIds = (
            session.roundWordIds +
                session.learnedWordIds +
                listOfNotNull(session.latestMatchedWordId)
            ).distinct()
        val wordsById = wordRepository.getWordsByIds(relatedWordIds).associateBy { it.id }
        val plan = wordRepository.getDailyLearningPlan()

        words = plan.activeWords
        roundWords = session.roundWordIds.mapNotNull(wordsById::get).associateBy { it.id }
        cursor = session.cursor
        recordWordProgress.clear()
        RecordWordProgressCodec.decodeAll(session.learnedWords).forEach { progress ->
            recordWordProgress[progress.english] = progress
        }
        learnedWordDetails.clear()
        learnedWordDetails += session.learnedWordIds.mapNotNull(wordsById::get)

        updatePlanSummary(plan)
        _uiState.value = _uiState.value.copy(
            avatarUrl = avatarUrl,
            isLoading = false,
            score = session.score,
            hearts = session.hearts,
            hasStarted = session.hasStarted,
            gameVisible = session.gameVisible,
            cards = session.cards.map { it.toUiModel() },
            latestMatchedWord = session.latestMatchedWordId?.let(wordsById::get),
            learnedWordSummaries = buildLearnedWordSummaries(learnedWordDetails)
        )
    }

    private fun updateRecordedWordProgress(matchedWord: WordItem, updatedWord: WordItem?) {
        val previous = recordWordProgress[matchedWord.english]
        val nextCorrectCount = ((previous?.correctCount ?: 0) + 1).coerceAtMost(3)
        recordWordProgress[matchedWord.english] = RecordWordProgress(
            english = matchedWord.english,
            correctCount = nextCorrectCount,
            isLearned = updatedWord?.isMastered == true || previous?.isLearned == true
        )
    }

    private fun downgradeRecordedWordProgress(word: WordItem) {
        val current = recordWordProgress[word.english] ?: return
        recordWordProgress[word.english] = current.copy(
            correctCount = current.correctCount.coerceAtMost(2).coerceAtLeast(1),
            isLearned = false
        )
    }

    private fun serializeRecordWordProgress(): List<String> {
        return RecordWordProgressCodec.encodeAll(recordWordProgress.values)
    }
    private fun publishSessionAsync() {
        viewModelScope.launch {
            publishSession()
        }
    }

    private suspend fun publishSession() {
        activeGameSessionManager.updateSession(
            ActiveGameSession(
                gameType = GAME_TYPE,
                score = _uiState.value.score,
                learnedWords = serializeRecordWordProgress(),
                hearts = _uiState.value.hearts,
                cards = _uiState.value.cards.map { it.toSnapshot() },
                roundWordIds = roundWords.values.map { it.id },
                cursor = cursor,
                hasStarted = _uiState.value.hasStarted,
                gameVisible = _uiState.value.gameVisible,
                latestMatchedWordId = _uiState.value.latestMatchedWord?.id,
                learnedWordIds = learnedWordDetails.map { it.id }.distinct()
            )
        )
    }
}

data class BreakthroughUiState(
    val isLoading: Boolean = false,
    val avatarUrl: String = "",
    val score: Int = 0,
    val hearts: Int = 5,
    val hasStarted: Boolean = false,
    val gameVisible: Boolean = false,
    val isResolvingPair: Boolean = false,
    val cards: List<MatchCardUiModel> = emptyList(),
    val dailyTarget: Int = PreferenceManager.DEFAULT_DAILY_WORD_TARGET,
    val todayWordCount: Int = 0,
    val masteredTodayCount: Int = 0,
    val statusTitle: String = "Ready to start",
    val statusMessage: String = "Tap start to load today's study words.",
    val latestMatchedWord: WordItem? = null,
    val gameOverMessage: String? = null,
    val gameOverScore: Int? = null,
    val learnedWordSummaries: List<String> = emptyList()
)


