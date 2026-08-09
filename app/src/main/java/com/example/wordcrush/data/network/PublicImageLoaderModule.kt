package com.example.wordcrush.data.network

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.io.InputStream

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PublicHttpClientEntryPoint {
    @PublicHttpClient
    fun publicHttpClient(): OkHttpClient
}

@GlideModule
class PublicImageLoaderModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            PublicHttpClientEntryPoint::class.java
        )
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(entryPoint.publicHttpClient())
        )
    }
}
