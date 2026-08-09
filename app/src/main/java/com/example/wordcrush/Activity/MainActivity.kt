package com.example.wordcrush.Activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.wordcrush.data.repository.AudioPlayer
import com.example.wordcrush.domain.usecase.PersistActiveSessionsUseCase
import com.example.wordcrush.ui.compose.WordCrushApp
import com.example.wordcrush.ui.compose.theme.WordCrushTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var audioPlayer: AudioPlayer

    @Inject
    lateinit var persistActiveSessionsUseCase: PersistActiveSessionsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordCrushTheme {
                WordCrushApp(
                    onPlayAudio = { word, type ->
                        CoroutineScope(Dispatchers.Main.immediate).launch {
                            audioPlayer.play(word, type)
                        }
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing && !isChangingConfigurations) {
            runBlocking {
                persistActiveSessionsUseCase()
            }
        }
        super.onDestroy()
        audioPlayer.release()
    }
}
