package com.example.wordcrush.ui.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
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
    val showTopLevelNavigation = currentRoute in mainDestinations.map { it.route }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val navigationContent: @Composable RowScope.() -> Unit = {
        mainDestinations.forEach { destination ->
            val icon = destinationIcon(destination.route)
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { navigateTo(destination.route) },
                label = { Text(destination.label) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = destination.label
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }

    val navHost: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit = { paddingValues ->
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

    if (showTopLevelNavigation && appDimens().usesNavigationRail) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                header = {
                    Icon(
                        imageVector = Icons.Filled.SportsEsports,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                }
            ) {
                mainDestinations.forEach { destination ->
                    NavigationRailItem(
                        selected = currentRoute == destination.route,
                        onClick = { navigateTo(destination.route) },
                        label = { Text(destination.label) },
                        icon = {
                            Icon(
                                imageVector = destinationIcon(destination.route),
                                contentDescription = destination.label
                            )
                        },
                        colors = androidx.compose.material3.NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                navHost(androidx.compose.foundation.layout.PaddingValues())
            }
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showTopLevelNavigation) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 0.dp,
                        content = navigationContent
                    )
                }
            }
        ) { paddingValues ->
            navHost(paddingValues)
        }
    }
}

private fun destinationIcon(route: String) = when (route) {
    MainRoute.Breakthrough -> Icons.Filled.SportsEsports
    MainRoute.WordBook -> Icons.AutoMirrored.Filled.MenuBook
    MainRoute.Home -> Icons.Filled.Person
    else -> Icons.Filled.Leaderboard
}
