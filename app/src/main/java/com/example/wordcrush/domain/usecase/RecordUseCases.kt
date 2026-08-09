package com.example.wordcrush.domain.usecase

import com.example.wordcrush.data.model.GameRecordItem
import com.example.wordcrush.data.repository.GameRecordRepository
import javax.inject.Inject

class GetRankingUseCase @Inject constructor(
    private val gameRecordRepository: GameRecordRepository
) {
    suspend operator fun invoke(gameType: Int, limit: Int) = gameRecordRepository.getRanking(gameType, limit)

    suspend fun cached(gameType: Int, limit: Int) =
        gameRecordRepository.getCachedRanking(gameType, limit)
}

class GetGameRecordsUseCase @Inject constructor(
    private val gameRecordRepository: GameRecordRepository
) {
    suspend operator fun invoke() = gameRecordRepository.getLocalRecords()
}

class DeleteGameRecordUseCase @Inject constructor(
    private val gameRecordRepository: GameRecordRepository
) {
    suspend operator fun invoke(record: GameRecordItem) = gameRecordRepository.deleteRecord(record)
}

class SaveGameRecordUseCase @Inject constructor(
    private val gameRecordRepository: GameRecordRepository
) {
    suspend operator fun invoke(gameType: Int, score: Int, learnedWords: List<String>) =
        gameRecordRepository.saveRecord(gameType, score, learnedWords)
}
