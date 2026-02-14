package dev.harrisonsoftware.stitchCounter.ui.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class ScallopedShape(
    private val scallopDepth: Dp = 3.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val depth = with(density) { scallopDepth.toPx() }
        val scallopWidth = depth * 5f
        val horizontalCount = (size.width / scallopWidth).toInt().coerceAtLeast(3)
        val actualScallopWidth = size.width / horizontalCount

        val path = Path().apply {
            moveTo(0f, depth)

            for (i in 0 until horizontalCount) {
                val controlX = i * actualScallopWidth + actualScallopWidth / 2
                val endX = (i + 1) * actualScallopWidth
                quadraticBezierTo(controlX, -depth * 0.4f, endX, depth)
            }

            lineTo(size.width, size.height - depth)

            for (i in horizontalCount - 1 downTo 0) {
                val controlX = i * actualScallopWidth + actualScallopWidth / 2
                val endX = i * actualScallopWidth
                quadraticBezierTo(controlX, size.height + depth * 0.4f, endX, size.height - depth)
            }

            close()
        }

        return Outline.Generic(path)
    }
}
