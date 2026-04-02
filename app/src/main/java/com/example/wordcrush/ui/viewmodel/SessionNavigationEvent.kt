package com.example.wordcrush.ui.viewmodel

sealed interface SessionNavigationEvent {
    data object NavigateToLogin : SessionNavigationEvent
}
