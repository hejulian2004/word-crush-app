package com.example.wordcrush.data.network

import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkConfigTest {
    @Test
    fun websocketUrlUsesWssAndNormalizesPath() {
        assertEquals(
            "https://txy.hejulian.org/word-crush/socket/match",
            NetworkConfig.websocketUrl("/socket/match").toString()
        )
    }
}
