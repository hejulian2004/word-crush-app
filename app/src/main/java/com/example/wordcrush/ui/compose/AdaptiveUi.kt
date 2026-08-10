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

/**
 * Window-based layout policy. It deliberately does not scale typography or controls with the
 * screen: a tablet gets a different composition, not a blown-up phone.
 */
@Immutable
internal data class AppDimens(
    val screenWidth: Dp,
    val screenHeight: Dp,
    val scale: Float = 1f
) {
    val isCompact = screenWidth < 600.dp
    val isMedium = screenWidth >= 600.dp && screenWidth < 840.dp
    val isExpanded = screenWidth >= 840.dp
    val usesNavigationRail = !isCompact
    val contentMaxWidth = if (isExpanded) 1120.dp else 760.dp

    val pagePadding = 16.dp
    val pageTopPadding = 16.dp
    val sectionSpacing = 16.dp
    val compactSpacing = 8.dp
    val tinySpacing = 4.dp
    val chipSpacing = 8.dp
    val controlSpacing = 12.dp
    val cardPadding = 16.dp
    val cardPaddingLarge = 20.dp
    val buttonHeight = 52.dp
    val inputHeight = 56.dp
    val avatarSize = 56.dp
    val smallAvatarSize = 42.dp
    val iconSize = 20.dp
    val cardCorner = 16.dp
    val cardCornerLarge = 20.dp
    val cardCornerAuth = 24.dp
    val gridMinCell = if (isExpanded) 190.dp else if (isMedium) 170.dp else 140.dp
    val staggeredGridMinCell = gridMinCell
    val matchGridHeight = when {
        screenHeight < 500.dp -> 300.dp
        screenHeight < 700.dp -> 420.dp
        else -> 560.dp
    }
    val matchCardMinHeight = if (screenHeight < 500.dp) 100.dp else 116.dp
    val heartSize = 20.dp
    val bottomInsetPadding = 16.dp

    // Source-compatible helper for remaining legacy composables. New layout code uses fixed tokens.
    fun scaled(base: Dp): Dp = base
}

private val LocalAppDimens = staticCompositionLocalOf {
    AppDimens(
        screenWidth = 360.dp,
        screenHeight = 800.dp
    )
}

@Composable
internal fun ProvideAppDimens(
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalAppDimens provides AppDimens(
                screenWidth = maxWidth,
                screenHeight = maxHeight
            )
        ) {
            content()
        }
    }
}

@Composable
internal fun appDimens(): AppDimens = LocalAppDimens.current
