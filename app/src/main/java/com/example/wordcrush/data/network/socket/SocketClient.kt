package com.example.wordcrush.data.network.socket

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface SocketState {
    data object Idle : SocketState
    data object Connecting : SocketState
    data object Connected : SocketState
    data class Reconnecting(val attempt: Int, val delayMillis: Long) : SocketState
    data class Failed(val message: String) : SocketState
    data object Closed : SocketState
}

sealed interface SocketEvent {
    data class Text(val value: String) : SocketEvent
    data class Binary(val value: ByteArray) : SocketEvent
    data class Closed(val code: Int, val reason: String) : SocketEvent
    data class Failure(val message: String) : SocketEvent
}

interface SocketClient {
    val state: StateFlow<SocketState>
    val events: SharedFlow<SocketEvent>

    fun connect(path: String)
    fun send(text: String): Boolean
    fun send(bytes: ByteArray): Boolean
    fun close()
}
