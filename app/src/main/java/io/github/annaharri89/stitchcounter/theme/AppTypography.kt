package io.github.annaharri89.stitchcounter.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/*
data class AppTypography(
    val body1: TextStyle = TextStyle(
        fontFamily = josefinSansRegular,
        fontSize = 32.sp,
        fontWeight = FontWeight.Normal,
    ),
    val body2: TextStyle = TextStyle(
        fontFamily = josefinSansRegular,
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal,
    ),
    val body3: TextStyle = TextStyle(
        fontFamily = josefinSansRegular,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
    ),
    val body4: TextStyle = TextStyle(
        fontFamily = josefinSansRegular,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
    ),
    val body5: TextStyle = TextStyle(
        fontFamily = josefinSansRegular,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
    ),
    val body6: TextStyle = TextStyle(
        fontFamily = josefinSansRegular,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
    ),
    val overLine1: TextStyle = TextStyle(//when overline is used it should be all caps
        fontFamily = josefinSansMedium,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
    ),
    val overLine2: TextStyle = TextStyle(//when overline is used it should be all caps
        fontFamily = josefinSansMedium,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
    ),
    val subtitle1: TextStyle = TextStyle(
        fontFamily = josefinSansBold,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
    ),
    val subtitle2: TextStyle = TextStyle(
        fontFamily = josefinSansBold,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    ),
    val subtitle3: TextStyle = TextStyle(
        fontFamily = josefinSansBold,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
    ),
    val subtitle4: TextStyle = TextStyle(
        fontFamily = josefinSansBold,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
    ),
    val subtitle5: TextStyle = TextStyle(
        fontFamily = josefinSansBold,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
    ),
    val h1: TextStyle = TextStyle(
        fontFamily = kalniaGlazeSemiBold,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp),
    val h2: TextStyle = TextStyle(
        fontFamily = kalniaGlazeSemiBold,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp),
    val h3: TextStyle = TextStyle(
        fontFamily = kalniaGlazeSemiBold,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp),
    val h4: TextStyle = TextStyle(
        fontFamily = kalniaGlazeSemiBold,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        fontSize = 26.sp),
    val h5: TextStyle = TextStyle(
        fontFamily = kalniaGlazeSemiBold,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    ),
    val h6: TextStyle = TextStyle(
        fontFamily = kalniaGlazeSemiBold,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    ),
    val h7: TextStyle = TextStyle(
        fontFamily = kalniaGlazeSemiBold,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
)*/

data class AppTypography(
    val body1: TextStyle = TextStyle(
        fontFamily = loraRegular,
        fontSize = 32.sp,
        fontWeight = FontWeight.Normal,
    ),
    val body2: TextStyle = TextStyle(
        fontFamily = loraRegular,
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal,
    ),
    val body3: TextStyle = TextStyle(
        fontFamily = loraRegular,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
    ),
    val body4: TextStyle = TextStyle(
        fontFamily = loraRegular,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
    ),
    val body5: TextStyle = TextStyle(
        fontFamily = loraRegular,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
    ),
    val body6: TextStyle = TextStyle(
        fontFamily = loraRegular,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
    ),
    val overLine1: TextStyle = TextStyle(//when overline is used it should be all caps
        fontFamily = loraMedium,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
    ),
    val overLine2: TextStyle = TextStyle(//when overline is used it should be all caps
        fontFamily = loraMedium,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
    ),
    val subtitle1: TextStyle = TextStyle(
        fontFamily = loraBold,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
    ),
    val subtitle2: TextStyle = TextStyle(
        fontFamily = loraBold,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    ),
    val subtitle3: TextStyle = TextStyle(
        fontFamily = loraBold,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
    ),
    val subtitle4: TextStyle = TextStyle(
        fontFamily = loraBold,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
    ),
    val subtitle5: TextStyle = TextStyle(
        fontFamily = loraBold,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
    ),
    val h1: TextStyle = TextStyle(
        fontFamily = dancingScriptBold,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.W900,
        fontSize = 32.sp),
    val h2: TextStyle = TextStyle(
        fontFamily = dancingScriptBold,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp),
    val h3: TextStyle = TextStyle(
        fontFamily = dancingScriptBold,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp),
    val h4: TextStyle = TextStyle(
        fontFamily = dancingScriptBold,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        fontSize = 26.sp),
    val h5: TextStyle = TextStyle(
        fontFamily = dancingScriptBold,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    ),
    val h6: TextStyle = TextStyle(
        fontFamily = dancingScriptBold,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    ),
    val h7: TextStyle = TextStyle(
        fontFamily = dancingScriptBold,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
)

val LocalTypography = staticCompositionLocalOf { AppTypography() }