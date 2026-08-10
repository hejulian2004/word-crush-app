package com.example.wordcrush.ui.compose.theme

import androidx.compose.ui.graphics.Color

val WordCrushInk = Color(0xFF20242A)
val WordCrushPaper = Color(0xFFF7F2E8)
val WordCrushSurface = Color(0xFFFFFDF8)
val WordCrushSurfaceVariant = Color(0xFFF0E8DA)
val WordCrushCoral = Color(0xFFC85346)
val WordCrushCoralContainer = Color(0xFFF8D8D1)
val WordCrushTeal = Color(0xFF2E7B73)
val WordCrushTealContainer = Color(0xFFD8EEE9)
val WordCrushAmber = Color(0xFFB7781C)
val WordCrushAmberContainer = Color(0xFFF5E2B9)
val WordCrushOutline = Color(0xFF8E8174)
val WordCrushOutlineVariant = Color(0xFFD8CBBE)
val WordCrushError = Color(0xFFBA1A1A)

val WordCrushNight = Color(0xFF181513)
val WordCrushNightSurface = Color(0xFF241F1B)
val WordCrushNightVariant = Color(0xFF352E28)
val WordCrushInkDark = Color(0xFFF5EFE7)
val WordCrushCoralDark = Color(0xFFFF8D7E)
val WordCrushCoralContainerDark = Color(0xFF71352E)
val WordCrushTealDark = Color(0xFF7FD1C6)
val WordCrushTealContainerDark = Color(0xFF1D4F49)
val WordCrushAmberDark = Color(0xFFF2C36A)
val WordCrushAmberContainerDark = Color(0xFF624A1E)
val WordCrushOutlineDark = Color(0xFFA8998A)
val WordCrushOutlineVariantDark = Color(0xFF51463D)
val WordCrushErrorDark = Color(0xFFFFB4AB)

// Compatibility aliases used by the game UI while the screen components migrate.
val WordCrushBlue = WordCrushInk
val WordCrushBlueDark = WordCrushInkDark
val WordCrushTealLegacy = WordCrushTeal
val WordCrushTealDarkLegacy = WordCrushTealDark
val WordCrushIvory = WordCrushPaper
val WordCrushLightOnSurfaceVariant = Color(0xFF5E5147)
val WordCrushLightPrimaryContainer = WordCrushCoralContainer
val WordCrushLightSecondaryContainer = WordCrushTealContainer
val WordCrushLightOutline = WordCrushOutline
val WordCrushLightOutlineVariant = WordCrushOutlineVariant
val WordCrushLightError = WordCrushError
val WordCrushDarkOnSurfaceVariant = Color(0xFFD7C9BD)
val WordCrushDarkPrimaryContainer = WordCrushCoralContainerDark
val WordCrushDarkSecondaryContainer = WordCrushTealContainerDark
val WordCrushDarkOutline = WordCrushOutlineDark
val WordCrushDarkOutlineVariant = WordCrushOutlineVariantDark
val WordCrushDarkError = WordCrushErrorDark

val WordCrushMatchEnglishSelected = WordCrushCoral
val WordCrushMatchChineseSelected = WordCrushTeal
val WordCrushMatchSelectedContent = Color.White
val WordCrushMatchSelectedBorder = Color.White.copy(alpha = 0.72f)

private const val MATCH_FLASH_BASE_CHANNEL = 0.24f
private const val MATCH_FLASH_CHANNEL_DELTA = 0.18f
private const val MATCH_FLASH_BASE_ALPHA = 0.22f
private const val MATCH_FLASH_ALPHA_DELTA = 0.42f

internal fun matchCardFlashColor(progress: Float): Color {
    val channel = MATCH_FLASH_BASE_CHANNEL + progress * MATCH_FLASH_CHANNEL_DELTA
    return Color(
        red = channel,
        green = channel * 0.70f,
        blue = channel * 0.62f,
        alpha = MATCH_FLASH_BASE_ALPHA + progress * MATCH_FLASH_ALPHA_DELTA
    )
}
