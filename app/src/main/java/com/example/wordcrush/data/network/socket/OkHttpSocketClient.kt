package com.example.wordcrush.data.network.socket

import com.example.wordcrush.data.network.NetworkConfig
import com.example.wordcrush.data.session.SessionManager
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
class OkHttpSocketClient(
    private val client: OkHttpClient,
    private val sessionManager: SessionManager,
    private val authenticated: Boolean
) : SocketClient {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow<SocketState>(SocketState.Idle)
    override val state = _state.asStateFlow()
    private val _events = MutableSharedFlow<SocketEvent>(extraBufferCapacity = 32)
    override val events = _events.asSharedFlow()

    private var socket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var path: String? = null
    private var reconnectAttempt = 0
    private val explicitlyClosed = AtomicBoolean(false)

    override fun connect(path: String) {
        explicitlyClosed.set(false)
        this.path = path
        reconnectJob?.cancel()
        openSocket()
    }

    override fun send(text: String): Boolean = socket?.send(text) == true

    override fun send(bytes: ByteArray): Boolean = socket?.send(ByteString.of(*bytes)) == true

    override fun close() {
        explicitlyClosed.set(true)
        reconnectJob?.cancel()
        reconnectJob = null
        socket?.close(1000, "client closed")
        socket = null
        _state.value = SocketState.Closed
    }

    private fun openSocket() {
        val currentPath = path ?: return
        if (authenticated && sessionManager.currentToken.isNullOrBlank()) {
            _state.value = SocketState.Failed("Authentication is required for this socket.")
            return
        }

        _state.value = if (reconnectAttempt == 0) {
            SocketState.Connecting
        } else {
            SocketState.Reconnecting(reconnectAttempt, backoffMillis(reconnectAttempt))
        }

        val request = Request.Builder()
            .url(NetworkConfig.websocketUrl(currentPath))
            .apply {
                if (authenticated) {
                    sessionManager.currentToken?.let { token ->
                        header("Authorization", "Bearer $token")
                    }
                }
            }
            .build()

        socket = client.newWebSocket(request, listener)
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            socket = webSocket
            reconnectAttempt = 0
            _state.value = SocketState.Connected
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            _events.tryEmit(SocketEvent.Text(text))
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            _events.tryEmit(SocketEvent.Binary(bytes.toByteArray()))
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            _events.tryEmit(SocketEvent.Closed(code, reason))
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            socket = null
            if (!explicitlyClosed.get()) {
                scheduleReconnect()
            } else {
                _state.value = SocketState.Closed
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            socket = null
            if (response?.code == 401 && authenticated) {
                sessionManager.invalidateFromNetwork()
                _state.value = SocketState.Failed("Socket authentication failed.")
                _events.tryEmit(SocketEvent.Failure("Socket authentication failed."))
                return
            }
            _events.tryEmit(SocketEvent.Failure(t.message ?: "Socket connection failed."))
            if (!explicitlyClosed.get()) {
                scheduleReconnect()
            }
        }
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectAttempt += 1
        val delayMillis = backoffMillis(reconnectAttempt)
        reconnectJob = scope.launch {
            _state.value = SocketState.Reconnecting(reconnectAttempt, delayMillis)
            delay(delayMillis)
            if (!explicitlyClosed.get()) {
                openSocket()
            }
        }
    }

    private fun backoffMillis(attempt: Int): Long {
        return (1000L shl (attempt - 1).coerceIn(0, 4)).coerceAtMost(30_000L)
    }
}
