package dev.harrisonsoftware.stitchCounter.feature.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme
import dev.harrisonsoftware.stitchCounter.ui.theme.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central manager for theme-related operations.
 * Maps AppTheme enum to actual ColorSchemes and provides color information for UI display.
 */
@Singleton
class ThemeManager @Inject constructor() {

    fun getLightColorScheme(theme: AppTheme): ColorScheme {
        return when (theme) {
            AppTheme.SEA_COTTAGE -> seaCottageLightColors()
            AppTheme.RETRO_SUMMER -> retroSummerLightColors()
            AppTheme.DUSTY_ROSE -> dustyRoseLightColors()
        }
    }

    fun getDarkColorScheme(theme: AppTheme): ColorScheme {
        return when (theme) {
            AppTheme.SEA_COTTAGE -> seaCottageDarkColors()
            AppTheme.RETRO_SUMMER -> retroSummerDarkColors()
            AppTheme.DUSTY_ROSE -> dustyRoseDarkColors()
        }
    }
    
    fun getQuaternaryColor(theme: AppTheme, isDark: Boolean): Color {
        return when (theme) {
            AppTheme.SEA_COTTAGE -> if (isDark) SeaCottageWhaleDark80 else SeaCottageWhaleDark40
            AppTheme.RETRO_SUMMER -> if (isDark) RetroSummerOrangeDark80 else RetroSummerOrangeDark40
            AppTheme.DUSTY_ROSE -> if (isDark) DustyRoseViolet80 else DustyRoseViolet40
        }
    }
    
    fun getOnQuaternaryColor(theme: AppTheme, isDark: Boolean): Color {
        return when (theme) {
            AppTheme.SEA_COTTAGE -> Color.White // Dark blue needs white text
            AppTheme.RETRO_SUMMER -> Color.White // Orange-red needs white text
            AppTheme.DUSTY_ROSE -> if (isDark) Color.Black else Color.White
        }
    }
    
    /**
     * Returns color information for UI display (Settings screen color swatches)
     */
    fun getThemeColors(theme: AppTheme): List<ThemeColor> {
        return when (theme) {
            AppTheme.SEA_COTTAGE -> listOf(
                ThemeColor("Mint", SeaCottageMint40, SeaCottageMint80),
                ThemeColor("Surf", SeaCottageSurf40, SeaCottageSurf80),
                ThemeColor("Whale Light", SeaCottageWhaleLight40, SeaCottageWhaleLight80),
                ThemeColor("Whale Dark", SeaCottageWhaleDark40, SeaCottageWhaleDark80)
            )
            AppTheme.RETRO_SUMMER -> listOf(
                ThemeColor("Cactus", RetroSummerCactus40, RetroSummerCactus80),
                ThemeColor("Sun", RetroSummerSun40, RetroSummerSun80),
                ThemeColor("Orange Light", RetroSummerOrangeLight40, RetroSummerOrangeLight80),
                ThemeColor("Orange Dark", RetroSummerOrangeDark40, RetroSummerOrangeDark80)
            )
            AppTheme.DUSTY_ROSE -> listOf(
                ThemeColor("Dusty Rose", DustyRose40, DustyRose80),
                ThemeColor("Dusty Rose Grey", DustyRoseGrey40, DustyRoseGrey80),
                ThemeColor("Pink", Pink40, Pink80),
                ThemeColor("Violet", DustyRoseViolet40, DustyRoseViolet80)
            )
        }
    }
}

/**
 * Data class representing a color in a theme with both light and dark variants
 */
data class ThemeColor(
    val name: String,
    val lightColor: androidx.compose.ui.graphics.Color,
    val darkColor: androidx.compose.ui.graphics.Color
)