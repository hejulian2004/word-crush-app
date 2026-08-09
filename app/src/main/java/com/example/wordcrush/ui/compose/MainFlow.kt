package com.example.wordcrush.ui.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import kotlinx.coroutines.launch

@Composable
internal fun MainFlow(
    snackbarHostState: SnackbarHostState,
    onPlayAudio: suspend (String, Int) -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = backStackEntry?.destination?.route
    val scope = rememberCoroutineScope()

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute in mainDestinations.map { it.route }) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp
                ) {
                    mainDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = { navigateTo(destination.route) },
                            label = { Text(destination.label) },
                            icon = { Text(destination.label.take(1)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = MainRoute.Breakthrough,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(MainRoute.Breakthrough) {
                MatchRoute(
                    onPlayAudio = onPlayAudio,
                    onShowMessage = { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    },
                    onOpenRanking = { gameType ->
                        navController.navigate(MainRoute.ranking(gameType))
                    }
                )
            }
            composable(MainRoute.WordBook) {
                WordBookRoute(
                    onPlayAudio = onPlayAudio,
                    onShowMessage = { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    }
                )
            }
            composable(MainRoute.Home) {
                HomeRoute(
                    onOpenRecords = { navController.navigate(MainRoute.Records) },
                    onLogout = onLogout,
                    onShowMessage = { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    }
                )
            }
            composable(
                route = MainRoute.RankingPattern,
                arguments = listOf(navArgument(GAME_TYPE_ARGUMENT) { type = NavType.IntType })
            ) { entry ->
                RankingRoute(
                    gameType = entry.arguments?.getInt(GAME_TYPE_ARGUMENT) ?: 0,
                    onBack = { navController.popBackStack() },
                    onShowMessage = { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    }
                )
            }
            composable(MainRoute.Records) {
                GameRecordRoute(
                    onBack = { navController.popBackStack() },
                    onShowMessage = { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    }
                )
            }
        }
    }
}
