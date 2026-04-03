package com.example.wordcrush.di

import com.example.wordcrush.data.api.AccountApi
import com.example.wordcrush.data.api.GameRecordApi
import com.example.wordcrush.data.cache.AvatarCacheStore
import com.example.wordcrush.data.local.PreferenceManager
import com.example.wordcrush.utils.AppStateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        appStateManager: AppStateManager,
        preferenceManager: PreferenceManager,
        avatarCacheStore: AvatarCacheStore
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = appStateManager.token.value.ifBlank {
                    runBlocking { preferenceManager.tokenFlow.firstOrNull().orEmpty() }
                }

                val requestBuilder = chain.request().newBuilder()
                if (token.isNotBlank()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                    requestBuilder.header("token", token)
                }
                val response = chain.proceed(requestBuilder.build())
                if (token.isNotBlank() && response.code == 401) {
                    runBlocking {
                        preferenceManager.clear()
                    }
                    avatarCacheStore.clear()
                    appStateManager.notifySessionExpired()
                }
                response
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        appStateManager: AppStateManager
    ): Retrofit {
        val baseUrl = appStateManager.domain.let { if (it.endsWith('/')) it else "$it/" }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAccountApi(retrofit: Retrofit): AccountApi {
        return retrofit.create(AccountApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGameRecordApi(retrofit: Retrofit): GameRecordApi {
        return retrofit.create(GameRecordApi::class.java)
    }
}
