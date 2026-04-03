package com.example.wordcrush.data.repository

import android.content.Context
import android.media.MediaPlayer
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
    private val client: OkHttpClient
) {
    private companion object {
        const val MAX_AUDIO_CACHE_FILES = 30
        const val AUDIO_CACHE_DIR = "word_pronunciation_cache"
    }

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val cacheMutex = Mutex()
    private val cacheDirectory: File by lazy {
        File(context.cacheDir, AUDIO_CACHE_DIR).apply { mkdirs() }
    }
    private val cachedFiles = LinkedHashMap<String, File>(MAX_AUDIO_CACHE_FILES, 0.75f, true)
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
        val url = "https://dict.youdao.com/dictvoice".toHttpUrl().newBuilder()
            .addQueryParameter("type", type.toString())
            .addQueryParameter("audio", word)
            .build()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful || response.body == null) {
                throw IllegalStateException("Unable to load pronunciation audio.")
            }

            val tempFile = File.createTempFile(cacheKey, ".download", cacheDirectory)
            FileOutputStream(tempFile).use { output ->
                output.write(response.body!!.bytes())
            }

            val targetFile = File(cacheDirectory, "$cacheKey.mp3")
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
            ?.filter { it.isFile && it.extension.equals("mp3", ignoreCase = true) }
            ?.sortedBy { it.lastModified() }
            ?.forEach { file ->
                cachedFiles[file.nameWithoutExtension] = file
            }
        trimCacheLocked()
        cacheIndexInitialized = true
    }

    private fun trimCacheLocked() {
        val iterator = cachedFiles.entries.iterator()
        while (cachedFiles.size > MAX_AUDIO_CACHE_FILES && iterator.hasNext()) {
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
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(normalized.toByteArray())
            .joinToString(separator = "") { byte -> "%02x".format(byte) }
        return "audio_$digest"
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
