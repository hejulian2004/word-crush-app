package com.example.wordcrush.domain.usecase

import android.os.SystemClock
import com.example.wordcrush.data.model.ActiveGameSession
import com.example.wordcrush.data.model.WordItem
import com.example.wordcrush.data.repository.ActiveGameSessionManager
import com.example.wordcrush.data.repository.LearningRepository
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RestoreActiveGameSessionUseCase @Inject constructor(
    private val sessionManager: ActiveGameSessionManager
) {
    suspend operator fun invoke(gameType: Int): ActiveGameSession? = sessionManager.getSession(gameType)
}

class PersistActiveGameSessionUseCase @Inject constructor(
    private val sessionManager: ActiveGameSessionManager
) {
    suspend operator fun invoke(session: ActiveGameSession) {
        sessionManager.updateSession(session)
    }
}

class ClearActiveGameSessionUseCase @Inject constructor(
    private val sessionManager: ActiveGameSessionManager
) {
    suspend operator fun invoke(gameType: Int) {
        sessionManager.clearSession(gameType)
    }
}

class PersistActiveSessionsUseCase @Inject constructor(
    private val sessionManager: ActiveGameSessionManager
) {
    suspend operator fun invoke() {
        sessionManager.persistActiveSessionsLocally()
    }
}

class GetWordsByIdsUseCase @Inject constructor(
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(ids: List<Int>): List<WordItem> = learningRepository.getWordsByIds(ids)
}

class RecordCorrectMatchUseCase @Inject constructor(
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(wordId: Int): WordItem? = learningRepository.recordCorrectMatch(wordId)
}

class RecordIncorrectMatchUseCase @Inject constructor(
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(wordIds: Set<Int>) {
        learningRepository.recordIncorrectMatch(wordIds)
    }
}

class CountdownUseCase @Inject constructor() {
    operator fun invoke(endElapsedRealtime: Long): Flow<Int> = flow {
        while (true) {
            val remainingMillis = endElapsedRealtime - SystemClock.elapsedRealtime()
            val remainingSeconds = (remainingMillis / 1_000L).toInt().coerceAtLeast(0)
            emit(remainingSeconds)
            if (remainingSeconds <= 0) return@flow
            delay(1_000L)
        }
    }
}
