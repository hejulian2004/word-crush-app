package com.example.wordcrush.ui.architecture

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/** State/effect store shared by ViewModels without imposing a Hilt superclass. */
class UdfStore<State, Effect>(initialState: State) {
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val effectChannel = Channel<Effect>(capacity = Channel.BUFFERED)
    val effect: Flow<Effect> = effectChannel.receiveAsFlow()

    val currentState: State
        get() = _uiState.value

    fun updateState(transform: (State) -> State) {
        _uiState.update(transform)
    }

    fun setState(state: State) {
        _uiState.value = state
    }

    fun emitEffect(effect: Effect) {
        effectChannel.trySend(effect)
    }

    fun close() {
        effectChannel.close()
    }
}
