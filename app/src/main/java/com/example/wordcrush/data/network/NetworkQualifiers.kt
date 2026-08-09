package com.example.wordcrush.data.network

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PublicHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PublicRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PublicWebSocketClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedWebSocketClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PublicWebSocket

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedWebSocket
