package com.example.wordcrush.data.model

import com.example.wordcrush.constants.AppConstants
import com.example.wordcrush.constants.AppStrings

data class RecordWordProgress(
    val english: String,
    val correctCount: Int,
    val isLearned: Boolean
) {
    val displayLabel: String
        get() = AppStrings.Records.progressLabel(english, correctCount, isLearned)
}

object RecordWordProgressCodec {
    fun encode(progress: RecordWordProgress): String {
        return listOf(
            progress.english,
            progress.correctCount.coerceIn(
                1,
                AppConstants.Learning.REQUIRED_CORRECT_MATCHES
            ).toString(),
            progress.isLearned.toString()
        ).joinToString(AppConstants.Records.PROGRESS_SEPARATOR)
    }

    fun encodeAll(progressItems: Collection<RecordWordProgress>): List<String> {
        return progressItems.map(::encode)
    }

    fun decodeAll(rawItems: List<String>): List<RecordWordProgress> {
        return rawItems.mapNotNull(::decode)
    }

    fun decode(raw: String): RecordWordProgress? {
        val normalized = raw.trim()
        if (normalized.isBlank()) {
            return null
        }

        val parts = normalized.split(AppConstants.Records.PROGRESS_SEPARATOR)
        if (parts.size != AppConstants.Records.PROGRESS_PART_COUNT) {
            return RecordWordProgress(
                english = normalized,
                correctCount = AppConstants.Learning.REQUIRED_CORRECT_MATCHES,
                isLearned = true
            )
        }

        val english = parts[0].trim().ifBlank { return null }
        val correctCount = parts[1].toIntOrNull()?.coerceIn(
            1,
            AppConstants.Learning.REQUIRED_CORRECT_MATCHES
        ) ?: 1
        val isLearned = parts[2].toBooleanStrictOrNull() ?: false
        return RecordWordProgress(
            english = english,
            correctCount = correctCount,
            isLearned = isLearned
        )
    }
}
