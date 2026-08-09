package com.example.wordcrush.data.network

import com.example.wordcrush.data.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionManager.currentToken
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                header("Authorization", "Bearer $token")
            }
        }.build()

        val response = chain.proceed(request)
        if (response.code == 401 && !token.isNullOrBlank()) {
            sessionManager.invalidateFromNetwork()
        }
        return response
    }
}
