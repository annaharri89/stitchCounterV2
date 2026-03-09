package dev.harrisonsoftware.stitchCounter.feature.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.harrisonsoftware.stitchCounter.MainActivity
import dev.harrisonsoftware.stitchCounter.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RootNavigationStateRestorationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun settings_tab_selection_matches_visible_screen_after_activity_recreation() {
        val settingsTabLabel = composeRule.activity.getString(R.string.nav_settings)
        val settingsScreenTitle = composeRule.activity.getString(R.string.settings_theme)

        composeRule.onNodeWithText(settingsTabLabel).performClick()
        composeRule.onNodeWithText(settingsScreenTitle).assertIsDisplayed()
        assertTabSelected(settingsTabLabel)

        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(settingsScreenTitle).assertIsDisplayed()
        assertTabSelected(settingsTabLabel)
    }

    private fun assertTabSelected(tabLabel: String) {
        composeRule.onNodeWithText(tabLabel, useUnmergedTree = true).assertIsSelected()
    }
}
