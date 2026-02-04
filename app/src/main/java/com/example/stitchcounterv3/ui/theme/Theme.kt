package com.example.stitchcounterv3.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.stitchcounterv3.domain.model.AppTheme
import com.example.stitchcounterv3.feature.theme.ThemeManager

val LocalQuaternaryColor = compositionLocalOf<Color> { 
    error("No quaternary color provided") 
}

val LocalOnQuaternaryColor = compositionLocalOf<Color> { 
    error("No onQuaternary color provided") 
}

fun seaCottageLightColors() = lightColorScheme(
    primary = SeaCottageSurf40,
    secondary = SeaCottageMint40,
    tertiary = SeaCottageWhaleLight40,
    primaryContainer = SeaCottagePrimaryContainer40,
    secondaryContainer = SeaCottageSecondaryContainer40,
    tertiaryContainer = SeaCottageTertiaryContainer40,
    onTertiaryContainer = SeaCottageOnTertiaryContainer40,
    error = SeaCottageError40,
    onError = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White
)


fun seaCottageDarkColors() = darkColorScheme(
    primary = SeaCottageSurf80,
    secondary = SeaCottageMint80,
    tertiary = SeaCottageWhaleLight80,
    primaryContainer = SeaCottagePrimaryContainer80,
    secondaryContainer = SeaCottageSecondaryContainer80,
    tertiaryContainer = SeaCottageTertiaryContainer80,
    onTertiaryContainer = SeaCottageOnTertiaryContainer80,
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

fun purpleLightColors() = lightColorScheme(
    primary =  Purple40 ,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    primaryContainer = PurplePrimaryContainer40,
    secondaryContainer = PurpleSecondaryContainer40,
    tertiaryContainer = PurpleTertiaryContainer40,
    onTertiaryContainer = PurpleOnTertiaryContainer40,
    error = PurpleError40,
    onError = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White
)

fun purpleDarkColors() = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    primaryContainer = PurplePrimaryContainer80,
    secondaryContainer = PurpleSecondaryContainer80,
    tertiaryContainer = PurpleTertiaryContainer80,
    onTertiaryContainer = PurpleOnTertiaryContainer80,
    error = PurpleError80,
    onError = Color.White,
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