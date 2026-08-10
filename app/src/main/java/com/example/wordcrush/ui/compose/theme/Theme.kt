package com.example.wordcrush.ui.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = WordCrushCoral,
    secondary = WordCrushTeal,
    tertiary = WordCrushAmber,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    background = WordCrushPaper,
    onBackground = WordCrushInk,
    surface = WordCrushSurface,
    onSurface = WordCrushInk,
    surfaceVariant = WordCrushSurfaceVariant,
    onSurfaceVariant = WordCrushLightOnSurfaceVariant,
    primaryContainer = WordCrushCoralContainer,
    onPrimaryContainer = WordCrushInk,
    secondaryContainer = WordCrushTealContainer,
    onSecondaryContainer = WordCrushInk,
    tertiaryContainer = WordCrushAmberContainer,
    onTertiaryContainer = WordCrushInk,
    outline = WordCrushLightOutline,
    outlineVariant = WordCrushLightOutlineVariant,
    error = WordCrushLightError,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = WordCrushCoralDark,
    secondary = WordCrushTealDark,
    tertiary = WordCrushAmberDark,
    onPrimary = WordCrushNight,
    onSecondary = WordCrushNight,
    onTertiary = WordCrushNight,
    background = WordCrushNight,
    onBackground = WordCrushInkDark,
    surface = WordCrushNightSurface,
    onSurface = WordCrushInkDark,
    surfaceVariant = WordCrushNightVariant,
    onSurfaceVariant = WordCrushDarkOnSurfaceVariant,
    primaryContainer = WordCrushCoralContainerDark,
    onPrimaryContainer = WordCrushInkDark,
    secondaryContainer = WordCrushTealContainerDark,
    onSecondaryContainer = WordCrushInkDark,
    tertiaryContainer = WordCrushAmberContainerDark,
    onTertiaryContainer = WordCrushInkDark,
    outline = WordCrushDarkOutline,
    outlineVariant = WordCrushDarkOutlineVariant,
    error = WordCrushDarkError,
    onError = WordCrushNight
)

private val WordCrushTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

@Composable
fun WordCrushTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = WordCrushTypography,
        content = content
    )
}
