package dev.harrisonsoftware.stitchCounter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme
import dev.harrisonsoftware.stitchCounter.feature.theme.ThemeManager

val LocalQuaternaryColor = compositionLocalOf<Color> { 
    error("No quaternary color provided") 
}

val LocalOnQuaternaryColor = compositionLocalOf<Color> { 
    error("No onQuaternary color provided") 
}

val LocalAppThemeStyle = staticCompositionLocalOf<AppThemeStyle> {
    CottageStyle
}

fun seaCottageLightColors() = lightColorScheme(
    primary = SeaCottagePrimaryLight,
    secondary = SeaCottageSecondaryLight,
    tertiary = SeaCottageTertiaryLight,
    primaryContainer = SeaCottagePrimaryContainerLight,
    secondaryContainer = SeaCottageSecondaryContainerLight,
    tertiaryContainer = SeaCottageTertiaryContainerLight,
    onTertiaryContainer = SeaCottageOnTertiaryContainerLight,
    error = SeaCottageError40,
    onError = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White
)


fun seaCottageDarkColors() = darkColorScheme(
    primary = SeaCottagePrimaryDark,
    secondary = SeaCottageSecondaryDark,
    tertiary = SeaCottageTertiaryDark,
    primaryContainer = SeaCottagePrimaryContainerDark,
    secondaryContainer = SeaCottageSecondaryContainerDark,
    tertiaryContainer = SeaCottageTertiaryContainerDark,
    onTertiaryContainer = SeaCottageOnTertiaryContainerDark,
    error = SeaCottageError80,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onError = Color.White
)

fun retroSummerLightColors() = lightColorScheme(
    primary =  RetroSummerCactus40 ,
    secondary = RetroSummerSun40,
    tertiary = RetroSummerOrangeLight40,
    onTertiary = Color.White,
    primaryContainer = RetroSummerPrimaryContainer40,
    secondaryContainer = RetroSummerSecondaryContainer40,
    tertiaryContainer = RetroSummerTertiaryContainer40,
    onTertiaryContainer = RetroSummerOnTertiaryContainer40,
    error = RetroSummerError40,
    onError = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White
)

fun retroSummerDarkColors() = darkColorScheme(
    primary = RetroSummerCactus80,
    secondary = RetroSummerSun80,
    tertiary = RetroSummerOrangeLight80,
    primaryContainer = RetroSummerPrimaryContainer80,
    secondaryContainer = RetroSummerSecondaryContainer80,
    tertiaryContainer = RetroSummerTertiaryContainer80,
    onTertiaryContainer = RetroSummerOnTertiaryContainer80,
    error = RetroSummerError80,
    onError = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White
)

fun dustyRoseLightColors() = lightColorScheme(
    primary =  DustyRose40 ,
    secondary = DustyRoseGold40,
    tertiary = Pink40,
    primaryContainer = DustyRosePrimaryContainer40,
    secondaryContainer = DustyRoseSecondaryContainer40,
    tertiaryContainer = DustyRoseTertiaryContainer40,
    onTertiaryContainer = DustyRoseOnTertiaryContainer40,
    error = DustyRoseError40,
    onError = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White
)

fun dustyRoseDarkColors() = darkColorScheme(
    primary = DustyRose80,
    secondary = DustyRoseGold80,
    tertiary = Pink80,
    primaryContainer = DustyRosePrimaryContainer80,
    secondaryContainer = DustyRoseSecondaryContainer80,
    tertiaryContainer = DustyRoseTertiaryContainer80,
    onTertiaryContainer = DustyRoseOnTertiaryContainer80,
    error = DustyRoseError80,
    onError = Color.White,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black
)

/**
 * Main theme composable that applies the selected color scheme app-wide.
 * 
 * Flow: ThemeViewModel observes DataStore → emits selected theme → 
 * ThemeManager maps theme to ColorScheme + AppThemeStyle → MaterialTheme applies everything
 */
@Composable
fun StitchCounterV3Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    theme: AppTheme = AppTheme.SEA_COTTAGE,
    content: @Composable () -> Unit
) {
    val themeManager = ThemeManager()
    val colorScheme = when {
        darkTheme -> themeManager.getDarkColorScheme(theme)
        else -> themeManager.getLightColorScheme(theme)
    }
    
    val quaternaryColor = when {
        darkTheme -> themeManager.getQuaternaryColor(theme, isDark = true)
        else -> themeManager.getQuaternaryColor(theme, isDark = false)
    }
    
    val onQuaternaryColor = when {
        darkTheme -> themeManager.getOnQuaternaryColor(theme, isDark = true)
        else -> themeManager.getOnQuaternaryColor(theme, isDark = false)
    }

    val themeStyle = themeManager.getStyle(theme)
    val typography = themeStyle.toTypography()

    CompositionLocalProvider(
        LocalQuaternaryColor provides quaternaryColor,
        LocalOnQuaternaryColor provides onQuaternaryColor,
        LocalAppThemeStyle provides themeStyle
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

val MaterialTheme.quaternary: Color
    @Composable get() = LocalQuaternaryColor.current

val MaterialTheme.onQuaternary: Color
    @Composable get() = LocalOnQuaternaryColor.current

val MaterialTheme.appStyle: AppThemeStyle
    @Composable get() = LocalAppThemeStyle.current
