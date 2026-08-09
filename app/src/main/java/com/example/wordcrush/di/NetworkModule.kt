package com.example.wordcrush.di

import com.example.wordcrush.data.api.AuthenticatedAccountApi
import com.example.wordcrush.data.api.AuthenticatedGameApi
import com.example.wordcrush.data.api.LearningApi
import com.example.wordcrush.data.api.PublicAccountApi
import com.example.wordcrush.data.api.PublicGameApi
import com.example.wordcrush.data.network.AuthenticatedHttpClient
import com.example.wordcrush.data.network.AuthenticatedRetrofit
import com.example.wordcrush.data.network.AuthInterceptor
import com.example.wordcrush.data.network.NetworkConfig
import com.example.wordcrush.data.network.PublicHttpClient
import com.example.wordcrush.data.network.PublicRetrofit
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    @PublicHttpClient
    fun providePublicHttpClient(): OkHttpClient {
        return httpClientBuilder()
            .build()
    }

    @Provides
    @Singleton
    @AuthenticatedHttpClient
    fun provideAuthenticatedHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return httpClientBuilder()
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @PublicRetrofit
    fun providePublicRetrofit(
        @PublicHttpClient client: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return retrofitBuilder(client, gson)
    }

    @Provides
    @Singleton
    @AuthenticatedRetrofit
    fun provideAuthenticatedRetrofit(
        @AuthenticatedHttpClient client: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return retrofitBuilder(client, gson)
    }

    @Provides
    @Singleton
    fun providePublicAccountApi(@PublicRetrofit retrofit: Retrofit): PublicAccountApi =
        retrofit.create(PublicAccountApi::class.java)

    @Provides
    @Singleton
    fun provideAuthenticatedAccountApi(@AuthenticatedRetrofit retrofit: Retrofit): AuthenticatedAccountApi =
        retrofit.create(AuthenticatedAccountApi::class.java)

    @Provides
    @Singleton
    fun providePublicGameApi(@PublicRetrofit retrofit: Retrofit): PublicGameApi =
        retrofit.create(PublicGameApi::class.java)

    @Provides
    @Singleton
    fun provideAuthenticatedGameApi(@AuthenticatedRetrofit retrofit: Retrofit): AuthenticatedGameApi =
        retrofit.create(AuthenticatedGameApi::class.java)

    @Provides
    @Singleton
    fun provideLearningApi(@AuthenticatedRetrofit retrofit: Retrofit): LearningApi =
        retrofit.create(LearningApi::class.java)

    private fun httpClientBuilder(): OkHttpClient.Builder {
        val logging = HttpLoggingInterceptor().apply {
            level = if (com.example.wordcrush.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            redactHeader("Authorization")
            redactHeader("token")
            redactHeader("Cookie")
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
    }

    private fun retrofitBuilder(client: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NetworkConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
