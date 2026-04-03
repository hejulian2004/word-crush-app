package com.example.wordcrush.data.model

data class RecordWordProgress(
    val english: String,
    val correctCount: Int,
    val isLearned: Boolean
) {
    val displayLabel: String
        get() = if (isLearned) {
            "$english Learned"
        } else {
            "$english ${correctCount.coerceIn(1, 3)}/3"
        }
}

object RecordWordProgressCodec {
    private const val SEPARATOR = "|#|"

    fun encode(progress: RecordWordProgress): String {
        return listOf(
            progress.english,
            progress.correctCount.coerceIn(1, 3).toString(),
            progress.isLearned.toString()
        ).joinToString(SEPARATOR)
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

        val parts = normalized.split(SEPARATOR)
        if (parts.size != 3) {
            return RecordWordProgress(
                english = normalized,
                correctCount = 3,
                isLearned = true
            )
        }

        val english = parts[0].trim().ifBlank { return null }
        val correctCount = parts[1].toIntOrNull()?.coerceIn(1, 3) ?: 1
        val isLearned = parts[2].toBooleanStrictOrNull() ?: false
        return RecordWordProgress(
            english = english,
            correctCount = correctCount,
            isLearned = isLearned
        )
    }
}
