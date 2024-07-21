package io.github.annaharri89.stitchcounter.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

data class AppShapes(
    val s: androidx.compose.ui.graphics.Shape = RoundedCornerShape(4.dp),
    val m: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp),
    val l: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    val pillButton: androidx.compose.ui.graphics.Shape = RoundedCornerShape(50.dp),
    val squarePillButton: androidx.compose.ui.graphics.Shape = RoundedCornerShape(7.dp),
    val square: androidx.compose.ui.graphics.Shape = RoundedCornerShape(0.dp),
    val circle: androidx.compose.ui.graphics.Shape = CircleShape,
    val card: androidx.compose.ui.graphics.Shape = RoundedCornerShape(20.dp),
    val topRoundedCornersCard: androidx.compose.ui.graphics.Shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    val bottomRoundedCornersCard: androidx.compose.ui.graphics.Shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
)

val LocalShapes = staticCompositionLocalOf { AppShapes() }