package com.example.wordcrush.ui.compose

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveUiTest {
    @Test
    fun compactWindowUsesBottomNavigationAndPhoneSpacing() {
        val dims = AppDimens(screenWidth = 360.dp, screenHeight = 800.dp)

        assertTrue(dims.isCompact)
        assertFalse(dims.usesNavigationRail)
        assertEquals(140.dp, dims.gridMinCell)
        assertEquals(16.dp, dims.pagePadding)
    }

    @Test
    fun expandedWindowUsesRailAndConstrainedContent() {
        val dims = AppDimens(screenWidth = 1024.dp, screenHeight = 768.dp)

        assertTrue(dims.isExpanded)
        assertTrue(dims.usesNavigationRail)
        assertEquals(1120.dp, dims.contentMaxWidth)
        assertEquals(190.dp, dims.gridMinCell)
    }
}
