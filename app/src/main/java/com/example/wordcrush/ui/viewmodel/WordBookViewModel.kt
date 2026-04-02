package com.example.wordcrush.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordcrush.data.model.WordItem
import com.example.wordcrush.data.repository.WordRepository
import com.example.wordcrush.ui.model.MatchGameEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class WordFilter {
    ALL,
    MASTERED,
    UNMASTERED
}

@HiltViewModel
class WordBookViewModel @Inject constructor(
    private val wordRepository: WordRepository
) : ViewModel() {
    private companion object {
        const val PAGE_SIZE = 40
    }

    private val _uiState = MutableStateFlow(WordBookUiState())
    val uiState: StateFlow<WordBookUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<MatchGameEvent>()
    val event: SharedFlow<MatchGameEvent> = _event.asSharedFlow()
    private var filteredWords: List<WordItem> = emptyList()
    private var searchJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isAppending = false)
            val words = loadByCurrentFilter(_uiState.value.query, _uiState.value.filter)
            filteredWords = words
            val initialWords = filteredWords.take(PAGE_SIZE)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                words = initialWords,
                canLoadMore = initialWords.size < filteredWords.size
            )
            updateEmptyState()
        }
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        scheduleSearch()
    }

    fun applySearch() {
        searchJob?.cancel()
        refresh()
    }

    fun updateFilter(filter: WordFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
        searchJob?.cancel()
        refresh()
    }

    fun updateMastery(word: WordItem, isMastered: Boolean) {
        viewModelScope.launch {
            val updatedWord = wordRepository.updateWordMastered(word.id, isMastered) ?: return@launch
            filteredWords = filteredWords.mapNotNull { currentWord ->
                when {
                    currentWord.id != updatedWord.id -> currentWord
                    shouldKeepWordInCurrentFilter(updatedWord, _uiState.value.filter) -> updatedWord
                    else -> null
                }
            }

            val currentVisibleCount = _uiState.value.words.size
            val nextVisibleCount = minOf(currentVisibleCount, filteredWords.size)
            _uiState.value = _uiState.value.copy(
                words = filteredWords.take(nextVisibleCount),
                canLoadMore = nextVisibleCount < filteredWords.size
            )
            updateEmptyState()
        }
    }

    fun playAudio(word: WordItem, type: Int) {
        viewModelScope.launch {
            _event.emit(MatchGameEvent.PlayAudio(word.english, type))
        }
    }

    fun loadMore() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isAppending || !currentState.canLoadMore) {
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isAppending = true)
            val nextCount = minOf(currentState.words.size + PAGE_SIZE, filteredWords.size)
            _uiState.value = _uiState.value.copy(
                words = filteredWords.take(nextCount),
                isAppending = false,
                canLoadMore = nextCount < filteredWords.size
            )
        }
    }

    private fun scheduleSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            refresh()
        }
    }

    private fun updateEmptyState() {
        val query = _uiState.value.query.trim()
        val filter = _uiState.value.filter
        val (title, message) = when {
            query.isNotBlank() && filter == WordFilter.ALL ->
                "No search results" to "No words match \"$query\". Try another keyword."
            query.isNotBlank() && filter == WordFilter.MASTERED ->
                "No remembered words found" to "No remembered words match \"$query\"."
            query.isNotBlank() && filter == WordFilter.UNMASTERED ->
                "No learning words found" to "No learning words match \"$query\"."
            filter == WordFilter.MASTERED ->
                "No remembered words yet" to "Words marked as remembered will appear here."
            filter == WordFilter.UNMASTERED ->
                "No learning words" to "Your current learning list is empty."
            else ->
                "No words found" to "Your word book is empty right now."
        }
        _uiState.value = _uiState.value.copy(
            emptyStateTitle = title,
            emptyStateMessage = message
        )
    }

    private suspend fun loadByCurrentFilter(query: String, filter: WordFilter): List<WordItem> {
        val baseWords = when (filter) {
            WordFilter.ALL -> wordRepository.getWords()
            WordFilter.MASTERED -> wordRepository.getWordsByMastered(true)
            WordFilter.UNMASTERED -> wordRepository.getWordsByMastered(false)
        }

        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            return baseWords
        }

        return baseWords.filter { word ->
            word.english.contains(normalizedQuery, ignoreCase = true) ||
                word.chinese.contains(normalizedQuery, ignoreCase = true) ||
                word.pronunciation.contains(normalizedQuery, ignoreCase = true)
        }
    }

    private fun shouldKeepWordInCurrentFilter(word: WordItem, filter: WordFilter): Boolean {
        return when (filter) {
            WordFilter.ALL -> true
            WordFilter.MASTERED -> word.isMastered
            WordFilter.UNMASTERED -> !word.isMastered
        }
    }
}

data class WordBookUiState(
    val isLoading: Boolean = false,
    val isAppending: Boolean = false,
    val query: String = "",
    val filter: WordFilter = WordFilter.ALL,
    val words: List<WordItem> = emptyList(),
    val canLoadMore: Boolean = false,
    val emptyStateTitle: String = "No words found",
    val emptyStateMessage: String = "Try another keyword or reset the filter."
)
