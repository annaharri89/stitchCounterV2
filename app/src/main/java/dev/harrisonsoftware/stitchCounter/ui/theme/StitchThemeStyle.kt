package dev.harrisonsoftware.stitchCounter.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ThemeButtonStyleType {
    SOLID_MUTED,
    RETRO_SHADOW,
    GRADIENT_GLOW
}

enum class ThemeHapticStyle {
    MEDIUM,
    RIGID,
    SOFT
}

data class AppThemeStyle(
    val counterFontFamily: FontFamily,
    val headingFontFamily: FontFamily,
    val bodyFontFamily: FontFamily,
    val buttonCornerRadius: Dp,
    val cardCornerRadius: Dp,
    val incrementSymbol: String,
    val decrementSymbol: String,
    val buttonStyleType: ThemeButtonStyleType,
    val hapticStyle: ThemeHapticStyle,
)

fun AppThemeStyle.toTypography(): Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = counterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = counterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = counterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = headingFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = headingFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = headingFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = headingFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = headingFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = headingFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
)

fun AppThemeStyle.buttonShape(): Shape = when (buttonStyleType) {
    ThemeButtonStyleType.RETRO_SHADOW -> ScallopedShape(scallopDepth = 3.dp)
    else -> RoundedCornerShape(buttonCornerRadius)
}

val CottageStyle = AppThemeStyle(
    counterFontFamily = FontFamily.Default,
    headingFontFamily = FontFamily.Default,
    bodyFontFamily = FontFamily.Default,
    buttonCornerRadius = 16.dp,
    cardCornerRadius = 16.dp,
    incrementSymbol = "+",
    decrementSymbol = "−",
    buttonStyleType = ThemeButtonStyleType.SOLID_MUTED,
    hapticStyle = ThemeHapticStyle.MEDIUM,
)

val KitschyRetroStyle = AppThemeStyle(
    counterFontFamily = FontFamily.Monospace,
    headingFontFamily = FontFamily.Monospace,
    bodyFontFamily = FontFamily.Default,
    buttonCornerRadius = 8.dp,
    cardCornerRadius = 8.dp,
    incrementSymbol = "▲",
    decrementSymbol = "▼",
    buttonStyleType = ThemeButtonStyleType.RETRO_SHADOW,
    hapticStyle = ThemeHapticStyle.RIGID,
)

val FairyStyle = AppThemeStyle(
    counterFontFamily = FontFamily.Serif,
    headingFontFamily = FontFamily.Serif,
    bodyFontFamily = FontFamily.Serif,
    buttonCornerRadius = 50.dp,
    cardCornerRadius = 24.dp,
    incrementSymbol = "✦",
    decrementSymbol = "✧",
    buttonStyleType = ThemeButtonStyleType.GRADIENT_GLOW,
    hapticStyle = ThemeHapticStyle.SOFT,
)
