package dev.harrisonsoftware.stitchCounter.feature.theme

import androidx.compose.ui.graphics.Color
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme
import dev.harrisonsoftware.stitchCounter.ui.theme.DustyRosePrimaryDark
import dev.harrisonsoftware.stitchCounter.ui.theme.DustyRosePrimaryLight
import dev.harrisonsoftware.stitchCounter.ui.theme.GoldenHearthQuaternaryDark
import dev.harrisonsoftware.stitchCounter.ui.theme.GoldenHearthQuaternaryLight
import dev.harrisonsoftware.stitchCounter.ui.theme.SeaCottagePrimaryDark
import dev.harrisonsoftware.stitchCounter.ui.theme.SeaCottagePrimaryLight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeManagerTest {

    private val themeManager = ThemeManager()

    @Test
    fun `getLightColorScheme and getDarkColorScheme map expected primary colors`() {
        val seaCottageLight = themeManager.getLightColorScheme(AppTheme.SEA_COTTAGE)
        val seaCottageDark = themeManager.getDarkColorScheme(AppTheme.SEA_COTTAGE)
        val dustyRoseLight = themeManager.getLightColorScheme(AppTheme.DUSTY_ROSE)
        val dustyRoseDark = themeManager.getDarkColorScheme(AppTheme.DUSTY_ROSE)

        assertEquals(SeaCottagePrimaryLight, seaCottageLight.primary)
        assertEquals(SeaCottagePrimaryDark, seaCottageDark.primary)
        assertEquals(DustyRosePrimaryLight, dustyRoseLight.primary)
        assertEquals(DustyRosePrimaryDark, dustyRoseDark.primary)
    }

    @Test
    fun `getQuaternaryColor and getOnQuaternaryColor return theme specific colors`() {
        assertEquals(
            GoldenHearthQuaternaryLight,
            themeManager.getQuaternaryColor(AppTheme.GOLDEN_HEARTH, isDark = false)
        )
        assertEquals(
            GoldenHearthQuaternaryDark,
            themeManager.getQuaternaryColor(AppTheme.GOLDEN_HEARTH, isDark = true)
        )

        assertEquals(Color.White, themeManager.getOnQuaternaryColor(AppTheme.SEA_COTTAGE, isDark = false))
        assertEquals(Color.Black, themeManager.getOnQuaternaryColor(AppTheme.GOLDEN_HEARTH, isDark = true))
        assertEquals(Color.White, themeManager.getOnQuaternaryColor(AppTheme.GOLDEN_HEARTH, isDark = false))
        assertEquals(Color.Black, themeManager.getOnQuaternaryColor(AppTheme.YARN_CANDY, isDark = true))
    }

    @Test
    fun `getThemeColors returns four palette colors for each theme`() {
        AppTheme.entries.forEach { theme ->
            val themeColors = themeManager.getThemeColors(theme)
            assertEquals(4, themeColors.size)
            assertTrue(themeColors.all { it.name.isNotBlank() })
        }
    }
}
