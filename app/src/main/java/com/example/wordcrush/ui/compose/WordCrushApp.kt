package com.example.wordcrush.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wordcrush.ui.viewmodel.MainViewModel
import com.example.wordcrush.ui.viewmodel.MainAction
import com.example.wordcrush.ui.viewmodel.MainEffect

internal enum class RootFlow {
    Loading,
    Auth,
    Main
}

internal object AuthRoute {
    const val Login = "login"
    const val Register = "register"
}

internal object MainRoute {
    const val Breakthrough = "breakthrough"
    const val WordBook = "word_book"
    const val Home = "home"
    const val RankingPattern = "ranking/{gameType}"
    const val Records = "records"

    fun ranking(gameType: Int): String = "ranking/$gameType"
}

internal data class TopLevelDestination(
    val route: String,
    val label: String
)

internal val mainDestinations = listOf(
    TopLevelDestination(MainRoute.Breakthrough, "Match"),
    TopLevelDestination(MainRoute.WordBook, "Words"),
    TopLevelDestination(MainRoute.Home, "Profile")
)

@Composable
fun WordCrushApp(
    onPlayAudio: suspend (String, Int) -> Unit
) {
    val viewModel: MainViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.onAction(MainAction.Initialize)
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MainEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    val rootFlow = when {
        !uiState.hasCheckedSession || uiState.isLoading -> RootFlow.Loading
        uiState.isLoggedIn -> RootFlow.Main
        else -> RootFlow.Auth
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        ProvideAppDimens {
            val dims = appDimens()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            ) {
                when (rootFlow) {
                    RootFlow.Loading -> LoadingScreen(
                        message = if (uiState.isLoading) "Checking session..." else "Preparing app..."
                    )
                    RootFlow.Auth -> AuthFlow(
                        snackbarHostState = snackbarHostState,
                        onRequestSessionRefresh = {
                            viewModel.onAction(MainAction.RefreshSession)
                        }
                    )
                    RootFlow.Main -> MainFlow(
                        snackbarHostState = snackbarHostState,
                        onPlayAudio = onPlayAudio,
                        onLogout = { viewModel.onAction(MainAction.RefreshSession) }
                    )
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(dims.bottomInsetPadding)
                )
            }
        }
    }
}
