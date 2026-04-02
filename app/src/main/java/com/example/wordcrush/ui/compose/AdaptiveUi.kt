package com.example.wordcrush.ui.compose

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
internal data class AppDimens(
    val screenWidth: Dp,
    val screenHeight: Dp,
    val scale: Float
) {
    val pagePadding = scaled(16.dp)
    val pageTopPadding = scaled(16.dp)
    val sectionSpacing = scaled(16.dp)
    val compactSpacing = scaled(8.dp)
    val tinySpacing = scaled(4.dp)
    val chipSpacing = scaled(8.dp)
    val controlSpacing = scaled(12.dp)
    val cardPadding = scaled(18.dp)
    val cardPaddingLarge = scaled(20.dp)
    val buttonHeight = scaled(52.dp)
    val inputHeight = scaled(56.dp)
    val avatarSize = scaled(56.dp)
    val smallAvatarSize = scaled(42.dp)
    val iconSize = scaled(20.dp)
    val cardCorner = scaled(20.dp)
    val cardCornerLarge = scaled(22.dp)
    val cardCornerAuth = scaled(28.dp)
    val staggeredGridMinCell = if (screenWidth >= 600.dp) scaled(170.dp) else scaled(140.dp)
    val matchGridHeight = when {
        screenHeight < 700.dp -> scaled(440.dp)
        screenHeight < 860.dp -> scaled(520.dp)
        else -> scaled(620.dp)
    }
    val matchCardMinHeight = scaled(120.dp)
    val heartSize = scaled(20.dp)
    val bottomInsetPadding = scaled(16.dp)

    fun scaled(base: Dp): Dp = (base.value * scale).dp
}

private val LocalAppDimens = staticCompositionLocalOf {
    AppDimens(
        screenWidth = 360.dp,
        screenHeight = 800.dp,
        scale = 1f
    )
}

@Composable
internal fun ProvideAppDimens(
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthScale = when {
            maxWidth < 360.dp -> 0.90f
            maxWidth < 400.dp -> 0.96f
            maxWidth < 520.dp -> 1f
            maxWidth < 720.dp -> 1.08f
            else -> 1.14f
        }
        val heightScale = when {
            maxHeight < 700.dp -> 0.94f
            maxHeight > 920.dp -> 1.05f
            else -> 1f
        }
        val dims = AppDimens(
            screenWidth = maxWidth,
            screenHeight = maxHeight,
            scale = (widthScale * heightScale).coerceIn(0.88f, 1.14f)
        )
        CompositionLocalProvider(LocalAppDimens provides dims) {
            content()
        }
    }
}

@Composable
internal fun appDimens(): AppDimens = LocalAppDimens.current
