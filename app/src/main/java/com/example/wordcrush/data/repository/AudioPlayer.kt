package com.example.wordcrush.data.repository

import android.content.Context
import android.media.MediaPlayer
import com.example.wordcrush.constants.AppConstants
import com.example.wordcrush.constants.AppStrings
import com.example.wordcrush.data.network.ApiPaths
import com.example.wordcrush.data.network.NetworkConfig
import com.example.wordcrush.data.network.PublicHttpClient
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

@Singleton
class AudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
    @PublicHttpClient
    private val client: OkHttpClient
) {
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val cacheMutex = Mutex()
    private val cacheDirectory: File by lazy {
        File(context.cacheDir, AppConstants.Audio.CACHE_DIRECTORY).apply { mkdirs() }
    }
    private val cachedFiles = LinkedHashMap<String, File>(
        AppConstants.Audio.MAX_CACHE_FILES,
        0.75f,
        true
    )
    private var cacheIndexInitialized = false
    private var mediaPlayer: MediaPlayer? = null

    suspend fun play(word: String, type: Int) = withContext(ioDispatcher) {
        val audioFile = resolveAudioFile(word, type)
        withContext(Dispatchers.Main) {
            playCachedFile(audioFile)
        }
    }

    private suspend fun resolveAudioFile(word: String, type: Int): File {
        val cacheKey = buildCacheKey(word, type)
        cacheMutex.withLock {
            ensureCacheIndexLocked()
            val cachedFile = cachedFiles[cacheKey]?.takeIf { it.exists() }
            if (cachedFile != null) {
                touchFile(cachedFile)
                return cachedFile
            }
        }

        val downloadedFile = downloadAudioFile(word, type, cacheKey)
        return cacheMutex.withLock {
            ensureCacheIndexLocked()
            val cachedFile = cachedFiles[cacheKey]?.takeIf { it.exists() }
            if (cachedFile != null) {
                downloadedFile.delete()
                touchFile(cachedFile)
                return@withLock cachedFile
            }

            touchFile(downloadedFile)
            cachedFiles[cacheKey] = downloadedFile
            trimCacheLocked()
            downloadedFile
        }
    }

    private fun playCachedFile(audioFile: File) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioFile.absolutePath)
            setOnPreparedListener { start() }
            setOnCompletionListener {
                it.release()
                if (mediaPlayer === it) {
                    mediaPlayer = null
                }
            }
            prepareAsync()
        }
    }

    private fun downloadAudioFile(word: String, type: Int, cacheKey: String): File {
        val url = "${NetworkConfig.AUDIO_BASE_URL}${ApiPaths.Audio.PRONUNCIATION}"
            .toHttpUrl().newBuilder()
            .addQueryParameter(AppConstants.Audio.TYPE_QUERY_PARAMETER, type.toString())
            .addQueryParameter(AppConstants.Audio.AUDIO_QUERY_PARAMETER, word)
            .build()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful || response.body == null) {
                throw IllegalStateException(AppStrings.Errors.AUDIO_LOAD_FAILED)
            }

            val tempFile = File.createTempFile(
                cacheKey,
                AppConstants.Audio.DOWNLOAD_SUFFIX,
                cacheDirectory
            )
            FileOutputStream(tempFile).use { output ->
                output.write(response.body!!.bytes())
            }

            val targetFile = File(
                cacheDirectory,
                "$cacheKey.${AppConstants.Audio.FILE_EXTENSION}"
            )
            if (targetFile.exists()) {
                targetFile.delete()
            }
            if (!tempFile.renameTo(targetFile)) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }
            return targetFile
        }
    }

    private fun ensureCacheIndexLocked() {
        if (cacheIndexInitialized) {
            return
        }

        cacheDirectory.listFiles()
            ?.filter {
                it.isFile && it.extension.equals(AppConstants.Audio.FILE_EXTENSION, ignoreCase = true)
            }
            ?.sortedBy { it.lastModified() }
            ?.forEach { file ->
                cachedFiles[file.nameWithoutExtension] = file
            }
        trimCacheLocked()
        cacheIndexInitialized = true
    }

    private fun trimCacheLocked() {
        val iterator = cachedFiles.entries.iterator()
        while (cachedFiles.size > AppConstants.Audio.MAX_CACHE_FILES && iterator.hasNext()) {
            val eldest = iterator.next()
            if (eldest.value.exists()) {
                eldest.value.delete()
            }
            iterator.remove()
        }
    }

    private fun touchFile(file: File) {
        file.setLastModified(System.currentTimeMillis())
    }

    private fun buildCacheKey(word: String, type: Int): String {
        val normalized = "${type}:${word.trim().lowercase(Locale.US)}"
        val digest = MessageDigest.getInstance(AppConstants.Audio.DIGEST_ALGORITHM)
            .digest(normalized.toByteArray())
            .joinToString(separator = "") { byte ->
                AppConstants.Audio.DIGEST_BYTE_FORMAT.format(byte)
            }
        return AppConstants.Audio.CACHE_KEY_PREFIX + digest
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
