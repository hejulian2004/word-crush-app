package com.example.wordcrush.data.model

import com.example.wordcrush.constants.AppConstants

data class ActiveGameSession(
    val gameType: Int,
    val score: Int,
    val learnedWords: List<String>,
    val hearts: Int = AppConstants.Game.MAX_HEARTS,
    val cards: List<ActiveMatchCardSnapshot> = emptyList(),
    val roundWordIds: List<Int> = emptyList(),
    val cursor: Int = 0,
    val hasStarted: Boolean = false,
    val gameVisible: Boolean = false,
    val latestMatchedWordId: Int? = null,
    val learnedWordIds: List<Int> = emptyList(),
    val remainingSeconds: Int? = null,
    val timerEndElapsedRealtime: Long? = null
)

data class ActiveMatchCardSnapshot(
    val id: Int,
    val wordId: Int,
    val text: String,
    val pronunciation: String? = null,
    val type: String,
    val isMatched: Boolean = false,
    val isSelected: Boolean = false,
    val feedback: String = "NONE"
)
