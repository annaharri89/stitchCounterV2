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
            AppTheme.PURPLE -> purpleLightColors()
        }
    }

    fun getDarkColorScheme(theme: AppTheme): ColorScheme {
        return when (theme) {
            AppTheme.SEA_COTTAGE -> seaCottageDarkColors()
            AppTheme.RETRO_SUMMER -> retroSummerDarkColors()
            AppTheme.PURPLE -> purpleDarkColors()
        }
    }
    
    fun getQuaternaryColor(theme: AppTheme, isDark: Boolean): Color {
        return when (theme) {
            AppTheme.SEA_COTTAGE -> if (isDark) SeaCottageQuaternaryDark else SeaCottageQuaternaryLight
            AppTheme.RETRO_SUMMER -> if (isDark) RetroSummerOrangeDark80 else RetroSummerOrangeDark40
            AppTheme.PURPLE -> if (isDark) PurpleViolet80 else PurpleViolet40
        }
    }
    
    fun getOnQuaternaryColor(theme: AppTheme, isDark: Boolean): Color {
        return when (theme) {
            AppTheme.SEA_COTTAGE -> Color.White // Dark blue needs white text
            AppTheme.RETRO_SUMMER -> Color.White // Orange-red needs white text
            AppTheme.PURPLE -> if (isDark) Color.Black else Color.White // Light purple-pink in dark theme needs black, dark in light theme needs white
        }
    }
    
    /**
     * Returns color information for UI display (Settings screen color swatches)
     */
    fun getThemeColors(theme: AppTheme): List<ThemeColor> {
        return when (theme) {
            AppTheme.SEA_COTTAGE -> listOf(
                ThemeColor("Mint", SeaCottageSecondaryLight, SeaCottageSecondaryDark),
                ThemeColor("Surf", SeaCottagePrimaryLight, SeaCottagePrimaryDark),
                ThemeColor("Whale Light", SeaCottageTertiaryLight, SeaCottageTertiaryDark),
                ThemeColor("Whale Dark", SeaCottageQuaternaryLight, SeaCottageQuaternaryDark)
            )
            AppTheme.RETRO_SUMMER -> listOf(
                ThemeColor("Cactus", RetroSummerCactus40, RetroSummerCactus80),
                ThemeColor("Sun", RetroSummerSun40, RetroSummerSun80),
                ThemeColor("Orange Light", RetroSummerOrangeLight40, RetroSummerOrangeLight80),
                ThemeColor("Orange Dark", RetroSummerOrangeDark40, RetroSummerOrangeDark80)
            )
            AppTheme.PURPLE -> listOf(
                ThemeColor("Purple", Purple40, Purple80),
                ThemeColor("Purple Grey", PurpleGrey40, PurpleGrey80),
                ThemeColor("Pink", Pink40, Pink80),
                ThemeColor("Violet", PurpleViolet40, PurpleViolet80)
            )
        }
    }
}

/**
 * Data class representing a color in a theme with both light and dark variants
 */
data class ThemeColor(
    val name: String,
    val lightColor: Color,
    val darkColor: Color
)