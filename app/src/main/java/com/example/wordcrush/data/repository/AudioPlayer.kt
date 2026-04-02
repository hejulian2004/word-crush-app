package com.example.wordcrush.data.repository

import android.content.Context
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val client = OkHttpClient()
    private var mediaPlayer: MediaPlayer? = null
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    suspend fun play(word: String, type: Int) = withContext(ioDispatcher) {
        val request = Request.Builder()
            .url("https://dict.youdao.com/dictvoice?type=$type&audio=$word")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful || response.body == null) {
                return@use
            }
            val tempFile = File.createTempFile("word_audio_", ".mp3", context.cacheDir)
            FileOutputStream(tempFile).use { output ->
                output.write(response.body!!.bytes())
            }
            withContext(Dispatchers.Main) {
                playTempFile(tempFile)
            }
        }
    }

    private fun playTempFile(tempFile: File) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(tempFile.absolutePath)
            setOnPreparedListener { start() }
            setOnCompletionListener {
                it.release()
                if (mediaPlayer === it) {
                    mediaPlayer = null
                }
                tempFile.delete()
            }
            prepareAsync()
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
