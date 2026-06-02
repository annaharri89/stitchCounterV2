package dev.harrisonsoftware.stitchCounter.feature.settings

import android.content.Context
import android.content.Intent
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsScreenTest {

    @Test
    fun `launchExternalActivitySafely returns true when start succeeds`() {
        val context = mockk<Context>()
        val intent = mockk<Intent>()
        every { context.startActivity(intent) } just runs

        val wasLaunched = launchExternalActivitySafely(context, intent)

        assertTrue(wasLaunched)
        verify(exactly = 1) { context.startActivity(intent) }
    }

    @Test
    fun `launchExternalActivitySafely returns false when start throws`() {
        val context = mockk<Context>()
        val intent = mockk<Intent>()
        every { context.startActivity(intent) } throws RuntimeException("No activity")

        val wasLaunched = launchExternalActivitySafely(context, intent)

        assertFalse(wasLaunched)
        verify(exactly = 1) { context.startActivity(intent) }
    }

    @Test
    fun `updatedExpandedSettingsSectionNames adds section without removing already expanded sections`() {
        val expandedSections = updatedExpandedSettingsSectionNames(
            expandedSectionNames = listOf("THEME"),
            sectionName = "SUPPORT",
            isExpanded = true
        )

        assertEquals(listOf("THEME", "SUPPORT"), expandedSections)
    }

    @Test
    fun `updatedExpandedSettingsSectionNames does not duplicate an expanded section`() {
        val expandedSections = updatedExpandedSettingsSectionNames(
            expandedSectionNames = listOf("THEME", "SUPPORT"),
            sectionName = "SUPPORT",
            isExpanded = true
        )

        assertEquals(listOf("THEME", "SUPPORT"), expandedSections)
    }

    @Test
    fun `updatedExpandedSettingsSectionNames removes only the collapsed section`() {
        val expandedSections = updatedExpandedSettingsSectionNames(
            expandedSectionNames = listOf("THEME", "SUPPORT", "LEGAL"),
            sectionName = "SUPPORT",
            isExpanded = false
        )

        assertEquals(listOf("THEME", "LEGAL"), expandedSections)
    }
}
