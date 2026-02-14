package dev.harrisonsoftware.stitchCounter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme
import dev.harrisonsoftware.stitchCounter.feature.theme.ThemeManager

val LocalQuaternaryColor = compositionLocalOf<Color> { 
    error("No quaternary color provided") 
}

val LocalOnQuaternaryColor = compositionLocalOf<Color> { 
    error("No onQuaternary color provided") 
}

fun seaCottageLightColors() = lightColorScheme(
    primary = SeaCottagePrimaryLight,
    secondary = SeaCottageSecondaryLight,
    tertiary = SeaCottageTertiaryLight,
    primaryContainer = SeaCottagePrimaryContainerLight,
    secondaryContainer = SeaCottageSecondaryContainerLight,
    tertiaryContainer = SeaCottageTertiaryContainerLight,
    error = SeaCottageErrorLight,
    onError = SeaCottageOnErrorLight,
    errorContainer = SeaCottageErrorContainerLight,
    onErrorContainer = SeaCottageOnErrorContainerLight,
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
    onPrimaryContainer = SeaCottageOnPrimaryContainerDark,
    onSecondaryContainer = SeaCottageOnSecondaryContainerDark,
    onTertiaryContainer = SeaCottageOnTertiaryContainerDark,
    error = SeaCottageErrorDark,
    onError = SeaCottageOnErrorDark,
    errorContainer = SeaCottageErrorContainerDark,
    onErrorContainer = SeaCottageOnErrorContainerDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White
)

fun retroSummerLightColors() = lightColorScheme(
    primary = RetroSummerCactus40,
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

fun goldenHearthLightColors() = lightColorScheme(
    primary = GoldenHearthPrimaryLight,
    secondary = GoldenHearthSecondaryLight,
    tertiary = GoldenHearthTertiaryLight,
    primaryContainer = GoldenHearthPrimaryContainerLight,
    secondaryContainer = GoldenHearthSecondaryContainerLight,
    tertiaryContainer = GoldenHearthTertiaryContainerLight,
    error = GoldenHearthErrorLight,
    onError = GoldenHearthOnErrorLight,
    errorContainer = GoldenHearthErrorContainerLight,
    onErrorContainer = GoldenHearthOnErrorContainerLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White
)

fun goldenHearthDarkColors() = darkColorScheme(
    primary = GoldenHearthPrimaryDark,
    secondary = GoldenHearthSecondaryDark,
    tertiary = GoldenHearthTertiaryDark,
    primaryContainer = GoldenHearthPrimaryContainerDark,
    secondaryContainer = GoldenHearthSecondaryContainerDark,
    tertiaryContainer = GoldenHearthTertiaryContainerDark,
    error = GoldenHearthErrorDark,
    onError = GoldenHearthOnErrorDark,
    errorContainer = GoldenHearthErrorContainerDark,
    onErrorContainer = GoldenHearthOnErrorContainerDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White
)

fun forestFiberLightColors() = lightColorScheme(
    primary = ForestFiberPrimaryLight,
    secondary = ForestFiberSecondaryLight,
    tertiary = ForestFiberTertiaryLight,
    primaryContainer = ForestFiberPrimaryContainerLight,
    secondaryContainer = ForestFiberSecondaryContainerLight,
    tertiaryContainer = ForestFiberTertiaryContainerLight,
    error = ForestFiberErrorLight,
    onError = ForestFiberOnErrorLight,
    errorContainer = ForestFiberErrorContainerLight,
    onErrorContainer = ForestFiberOnErrorContainerLight,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White
)

fun forestFiberDarkColors() = darkColorScheme(
    primary = ForestFiberPrimaryDark,
    secondary = ForestFiberSecondaryDark,
    tertiary = ForestFiberTertiaryDark,
    primaryContainer = ForestFiberPrimaryContainerDark,
    secondaryContainer = ForestFiberSecondaryContainerDark,
    tertiaryContainer = ForestFiberTertiaryContainerDark,
    error = ForestFiberErrorDark,
    onError = ForestFiberOnErrorDark,
    errorContainer = ForestFiberErrorContainerDark,
    onErrorContainer = ForestFiberOnErrorContainerDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White
)

fun cloudSoftLightColors() = lightColorScheme(
    primary = CloudSoftPrimaryLight,
    secondary = CloudSoftSecondaryLight,
    tertiary = CloudSoftTertiaryLight,
    primaryContainer = CloudSoftPrimaryContainerLight,
    secondaryContainer = CloudSoftSecondaryContainerLight,
    tertiaryContainer = CloudSoftTertiaryContainerLight,
    error = CloudSoftErrorLight,
    onError = CloudSoftOnErrorLight,
    errorContainer = CloudSoftErrorContainerLight,
    onErrorContainer = CloudSoftOnErrorContainerLight,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black
)

fun cloudSoftDarkColors() = darkColorScheme(
    primary = CloudSoftPrimaryDark,
    secondary = CloudSoftSecondaryDark,
    tertiary = CloudSoftTertiaryDark,
    primaryContainer = CloudSoftPrimaryContainerDark,
    secondaryContainer = CloudSoftSecondaryContainerDark,
    tertiaryContainer = CloudSoftTertiaryContainerDark,
    error = CloudSoftErrorDark,
    onError = CloudSoftOnErrorDark,
    errorContainer = CloudSoftErrorContainerDark,
    onErrorContainer = CloudSoftOnErrorContainerDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White
)

fun yarnCandyLightColors() = lightColorScheme(
    primary = YarnCandyPrimaryLight,
    secondary = YarnCandySecondaryLight,
    tertiary = YarnCandyTertiaryLight,
    primaryContainer = YarnCandyPrimaryContainerLight,
    secondaryContainer = YarnCandySecondaryContainerLight,
    tertiaryContainer = YarnCandyTertiaryContainerLight,
    error = YarnCandyErrorLight,
    onError = YarnCandyOnErrorLight,
    errorContainer = YarnCandyErrorContainerLight,
    onErrorContainer = YarnCandyOnErrorContainerLight,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White
)

fun yarnCandyDarkColors() = darkColorScheme(
    primary = YarnCandyPrimaryDark,
    secondary = YarnCandySecondaryDark,
    tertiary = YarnCandyTertiaryDark,
    primaryContainer = YarnCandyPrimaryContainerDark,
    secondaryContainer = YarnCandySecondaryContainerDark,
    tertiaryContainer = YarnCandyTertiaryContainerDark,
    error = YarnCandyErrorDark,
    onError = YarnCandyOnErrorDark,
    errorContainer = YarnCandyErrorContainerDark,
    onErrorContainer = YarnCandyOnErrorContainerDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black
)

fun dustyRoseLightColors() = lightColorScheme(
    primary = DustyRosePrimaryLight,
    secondary = DustyRoseSecondaryLight,
    tertiary = DustyRoseTertiaryLight,
    primaryContainer = DustyRosePrimaryContainerLight,
    secondaryContainer = DustyRoseSecondaryContainerLight,
    tertiaryContainer = DustyRoseTertiaryContainerLight,
    error = DustyRoseErrorLight,
    onError = DustyRoseOnErrorLight,
    errorContainer = DustyRoseErrorContainerLight,
    onErrorContainer = DustyRoseOnErrorContainerLight,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White
)

fun dustyRoseDarkColors() = darkColorScheme(
    primary = DustyRosePrimaryDark,
    secondary = DustyRoseSecondaryDark,
    tertiary = DustyRoseTertiaryDark,
    primaryContainer = DustyRosePrimaryContainerDark,
    secondaryContainer = DustyRoseSecondaryContainerDark,
    tertiaryContainer = DustyRoseTertiaryContainerDark,
    error = DustyRoseErrorDark,
    onError = DustyRoseOnErrorDark,
    errorContainer = DustyRoseErrorContainerDark,
    onErrorContainer = DustyRoseOnErrorContainerDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White
)

/**
 * Main theme composable that applies the selected color scheme app-wide.
 * 
 * Flow: ThemeViewModel observes DataStore → emits selected theme → 
 * ThemeManager maps theme to ColorScheme → MaterialTheme applies colors
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

    CompositionLocalProvider(
        LocalQuaternaryColor provides quaternaryColor,
        LocalOnQuaternaryColor provides onQuaternaryColor
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

val MaterialTheme.quaternary: Color
    @Composable get() = LocalQuaternaryColor.current

val MaterialTheme.onQuaternary: Color
    @Composable get() = LocalOnQuaternaryColor.current