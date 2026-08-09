package com.example.wordcrush.ui.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = WordCrushBlue,
    secondary = WordCrushTeal,
    tertiary = WordCrushAmber,
    onPrimary = WordCrushSurface,
    onSecondary = WordCrushSurface,
    onTertiary = WordCrushSurface,
    background = WordCrushIvory,
    onBackground = WordCrushBlue,
    surface = WordCrushSurface,
    onSurface = WordCrushBlue,
    surfaceVariant = WordCrushSurfaceVariant,
    onSurfaceVariant = WordCrushLightOnSurfaceVariant,
    primaryContainer = WordCrushLightPrimaryContainer,
    onPrimaryContainer = WordCrushBlue,
    secondaryContainer = WordCrushLightSecondaryContainer,
    onSecondaryContainer = WordCrushBlue,
    outline = WordCrushLightOutline,
    outlineVariant = WordCrushLightOutlineVariant,
    error = WordCrushLightError,
    onError = WordCrushSurface
)

private val DarkColors = darkColorScheme(
    primary = WordCrushBlueDark,
    secondary = WordCrushTealDark,
    tertiary = WordCrushAmber,
    onPrimary = WordCrushNight,
    onSecondary = WordCrushNight,
    onTertiary = WordCrushNight,
    background = WordCrushNight,
    onBackground = WordCrushBlueDark,
    surface = WordCrushNightSurface,
    onSurface = WordCrushBlueDark,
    surfaceVariant = WordCrushNightVariant,
    onSurfaceVariant = WordCrushDarkOnSurfaceVariant,
    primaryContainer = WordCrushDarkPrimaryContainer,
    onPrimaryContainer = WordCrushBlueDark,
    secondaryContainer = WordCrushDarkSecondaryContainer,
    onSecondaryContainer = WordCrushBlueDark,
    outline = WordCrushDarkOutline,
    outlineVariant = WordCrushDarkOutlineVariant,
    error = WordCrushDarkError,
    onError = WordCrushNight
)

@Composable
fun WordCrushTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
