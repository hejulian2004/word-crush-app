package com.example.wordcrush.ui.compose.theme

import androidx.compose.ui.graphics.Color

val WordCrushBlue = Color(0xFF181818)
val WordCrushBlueDark = Color(0xFFF2F2F2)
val WordCrushTeal = Color(0xFF2A2A2A)
val WordCrushTealDark = Color(0xFFD8D8D8)
val WordCrushAmber = Color(0xFF6E6E6E)
val WordCrushIvory = Color(0xFFF4F4F2)
val WordCrushSurface = Color(0xFFFCFCFA)
val WordCrushSurfaceVariant = Color(0xFFE7E7E3)
val WordCrushNight = Color(0xFF0F0F10)
val WordCrushNightSurface = Color(0xFF171718)
val WordCrushNightVariant = Color(0xFF2B2B2E)

val WordCrushLightOnSurfaceVariant = Color(0xFF4B4B4B)
val WordCrushLightPrimaryContainer = Color(0xFFDADAD7)
val WordCrushLightSecondaryContainer = Color(0xFFE4E4E1)
val WordCrushLightOutline = Color(0xFF9A9A95)
val WordCrushLightOutlineVariant = Color(0xFFC9C9C3)
val WordCrushLightError = Color(0xFF505050)

val WordCrushDarkOnSurfaceVariant = Color(0xFFC1C1C6)
val WordCrushDarkPrimaryContainer = Color(0xFF303033)
val WordCrushDarkSecondaryContainer = Color(0xFF262629)
val WordCrushDarkOutline = Color(0xFF7C7C82)
val WordCrushDarkOutlineVariant = Color(0xFF444449)
val WordCrushDarkError = Color(0xFFB4B4B4)

val WordCrushMatchEnglishSelected = Color(0xFF151515)
val WordCrushMatchChineseSelected = Color(0xFF3A3A3A)
val WordCrushMatchSelectedContent = Color(0xFFF7F7F5)
val WordCrushMatchSelectedBorder = Color(0xFFE8E8E3)

private const val MATCH_FLASH_BASE_CHANNEL = 0.20f
private const val MATCH_FLASH_CHANNEL_DELTA = 0.18f
private const val MATCH_FLASH_BASE_ALPHA = 0.26f
private const val MATCH_FLASH_ALPHA_DELTA = 0.44f

internal fun matchCardFlashColor(progress: Float): Color {
    val channel = MATCH_FLASH_BASE_CHANNEL + progress * MATCH_FLASH_CHANNEL_DELTA
    return Color(
        red = channel,
        green = channel,
        blue = channel,
        alpha = MATCH_FLASH_BASE_ALPHA + progress * MATCH_FLASH_ALPHA_DELTA
    )
}
