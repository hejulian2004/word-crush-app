package com.example.wordcrush.constants

internal object AppConstants {
    object Preferences {
        const val DATA_STORE_NAME = "word_crush_preferences"
        const val TOKEN_KEY = "token"
        const val USERNAME_KEY = "username"
        const val UID_KEY = "uid"
        const val AVATAR_URL_KEY = "avatar_url"
        const val DAILY_WORD_TARGET_KEY = "daily_word_target"
        const val DAILY_PLAN_DATE_KEY = "daily_plan_date"
        const val DAILY_PLAN_WORD_IDS_KEY = "daily_plan_word_ids"
        const val ACTIVE_GAME_SESSIONS_KEY = "active_game_sessions"
        const val LEARNING_MIGRATION_COMPLETED_KEY = "learning_migration_completed"
        const val LIST_SEPARATOR = ","
    }

    object Learning {
        const val DEFAULT_DAILY_WORD_TARGET = 30
        const val MIN_DAILY_WORD_TARGET = 1
        const val MAX_DAILY_WORD_TARGET = 500
        const val REQUIRED_CORRECT_MATCHES = 3
        const val CATALOG_PAGE_SIZE = 1_000
        const val SYNC_BATCH_SIZE = 200
        const val CORRECT_MATCH = "CORRECT_MATCH"
        const val MARK_UNREMEMBERED = "MARK_UNREMEMBERED"
        const val IMPORT_SNAPSHOT = "IMPORT_SNAPSHOT"
        const val UPDATE_DAILY_TARGET = "UPDATE_DAILY_TARGET"
    }

    object Game {
        const val DEFAULT_DURATION_SECONDS = 180
        const val MAX_HEARTS = 5
        const val ROUND_PAIR_COUNT = 6
        const val SCORE_HEART_REWARD_INTERVAL = 6
        const val MAX_LEARNED_SUMMARIES = 6
    }

    object Ranking {
        const val DEFAULT_LIMIT = 50
        const val HIGHLIGHT_COUNT = 3
    }

    object Auth {
        const val MIN_PASSWORD_LENGTH = 6
    }

    object Avatar {
        const val MAX_AVATAR_BYTES = 1024 * 1024
        const val MAX_BITMAP_EDGE = 1_280
        const val MIN_BITMAP_EDGE = 320
        const val JPEG_MIME_TYPE = "image/jpeg"
        const val FORM_FIELD_NAME = "file"
        const val FALLBACK_FILE_NAME = "avatar"
        const val HTTP_SCHEME = "http://"
        const val HTTPS_SCHEME = "https://"
        const val FILE_EXTENSION = ".jpg"
    }

    object Audio {
        const val MAX_CACHE_FILES = 30
        const val CACHE_DIRECTORY = "word_pronunciation_cache"
        const val TYPE_QUERY_PARAMETER = "type"
        const val AUDIO_QUERY_PARAMETER = "audio"
        const val DOWNLOAD_SUFFIX = ".download"
        const val FILE_EXTENSION = "mp3"
        const val DIGEST_ALGORITHM = "SHA-256"
        const val DIGEST_BYTE_FORMAT = "%02x"
        const val CACHE_KEY_PREFIX = "audio_"
    }

    object Cache {
        const val MAX_CACHED_USERS = 50
        const val MAX_RANKING_CACHE_ENTRIES = 6
    }

    object WordBook {
        const val PAGE_SIZE = 40
        const val LOAD_MORE_THRESHOLD = 6
        const val DATE_FORMAT = "yyyy-MM-dd"
    }

    object Records {
        const val TIME_FORMAT = "yyyy-MM-dd-HH:mm:ss.SSS"
        const val SNAPSHOT_SEPARATOR = "|"
        const val WORDS_SEPARATOR = ","
        const val PROGRESS_SEPARATOR = "|#|"
        const val PROGRESS_PART_COUNT = 3
    }

    object Logging {
        const val TAG = "WordCrush"
        const val IS_DEBUG = true
    }

    object Animation {
        const val MATCHED_ALPHA_LABEL = "matchedAlpha"
        const val MATCHED_SCALE_LABEL = "matchedScale"
    }
}
