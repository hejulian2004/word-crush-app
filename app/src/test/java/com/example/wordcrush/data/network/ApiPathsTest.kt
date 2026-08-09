package com.example.wordcrush.data.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ApiPathsTest {

    @Test
    fun pathsAreGroupedByFeatureAndRemainRelative() {
        val paths = listOf(
            ApiPaths.Account.LOGIN,
            ApiPaths.Account.REGISTER,
            ApiPaths.Account.CHECK_TOKEN,
            ApiPaths.Account.CHANGE_PASSWORD,
            ApiPaths.Account.AVATAR,
            ApiPaths.GameRecord.TOP_RANKING,
            ApiPaths.GameRecord.ADD,
            ApiPaths.GameRecord.DELETE,
            ApiPaths.GameRecord.ALL,
            ApiPaths.Learning.CATALOG,
            ApiPaths.Learning.STATE,
            ApiPaths.Learning.PLAN,
            ApiPaths.Learning.DAILY_TARGET,
            ApiPaths.Learning.SYNC,
            ApiPaths.Audio.PRONUNCIATION
        )

        assertEquals(15, paths.size)
        assertFalse(paths.any { it.startsWith("/") })
        assertEquals("api/user/login", ApiPaths.Account.LOGIN)
        assertEquals("api/getTopNRecord", ApiPaths.GameRecord.TOP_RANKING)
        assertEquals("api/learning/sync", ApiPaths.Learning.SYNC)
        assertEquals("dictvoice", ApiPaths.Audio.PRONUNCIATION)
    }
}
