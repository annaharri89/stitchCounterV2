package io.github.annaharri89.stitchcounter.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode

object STTheme {

    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    val typography: AppTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    val shapes: AppShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalShapes.current

    val spaces: AppSpaces
        @Composable
        @ReadOnlyComposable
        get() = LocalSpaces.current
}

@Composable
fun Theme(
    spaces: AppSpaces = STTheme.spaces,
    shapes: AppShapes = STTheme.shapes,
    typography: AppTypography = STTheme.typography,
    colors: AppColors = STTheme.colors,
    darkColors: AppColors? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val currentColor = if (darkColors != null && darkTheme) darkColors else colors
    val rememberedColors = remember { currentColor.copy() }.apply { updateColorsFrom(currentColor) }
    CompositionLocalProvider(
        LocalColors provides rememberedColors,
        LocalShapes provides shapes,
        LocalTypography provides typography,
        LocalSpaces provides spaces, content = content)
}

@Composable
fun AppThemeProvider(darkTheme: Boolean = isSystemInDarkTheme(),
                     darkColors: AppColors,
                     lightColors: AppColors,
                     content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        darkColors
    } else {
        lightColors
    }

    Theme(
        colors = colors,
        spaces = Spaces,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}