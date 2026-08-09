package com.example.wordcrush.data.network

/**
 * Relative request paths used by the Android client.
 *
 * Keep these paths without a leading slash so Retrofit preserves the
 * /word-crush/ prefix from [NetworkConfig.API_BASE_URL].
 */
object ApiPaths {
    object Account {
        const val LOGIN = "api/user/login"
        const val REGISTER = "api/user/register"
        const val CHECK_TOKEN = "api/user/checkToken"
        const val CHANGE_PASSWORD = "api/user/changePassword"
        const val AVATAR = "api/user/avatar"
    }

    object GameRecord {
        const val TOP_RANKING = "api/getTopNRecord"
        const val ADD = "api/addGameRecord"
        const val DELETE = "api/deleteGameRecord"
        const val ALL = "api/getAllGameRecord"
    }

    object Learning {
        const val CATALOG = "api/learning/catalog"
        const val STATE = "api/learning/state"
        const val PLAN = "api/learning/plan"
        const val DAILY_TARGET = "api/learning/settings/daily-target"
        const val SYNC = "api/learning/sync"
    }

    object Audio {
        const val PRONUNCIATION = "dictvoice"
    }
}
