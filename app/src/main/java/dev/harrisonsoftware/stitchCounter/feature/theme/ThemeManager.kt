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
            AppTheme.GOLDEN_HEARTH -> goldenHearthLightColors()
            AppTheme.FOREST_FIBER -> forestFiberLightColors()
            AppTheme.CLOUD_SOFT -> cloudSoftLightColors()
            AppTheme.YARN_CANDY -> yarnCandyLightColors()
            AppTheme.DUSTY_ROSE -> dustyRoseLightColors()
        }
    }

    fun getDarkColorScheme(theme: AppTheme): ColorScheme {
        return when (theme) {
            AppTheme.SEA_COTTAGE -> seaCottageDarkColors()
            AppTheme.RETRO_SUMMER -> retroSummerDarkColors()
            AppTheme.GOLDEN_HEARTH -> goldenHearthDarkColors()
            AppTheme.FOREST_FIBER -> forestFiberDarkColors()
            AppTheme.CLOUD_SOFT -> cloudSoftDarkColors()
            AppTheme.YARN_CANDY -> yarnCandyDarkColors()
            AppTheme.DUSTY_ROSE -> dustyRoseDarkColors()
        }
    }
    
    fun getQuaternaryColor(theme: AppTheme, isDark: Boolean): Color {
        return when (theme) {
            AppTheme.SEA_COTTAGE -> if (isDark) SeaCottageQuaternaryDark else SeaCottageQuaternaryLight
            AppTheme.RETRO_SUMMER -> if (isDark) RetroSummerOrangeDark80 else RetroSummerOrangeDark40
            AppTheme.GOLDEN_HEARTH -> if (isDark) GoldenHearthQuaternaryDark else GoldenHearthQuaternaryLight
            AppTheme.FOREST_FIBER -> if (isDark) ForestFiberQuaternaryDark else ForestFiberQuaternaryLight
            AppTheme.CLOUD_SOFT -> if (isDark) CloudSoftQuaternaryDark else CloudSoftQuaternaryLight
            AppTheme.YARN_CANDY -> if (isDark) YarnCandyQuaternaryDark else YarnCandyQuaternaryLight
            AppTheme.DUSTY_ROSE -> if (isDark) DustyRoseQuaternaryDark else DustyRoseQuaternaryLight
        }
    }
    
    fun getOnQuaternaryColor(theme: AppTheme, isDark: Boolean): Color {
        return when (theme) {
            AppTheme.SEA_COTTAGE -> Color.White
            AppTheme.RETRO_SUMMER -> Color.White
            AppTheme.GOLDEN_HEARTH -> if (isDark) Color.Black else Color.White
            AppTheme.FOREST_FIBER -> Color.White
            AppTheme.CLOUD_SOFT -> if (isDark) Color.Black else Color.White
            AppTheme.YARN_CANDY -> if (isDark) Color.Black else Color.White
            AppTheme.DUSTY_ROSE -> if (isDark) Color.Black else Color.White
        }
    }
    
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
            AppTheme.GOLDEN_HEARTH -> listOf(
                ThemeColor("Terracotta", GoldenHearthPrimaryLight, GoldenHearthPrimaryDark),
                ThemeColor("Honey", GoldenHearthSecondaryLight, GoldenHearthSecondaryDark),
                ThemeColor("Sage", GoldenHearthTertiaryLight, GoldenHearthTertiaryDark),
                ThemeColor("Plum", GoldenHearthQuaternaryLight, GoldenHearthQuaternaryDark)
            )
            AppTheme.FOREST_FIBER -> listOf(
                ThemeColor("Moss", ForestFiberPrimaryLight, ForestFiberPrimaryDark),
                ThemeColor("Sage", ForestFiberSecondaryLight, ForestFiberSecondaryDark),
                ThemeColor("Wood", ForestFiberTertiaryLight, ForestFiberTertiaryDark),
                ThemeColor("Clay", ForestFiberQuaternaryLight, ForestFiberQuaternaryDark)
            )
            AppTheme.CLOUD_SOFT -> listOf(
                ThemeColor("Misty Blue", CloudSoftPrimaryLight, CloudSoftPrimaryDark),
                ThemeColor("Pale Sky", CloudSoftSecondaryLight, CloudSoftSecondaryDark),
                ThemeColor("Linen", CloudSoftTertiaryLight, CloudSoftTertiaryDark),
                ThemeColor("Mauve", CloudSoftQuaternaryLight, CloudSoftQuaternaryDark)
            )
            AppTheme.YARN_CANDY -> listOf(
                ThemeColor("Periwinkle", YarnCandyPrimaryLight, YarnCandyPrimaryDark),
                ThemeColor("Cotton Candy", YarnCandySecondaryLight, YarnCandySecondaryDark),
                ThemeColor("Lavender", YarnCandyTertiaryLight, YarnCandyTertiaryDark),
                ThemeColor("Peachy Pink", YarnCandyQuaternaryLight, YarnCandyQuaternaryDark)
            )
            AppTheme.DUSTY_ROSE -> listOf(
                ThemeColor("Rose", DustyRosePrimaryLight, DustyRosePrimaryDark),
                ThemeColor("Blush", DustyRoseSecondaryLight, DustyRoseSecondaryDark),
                ThemeColor("Sage", DustyRoseTertiaryLight, DustyRoseTertiaryDark),
                ThemeColor("Plum", DustyRoseQuaternaryLight, DustyRoseQuaternaryDark)
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