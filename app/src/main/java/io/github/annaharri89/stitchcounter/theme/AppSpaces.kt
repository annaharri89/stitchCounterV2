package io.github.annaharri89.stitchcounter.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppSpaces(
    val s: Dp = 4.dp,
    val m: Dp = 8.dp,
    val l: Dp = 16.dp,
    val xL: Dp = 20.dp,
    val xxL: Dp = 24.dp,
    val buttonHeight: Dp = 50.dp
)

val LocalSpaces = staticCompositionLocalOf { AppSpaces() }