package com.example.wordcrush.ui.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    onSurfaceVariant = Color(0xFF4B4B4B),
    primaryContainer = Color(0xFFDADAD7),
    onPrimaryContainer = WordCrushBlue,
    secondaryContainer = Color(0xFFE4E4E1),
    onSecondaryContainer = WordCrushBlue,
    outline = Color(0xFF9A9A95),
    outlineVariant = Color(0xFFC9C9C3),
    error = Color(0xFF505050),
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
    onSurfaceVariant = Color(0xFFC1C1C6),
    primaryContainer = Color(0xFF303033),
    onPrimaryContainer = WordCrushBlueDark,
    secondaryContainer = Color(0xFF262629),
    onSecondaryContainer = WordCrushBlueDark,
    outline = Color(0xFF7C7C82),
    outlineVariant = Color(0xFF444449),
    error = Color(0xFFB4B4B4),
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
