package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ── Default colors ──────────────────────────────────────────────────
private val DefaultSparklingHeartColor = Color(0xFFE91E63)
private val HeartHighlightColor = Color(0xFFFF80AB)
private val SparkleWhiteColor = Color(0xFFFFFFFF)
private val SparkleYellowColor = Color(0xFFFFF9C4)
private val SparklePinkColor = Color(0xFFF8BBD0)

// ── Math ────────────────────────────────────────────────────────────
private const val TWO_PI_F = (2.0 * PI).toFloat()

// ── Animation timing (coprime durations avoid repeating patterns) ───
private const val ORBIT_CYCLE_DURATION_MS = 3700
private const val SPARKLE_PULSE_DURATION_MS = 1300
private const val SECONDARY_ORBIT_DURATION_MS = 2900
private const val TERTIARY_ORBIT_DURATION_MS = 4300
private const val HEART_PULSE_DURATION_MS = 1900

// ── Heart layout (fractions of canvas dimensions) ───────────────────
private const val HEART_CENTER_Y_FRACTION = 0.50f
private const val HEART_HEIGHT_FRACTION = 0.60f
private const val HEART_WIDTH_RATIO = 0.55f
private const val HEART_CLEFT_DEPTH_RATIO = 0.15f

// ── Heart pulse ─────────────────────────────────────────────────────
private const val HEART_PULSE_AMPLITUDE = 0.03f

// ── Highlight on heart ──────────────────────────────────────────────
private const val HIGHLIGHT_OFFSET_X_FRACTION = -0.18f
private const val HIGHLIGHT_OFFSET_Y_FRACTION = -0.18f
private const val HIGHLIGHT_RADIUS_FRACTION = 0.14f
private const val HIGHLIGHT_ALPHA = 0.45f

// ── Sparkle rendering ───────────────────────────────────────────────
private const val SPARKLE_OUTER_LENGTH_FRACTION = 0.12f
private const val SPARKLE_INNER_LENGTH_RATIO = 0.30f
private const val SPARKLE_GLOW_RADIUS_RATIO = 2.0f
private const val SPARKLE_GLOW_ALPHA_RATIO = 0.25f
private const val SPARKLE_MIN_ALPHA = 0.30f
private const val SPARKLE_MAX_ALPHA = 0.95f

// ── Orbit radii (fractions of half-canvas-width) ────────────────────
private const val ORBIT_RADIUS_INNER_FRACTION = 0.72f
private const val ORBIT_RADIUS_MIDDLE_FRACTION = 0.88f
private const val ORBIT_RADIUS_OUTER_FRACTION = 1.02f

/**
 * Animated sparkling-heart icon (💖): a pink heart with a soft
 * highlight and multiple four-pointed sparkles that orbit around it
 * at varying speeds, radii, and phase offsets. Sparkles pulse in
 * size and brightness as they travel. Coprime animation durations
 * prevent visible looping.
 *
 * When system animations are disabled the transition freezes at its
 * initial value, producing a static composition.
 */
@Composable
fun SparklingHeartIcon(
    modifier: Modifier = Modifier,
    heartColor: Color = DefaultSparklingHeartColor,
    accessibilityLabel: String = "Sparkling heart",
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkling_heart")

    val orbitPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(ORBIT_CYCLE_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_phase"
    )
    val secondaryOrbitPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(SECONDARY_ORBIT_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "secondary_orbit_phase"
    )
    val tertiaryOrbitPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(TERTIARY_ORBIT_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tertiary_orbit_phase"
    )
    val sparklePulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(SPARKLE_PULSE_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle_pulse"
    )
    val heartPulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(HEART_PULSE_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "heart_pulse"
    )

    Box(
        modifier = modifier
            .size(24.dp)
            .semantics { contentDescription = accessibilityLabel },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth * 0.5f
            val centerY = canvasHeight * HEART_CENTER_Y_FRACTION

            val pulseScale = 1f + HEART_PULSE_AMPLITUDE *
                    sin(heartPulsePhase * TWO_PI_F)
            val heartHeight = canvasHeight * HEART_HEIGHT_FRACTION * pulseScale
            val heartBottomY = centerY + heartHeight * 0.52f

            // ── Heart shape ─────────────────────────────────────────
            drawHeartShape(centerX, heartBottomY, heartHeight, heartColor)

            // ── Highlight (gives the 💖 sheen) ─────────────────────
            val highlightCenterX = centerX + canvasWidth * HIGHLIGHT_OFFSET_X_FRACTION
            val highlightCenterY = centerY + canvasHeight * HIGHLIGHT_OFFSET_Y_FRACTION
            drawCircle(
                color = HeartHighlightColor.copy(alpha = HIGHLIGHT_ALPHA),
                radius = canvasWidth * HIGHLIGHT_RADIUS_FRACTION,
                center = Offset(highlightCenterX, highlightCenterY)
            )

            // ── Orbiting sparkles ───────────────────────────────────
            val halfWidth = canvasWidth * 0.5f

            // Sparkle 1 — large, white, inner orbit
            drawOrbitingSparkle(
                centerX = centerX,
                centerY = centerY,
                orbitRadiusX = halfWidth * ORBIT_RADIUS_MIDDLE_FRACTION,
                orbitRadiusY = halfWidth * ORBIT_RADIUS_MIDDLE_FRACTION * 0.85f,
                anglePhase = orbitPhase,
                pulsePhase = sparklePulsePhase,
                sparkleColor = SparkleWhiteColor,
                outerLength = canvasWidth * SPARKLE_OUTER_LENGTH_FRACTION
            )

            // Sparkle 2 — medium, yellow, outer orbit, offset
            drawOrbitingSparkle(
                centerX = centerX,
                centerY = centerY,
                orbitRadiusX = halfWidth * ORBIT_RADIUS_OUTER_FRACTION,
                orbitRadiusY = halfWidth * ORBIT_RADIUS_OUTER_FRACTION * 0.80f,
                anglePhase = secondaryOrbitPhase,
                pulsePhase = (sparklePulsePhase + 0.33f) % 1f,
                sparkleColor = SparkleYellowColor,
                outerLength = canvasWidth * SPARKLE_OUTER_LENGTH_FRACTION * 0.75f
            )

            // Sparkle 3 — small, pink, inner orbit, opposite side
            drawOrbitingSparkle(
                centerX = centerX,
                centerY = centerY,
                orbitRadiusX = halfWidth * ORBIT_RADIUS_INNER_FRACTION,
                orbitRadiusY = halfWidth * ORBIT_RADIUS_INNER_FRACTION * 0.90f,
                anglePhase = tertiaryOrbitPhase,
                pulsePhase = (sparklePulsePhase + 0.66f) % 1f,
                sparkleColor = SparklePinkColor,
                outerLength = canvasWidth * SPARKLE_OUTER_LENGTH_FRACTION * 0.60f
            )

            // Sparkle 4 — medium, white, middle orbit, phase-shifted
            drawOrbitingSparkle(
                centerX = centerX,
                centerY = centerY,
                orbitRadiusX = halfWidth * ORBIT_RADIUS_MIDDLE_FRACTION * 0.95f,
                orbitRadiusY = halfWidth * ORBIT_RADIUS_MIDDLE_FRACTION * 0.78f,
                anglePhase = (orbitPhase + 0.50f) % 1f,
                pulsePhase = (sparklePulsePhase + 0.50f) % 1f,
                sparkleColor = SparkleWhiteColor,
                outerLength = canvasWidth * SPARKLE_OUTER_LENGTH_FRACTION * 0.70f
            )

            // Sparkle 5 — tiny, yellow, outer orbit, phase-shifted
            drawOrbitingSparkle(
                centerX = centerX,
                centerY = centerY,
                orbitRadiusX = halfWidth * ORBIT_RADIUS_OUTER_FRACTION * 0.92f,
                orbitRadiusY = halfWidth * ORBIT_RADIUS_OUTER_FRACTION * 0.75f,
                anglePhase = (secondaryOrbitPhase + 0.55f) % 1f,
                pulsePhase = (sparklePulsePhase + 0.15f) % 1f,
                sparkleColor = SparkleYellowColor,
                outerLength = canvasWidth * SPARKLE_OUTER_LENGTH_FRACTION * 0.50f
            )
        }
    }
}

// ── Private drawing helpers ─────────────────────────────────────────

private fun DrawScope.drawHeartShape(
    centerX: Float,
    bottomY: Float,
    heartHeight: Float,
    color: Color,
) {
    val topY = bottomY - heartHeight
    val halfWidth = heartHeight * HEART_WIDTH_RATIO
    val cleftY = topY + heartHeight * HEART_CLEFT_DEPTH_RATIO

    val heartPath = Path().apply {
        moveTo(centerX, bottomY)

        cubicTo(
            centerX - halfWidth * 0.10f, bottomY - heartHeight * 0.22f,
            centerX - halfWidth * 1.12f, bottomY - heartHeight * 0.55f,
            centerX - halfWidth * 0.90f, topY + heartHeight * 0.15f
        )
        cubicTo(
            centerX - halfWidth * 0.68f, topY - heartHeight * 0.06f,
            centerX - halfWidth * 0.15f, topY - heartHeight * 0.01f,
            centerX, cleftY
        )
        cubicTo(
            centerX + halfWidth * 0.15f, topY - heartHeight * 0.01f,
            centerX + halfWidth * 0.68f, topY - heartHeight * 0.06f,
            centerX + halfWidth * 0.90f, topY + heartHeight * 0.15f
        )
        cubicTo(
            centerX + halfWidth * 1.12f, bottomY - heartHeight * 0.55f,
            centerX + halfWidth * 0.10f, bottomY - heartHeight * 0.22f,
            centerX, bottomY
        )

        close()
    }

    drawPath(heartPath, color)
}

private fun DrawScope.drawOrbitingSparkle(
    centerX: Float,
    centerY: Float,
    orbitRadiusX: Float,
    orbitRadiusY: Float,
    anglePhase: Float,
    pulsePhase: Float,
    sparkleColor: Color,
    outerLength: Float,
) {
    val angle = anglePhase * TWO_PI_F
    val sparkleX = centerX + cos(angle) * orbitRadiusX
    val sparkleY = centerY + sin(angle) * orbitRadiusY

    val pulseFactor = 0.65f + 0.35f * sin(pulsePhase * TWO_PI_F)
    val scaledOuterLength = outerLength * pulseFactor
    val innerLength = scaledOuterLength * SPARKLE_INNER_LENGTH_RATIO

    val alpha = SPARKLE_MIN_ALPHA +
            (SPARKLE_MAX_ALPHA - SPARKLE_MIN_ALPHA) * pulseFactor

    drawFourPointedSparkle(
        centerX = sparkleX,
        centerY = sparkleY,
        outerLength = scaledOuterLength,
        innerLength = innerLength,
        color = sparkleColor,
        alpha = alpha
    )
}

private fun DrawScope.drawFourPointedSparkle(
    centerX: Float,
    centerY: Float,
    outerLength: Float,
    innerLength: Float,
    color: Color,
    alpha: Float,
) {
    // Soft glow behind the sparkle
    drawCircle(
        color = color.copy(alpha = alpha * SPARKLE_GLOW_ALPHA_RATIO),
        radius = outerLength * SPARKLE_GLOW_RADIUS_RATIO,
        center = Offset(centerX, centerY)
    )

    // Four-pointed star: points at N, E, S, W with diagonal inner vertices
    val sparklePath = Path().apply {
        // Top point
        moveTo(centerX, centerY - outerLength)
        // Upper-right inner
        lineTo(centerX + innerLength, centerY - innerLength)
        // Right point
        lineTo(centerX + outerLength, centerY)
        // Lower-right inner
        lineTo(centerX + innerLength, centerY + innerLength)
        // Bottom point
        lineTo(centerX, centerY + outerLength)
        // Lower-left inner
        lineTo(centerX - innerLength, centerY + innerLength)
        // Left point
        lineTo(centerX - outerLength, centerY)
        // Upper-left inner
        lineTo(centerX - innerLength, centerY - innerLength)
        close()
    }

    drawPath(sparklePath, color.copy(alpha = alpha))
}
