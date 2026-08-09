package com.example.wordcrush.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class AvatarUrlFactoryTest {

    @Test
    fun createUsesCentralizedAvatarPathAndVersionQuery() {
        val factory = AvatarUrlFactory("https://example.test/word-crush/") { "alice%20smith" }

        assertEquals(
            "https://example.test/word-crush/api/user/avatar/alice%20smith?v=7",
            factory.create("alice smith", avatarVersion = 7L)
        )
    }
}
