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
    onPrimaryContainer = SeaCottageOnPrimaryContainerLight,
    onSecondaryContainer = SeaCottageOnSecondaryContainerLight,
    onTertiaryContainer = SeaCottageOnTertiaryContainerLight,
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

fun goldenHearthLightColors() = lightColorScheme(
    primary = GoldenHearthPrimaryLight,
    secondary = GoldenHearthSecondaryLight,
    tertiary = GoldenHearthTertiaryLight,
    primaryContainer = GoldenHearthPrimaryContainerLight,
    secondaryContainer = GoldenHearthSecondaryContainerLight,
    tertiaryContainer = GoldenHearthTertiaryContainerLight,
    onPrimaryContainer = GoldenHearthOnPrimaryContainerLight,
    onSecondaryContainer = GoldenHearthOnSecondaryContainerLight,
    onTertiaryContainer = GoldenHearthOnTertiaryContainerLight,
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
    onPrimaryContainer = GoldenHearthOnPrimaryContainerDark,
    onSecondaryContainer = GoldenHearthOnSecondaryContainerDark,
    onTertiaryContainer = GoldenHearthOnTertiaryContainerDark,
    error = GoldenHearthErrorDark,
    onError = GoldenHearthOnErrorDark,
    errorContainer = GoldenHearthErrorContainerDark,
    onErrorContainer = GoldenHearthOnErrorContainerDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black
)

fun forestFiberLightColors() = lightColorScheme(
    primary = ForestFiberPrimaryLight,
    secondary = ForestFiberSecondaryLight,
    tertiary = ForestFiberTertiaryLight,
    primaryContainer = ForestFiberPrimaryContainerLight,
    secondaryContainer = ForestFiberSecondaryContainerLight,
    tertiaryContainer = ForestFiberTertiaryContainerLight,
    onPrimaryContainer = ForestFiberOnPrimaryContainerLight,
    onSecondaryContainer = ForestFiberOnSecondaryContainerLight,
    onTertiaryContainer = ForestFiberOnTertiaryContainerLight,
    error = ForestFiberErrorLight,
    onError = ForestFiberOnErrorLight,
    errorContainer = ForestFiberErrorContainerLight,
    onErrorContainer = ForestFiberOnErrorContainerLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White
)

fun forestFiberDarkColors() = darkColorScheme(
    primary = ForestFiberPrimaryDark,
    secondary = ForestFiberSecondaryDark,
    tertiary = ForestFiberTertiaryDark,
    primaryContainer = ForestFiberPrimaryContainerDark,
    secondaryContainer = ForestFiberSecondaryContainerDark,
    tertiaryContainer = ForestFiberTertiaryContainerDark,
    onPrimaryContainer = ForestFiberOnPrimaryContainerDark,
    onSecondaryContainer = ForestFiberOnSecondaryContainerDark,
    onTertiaryContainer = ForestFiberOnTertiaryContainerDark,
    error = ForestFiberErrorDark,
    onError = ForestFiberOnErrorDark,
    errorContainer = ForestFiberErrorContainerDark,
    onErrorContainer = ForestFiberOnErrorContainerDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black
)

fun cloudSoftLightColors() = lightColorScheme(
    primary = CloudSoftPrimaryLight,
    secondary = CloudSoftSecondaryLight,
    tertiary = CloudSoftTertiaryLight,
    primaryContainer = CloudSoftPrimaryContainerLight,
    secondaryContainer = CloudSoftSecondaryContainerLight,
    tertiaryContainer = CloudSoftTertiaryContainerLight,
    onPrimaryContainer = CloudSoftOnPrimaryContainerLight,
    onSecondaryContainer = CloudSoftOnSecondaryContainerLight,
    onTertiaryContainer = CloudSoftOnTertiaryContainerLight,
    error = CloudSoftErrorLight,
    onError = CloudSoftOnErrorLight,
    errorContainer = CloudSoftErrorContainerLight,
    onErrorContainer = CloudSoftOnErrorContainerLight,
    onPrimary = Color.Black,
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
    onPrimaryContainer = CloudSoftOnPrimaryContainerDark,
    onSecondaryContainer = CloudSoftOnSecondaryContainerDark,
    onTertiaryContainer = CloudSoftOnTertiaryContainerDark,
    error = CloudSoftErrorDark,
    onError = CloudSoftOnErrorDark,
    errorContainer = CloudSoftErrorContainerDark,
    onErrorContainer = CloudSoftOnErrorContainerDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black
)

fun yarnCandyLightColors() = lightColorScheme(
    primary = YarnCandyPrimaryLight,
    secondary = YarnCandySecondaryLight,
    tertiary = YarnCandyTertiaryLight,
    primaryContainer = YarnCandyPrimaryContainerLight,
    secondaryContainer = YarnCandySecondaryContainerLight,
    tertiaryContainer = YarnCandyTertiaryContainerLight,
    onPrimaryContainer = YarnCandyOnPrimaryContainerLight,
    onSecondaryContainer = YarnCandyOnSecondaryContainerLight,
    onTertiaryContainer = YarnCandyOnTertiaryContainerLight,
    error = YarnCandyErrorLight,
    onError = YarnCandyOnErrorLight,
    errorContainer = YarnCandyErrorContainerLight,
    onErrorContainer = YarnCandyOnErrorContainerLight,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black
)

fun yarnCandyDarkColors() = darkColorScheme(
    primary = YarnCandyPrimaryDark,
    secondary = YarnCandySecondaryDark,
    tertiary = YarnCandyTertiaryDark,
    primaryContainer = YarnCandyPrimaryContainerDark,
    secondaryContainer = YarnCandySecondaryContainerDark,
    tertiaryContainer = YarnCandyTertiaryContainerDark,
    onPrimaryContainer = YarnCandyOnPrimaryContainerDark,
    onSecondaryContainer = YarnCandyOnSecondaryContainerDark,
    onTertiaryContainer = YarnCandyOnTertiaryContainerDark,
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
    onPrimaryContainer = DustyRoseOnPrimaryContainerLight,
    onSecondaryContainer = DustyRoseOnSecondaryContainerLight,
    onTertiaryContainer = DustyRoseOnTertiaryContainerLight,
    error = DustyRoseErrorLight,
    onError = DustyRoseOnErrorLight,
    errorContainer = DustyRoseErrorContainerLight,
    onErrorContainer = DustyRoseOnErrorContainerLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White
)

fun dustyRoseDarkColors() = darkColorScheme(
    primary = DustyRosePrimaryDark,
    secondary = DustyRoseSecondaryDark,
    tertiary = DustyRoseTertiaryDark,
    primaryContainer = DustyRosePrimaryContainerDark,
    secondaryContainer = DustyRoseSecondaryContainerDark,
    tertiaryContainer = DustyRoseTertiaryContainerDark,
    onPrimaryContainer = DustyRoseOnPrimaryContainerDark,
    onSecondaryContainer = DustyRoseOnSecondaryContainerDark,
    onTertiaryContainer = DustyRoseOnTertiaryContainerDark,
    error = DustyRoseErrorDark,
    onError = DustyRoseOnErrorDark,
    errorContainer = DustyRoseErrorContainerDark,
    onErrorContainer = DustyRoseOnErrorContainerDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black
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
    theme: AppTheme = AppTheme.DUSTY_ROSE,
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
