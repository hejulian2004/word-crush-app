package com.example.wordcrush.ui.architecture

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class UdfStoreTest {
    @Test
    fun effectIsDeliveredAsOneShotFlow() = runBlocking {
        val store = UdfStore<Int, String>(0)
        store.emitEffect("show message")

        assertEquals("show message", store.effect.first())
        store.close()
    }
}
