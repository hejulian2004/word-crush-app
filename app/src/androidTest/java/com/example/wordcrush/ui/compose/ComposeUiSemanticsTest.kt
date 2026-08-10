package com.example.wordcrush.ui.compose

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.wordcrush.ui.compose.theme.WordCrushTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeUiSemanticsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun errorStateExposesRetryAction() {
        composeRule.setContent {
            WordCrushTheme {
                EmptyStateCard(
                    title = "Unable to load words",
                    message = "The word book is unavailable.",
                    actionLabel = "Retry",
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithText("Retry").assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun progressSummaryExposesReadableCount() {
        composeRule.setContent {
            WordCrushTheme {
                ProgressSummary(
                    label = "Today's fixed set",
                    completed = 3,
                    total = 5
                )
            }
        }

        composeRule.onNodeWithText("3/5").assertIsDisplayed()
    }
}
