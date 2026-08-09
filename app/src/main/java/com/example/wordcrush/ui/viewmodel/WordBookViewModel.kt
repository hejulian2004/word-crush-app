package com.example.wordcrush.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordcrush.constants.AppConstants
import com.example.wordcrush.constants.AppStrings
import com.example.wordcrush.data.model.WordItem
import com.example.wordcrush.domain.usecase.SearchWordsUseCase
import com.example.wordcrush.domain.usecase.UpdateWordMasteryUseCase
import com.example.wordcrush.domain.usecase.WordFilter as DomainWordFilter
import com.example.wordcrush.ui.architecture.UdfStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

typealias WordFilter = DomainWordFilter

sealed interface WordBookAction {
    data object Refresh : WordBookAction
    data class QueryChanged(val value: String) : WordBookAction
    data class FilterChanged(val filter: WordFilter) : WordBookAction
    data object ApplySearch : WordBookAction
    data object LoadMore : WordBookAction
    data class MasteryChanged(val word: WordItem, val isMastered: Boolean) : WordBookAction
    data class PlayAudio(val word: WordItem, val type: Int) : WordBookAction
}

sealed interface WordBookEffect {
    data class PlayAudio(val word: String, val type: Int) : WordBookEffect
}

@HiltViewModel
class WordBookViewModel @Inject constructor(
    private val searchWordsUseCase: SearchWordsUseCase,
    private val updateWordMasteryUseCase: UpdateWordMasteryUseCase
) : ViewModel() {
    private var matchingWords: List<WordItem> = emptyList()
    private var searchJob: Job? = null

    private val store = UdfStore<WordBookUiState, WordBookEffect>(WordBookUiState())
    val uiState = store.uiState
    val effect = store.effect

    private val currentState: WordBookUiState
        get() = store.currentState

    private fun updateState(transform: (WordBookUiState) -> WordBookUiState) = store.updateState(transform)
    private fun emitEffect(effect: WordBookEffect) = store.emitEffect(effect)

    init {
        onAction(WordBookAction.Refresh)
    }

    fun onAction(action: WordBookAction) {
        when (action) {
            WordBookAction.Refresh -> refresh()
            is WordBookAction.QueryChanged -> {
                updateState { it.copy(query = action.value) }
                scheduleSearch()
            }
            is WordBookAction.FilterChanged -> {
                updateState { it.copy(filter = action.filter) }
                searchJob?.cancel()
                refresh()
            }
            WordBookAction.ApplySearch -> {
                searchJob?.cancel()
                refresh()
            }
            WordBookAction.LoadMore -> loadMore()
            is WordBookAction.MasteryChanged -> updateMastery(action.word, action.isMastered)
            is WordBookAction.PlayAudio -> emitEffect(
                WordBookEffect.PlayAudio(action.word.english, action.type)
            )
        }
    }

    private fun refresh() {
        val query = currentState.query
        val filter = currentState.filter
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, isAppending = false) }
            runCatching { searchWordsUseCase(query, filter) }
                .onSuccess { words ->
                    matchingWords = words
                    val visibleWords = words.take(AppConstants.WordBook.PAGE_SIZE)
                    updateState {
                        it.copy(
                            isLoading = false,
                            words = visibleWords,
                            canLoadMore = visibleWords.size < words.size
                        ).withEmptyState()
                    }
                }
                .onFailure { error ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            words = emptyList(),
                            canLoadMore = false,
                            emptyStateTitle = AppStrings.Errors.LOAD_WORDS_TITLE,
                            emptyStateMessage = error.message ?: AppStrings.Errors.LOAD_WORDS_FAILED
                        )
                    }
                }
        }
    }

    private fun updateMastery(word: WordItem, isMastered: Boolean) {
        viewModelScope.launch {
            val updatedWord = updateWordMasteryUseCase(word.id, isMastered) ?: return@launch
            matchingWords = matchingWords.mapNotNull { currentWord ->
                when {
                    currentWord.id != updatedWord.id -> currentWord
                    shouldKeepWordInCurrentFilter(updatedWord, currentState.filter) -> updatedWord
                    else -> null
                }
            }
            val visibleCount = minOf(currentState.words.size, matchingWords.size)
            updateState {
                it.copy(
                    words = matchingWords.take(visibleCount),
                    canLoadMore = visibleCount < matchingWords.size
                ).withEmptyState()
            }
        }
    }

    private fun loadMore() {
        val state = currentState
        if (state.isLoading || state.isAppending || !state.canLoadMore) return
        viewModelScope.launch {
            updateState { it.copy(isAppending = true) }
            val nextCount = minOf(
                state.words.size + AppConstants.WordBook.PAGE_SIZE,
                matchingWords.size
            )
            updateState {
                it.copy(
                    words = matchingWords.take(nextCount),
                    isAppending = false,
                    canLoadMore = nextCount < matchingWords.size
                )
            }
        }
    }

    private fun scheduleSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            refresh()
        }
    }

    private fun shouldKeepWordInCurrentFilter(word: WordItem, filter: WordFilter): Boolean {
        return when (filter) {
            WordFilter.ALL -> true
            WordFilter.MASTERED -> word.isMastered
            WordFilter.UNMASTERED -> !word.isMastered
        }
    }

    private fun WordBookUiState.withEmptyState(): WordBookUiState {
        val normalizedQuery = query.trim()
        val (title, message) = when {
            normalizedQuery.isNotBlank() && filter == WordFilter.ALL ->
                AppStrings.WordBook.NO_SEARCH_RESULTS to
                    AppStrings.WordBook.noWordsMatch(normalizedQuery)
            normalizedQuery.isNotBlank() && filter == WordFilter.MASTERED ->
                AppStrings.WordBook.NO_REMEMBERED_WORDS_FOUND to
                    AppStrings.WordBook.noRememberedMatch(normalizedQuery)
            normalizedQuery.isNotBlank() && filter == WordFilter.UNMASTERED ->
                AppStrings.WordBook.NO_LEARNING_WORDS_FOUND to
                    AppStrings.WordBook.noLearningMatch(normalizedQuery)
            filter == WordFilter.MASTERED ->
                AppStrings.WordBook.NO_REMEMBERED_WORDS_YET to AppStrings.WordBook.rememberedWordsHint()
            filter == WordFilter.UNMASTERED ->
                AppStrings.WordBook.NO_LEARNING_WORDS to AppStrings.WordBook.learningWordsHint()
            else ->
                AppStrings.WordBook.NO_WORDS_FOUND to AppStrings.WordBook.emptyBookHint()
        }
        return copy(emptyStateTitle = title, emptyStateMessage = message)
    }

    override fun onCleared() {
        searchJob?.cancel()
        store.close()
        super.onCleared()
    }
}

data class WordBookUiState(
    val isLoading: Boolean = false,
    val isAppending: Boolean = false,
    val query: String = "",
    val filter: WordFilter = WordFilter.ALL,
    val words: List<WordItem> = emptyList(),
    val canLoadMore: Boolean = false,
    val emptyStateTitle: String = AppStrings.WordBook.NO_WORDS_FOUND,
    val emptyStateMessage: String = AppStrings.WordBook.TRY_ANOTHER_KEYWORD
)
