package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import dev.harrisonsoftware.stitchCounter.ui.theme.LocalAppThemeStyle
import dev.harrisonsoftware.stitchCounter.ui.theme.ThemeButtonStyleType
import dev.harrisonsoftware.stitchCounter.ui.theme.ThemeHapticStyle
import dev.harrisonsoftware.stitchCounter.ui.theme.buttonShape

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val themeStyle = LocalAppThemeStyle.current
    val buttonShape = themeStyle.buttonShape()

    when (themeStyle.buttonStyleType) {
        ThemeButtonStyleType.SOLID_MUTED -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                contentPadding = contentPadding,
                content = content
            )
        }

        ThemeButtonStyleType.RETRO_SHADOW -> {
            Button(
                onClick = onClick,
                modifier = modifier.retroOffsetShadow(
                    shape = buttonShape,
                    shadowColor = Color.Black.copy(alpha = 0.35f)
                ),
                enabled = enabled,
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    hoveredElevation = 0.dp
                ),
                border = BorderStroke(1.5.dp, contentColor.copy(alpha = 0.25f)),
                contentPadding = contentPadding,
                content = content
            )
        }

        ThemeButtonStyleType.GRADIENT_GLOW -> {
            val resolvedContainerColor = if (enabled) containerColor
                else containerColor.copy(alpha = 0.38f)
            val gradientEndColor = lerp(resolvedContainerColor, Color.White, 0.35f)
            val gradientBrush = Brush.linearGradient(
                colors = listOf(resolvedContainerColor, gradientEndColor)
            )

            Surface(
                onClick = onClick,
                modifier = modifier
                    .fairyGlowShadow(
                        shape = buttonShape,
                        glowColor = resolvedContainerColor
                    ),
                enabled = enabled,
                shape = buttonShape,
                color = Color.Transparent,
                contentColor = if (enabled) contentColor else contentColor.copy(alpha = 0.38f)
            ) {
                Row(
                    modifier = Modifier
                        .background(brush = gradientBrush)
                        .padding(contentPadding),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    content()
                }
            }
        }
    }
}

private fun Modifier.retroOffsetShadow(
    shape: Shape,
    shadowColor: Color,
    offsetX: Float = 8f,
    offsetY: Float = 8f
): Modifier = this.drawWithContent {
    val outline = shape.createOutline(size, layoutDirection, this)
    translate(left = offsetX, top = offsetY) {
        drawOutline(outline, color = shadowColor)
    }
    drawContent()
}

private fun Modifier.fairyGlowShadow(
    shape: Shape,
    glowColor: Color,
    offsetY: Float = 6f
): Modifier = this.drawWithContent {
    val outline = shape.createOutline(size, layoutDirection, this)
    clipRect(top = size.height * 0.5f) {
        translate(top = offsetY) {
            drawOutline(
                outline,
                color = glowColor.copy(alpha = 0.25f),
            )
        }
    }
    drawContent()
}

@Composable
fun rememberThemedHaptic(): () -> Unit {
    val themeStyle = LocalAppThemeStyle.current
    val view = LocalView.current
    return remember(themeStyle.hapticStyle, view) {
        {
            val hapticConstant = when (themeStyle.hapticStyle) {
                ThemeHapticStyle.MEDIUM -> HapticFeedbackConstants.VIRTUAL_KEY
                ThemeHapticStyle.RIGID -> HapticFeedbackConstants.KEYBOARD_TAP
                ThemeHapticStyle.SOFT -> HapticFeedbackConstants.CLOCK_TICK
            }
            view.performHapticFeedback(hapticConstant)
        }
    }
}
