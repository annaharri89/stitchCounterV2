package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import android.provider.Settings
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.harrisonsoftware.stitchCounter.ui.theme.LocalAppThemeStyle
import dev.harrisonsoftware.stitchCounter.ui.theme.ThemeButtonStyleType
import kotlin.math.cos
import kotlin.math.sin

private data class SparkleParticle(
    val relativeX: Float,
    val relativeY: Float,
    val outerRadius: Dp,
    val innerRadius: Dp,
    val animationDelayMs: Int,
    val animationDurationMs: Int
)

private val SPARKLE_PARTICLES = listOf(
    SparkleParticle(0.10f, 0.06f, 5.dp, 1.5.dp, 0, 2400),
    SparkleParticle(0.90f, 0.12f, 7.dp, 2.dp, 400, 2800),
    SparkleParticle(0.04f, 0.42f, 4.dp, 1.2.dp, 800, 2200),
    SparkleParticle(0.94f, 0.50f, 6.dp, 1.8.dp, 1200, 2600),
    SparkleParticle(0.13f, 0.76f, 5.dp, 1.5.dp, 600, 2500),
    SparkleParticle(0.87f, 0.83f, 4.dp, 1.2.dp, 1000, 2300),
    SparkleParticle(0.52f, 0.03f, 3.5.dp, 1.dp, 200, 2000),
    SparkleParticle(0.48f, 0.94f, 4.dp, 1.2.dp, 1400, 2700),
    SparkleParticle(0.30f, 0.30f, 3.dp, 0.8.dp, 1600, 2100),
    SparkleParticle(0.72f, 0.65f, 3.5.dp, 1.dp, 1800, 2400),
)

private const val SPARKLE_MAX_ALPHA = 0.55f
private const val SPARKLE_STATIC_ALPHA = 0.35f
private const val FOUR_POINT_STAR_POINTS = 4
private const val STAR_ANGLE_OFFSET_DEGREES = -90f
private const val DEGREES_PER_POINT = 360f / (FOUR_POINT_STAR_POINTS * 2)
private const val DEGREES_TO_RADIANS = Math.PI.toFloat() / 180f

/**
 * Draws twinkling four-pointed star particles across the composable area.
 * Only renders when the active theme uses GRADIENT_GLOW (Fairy theme).
 * Respects the system's "Remove animations" accessibility setting —
 * when enabled, shows static sparkles instead of animating.
 */
@Composable
fun FairySparkleOverlay(modifier: Modifier = Modifier) {
    val themeStyle = LocalAppThemeStyle.current
    if (themeStyle.buttonStyleType != ThemeButtonStyleType.GRADIENT_GLOW) return

    val reduceMotionEnabled = rememberReduceMotionEnabled()
    val sparkleColor = MaterialTheme.colorScheme.primary

    if (reduceMotionEnabled) {
        StaticSparkles(sparkleColor = sparkleColor, modifier = modifier)
    } else {
        AnimatedSparkles(sparkleColor = sparkleColor, modifier = modifier)
    }
}

@Composable
private fun StaticSparkles(
    sparkleColor: Color,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val particleRadiiPx = remember(density) {
        SPARKLE_PARTICLES.map { particle ->
            with(density) {
                particle.outerRadius.toPx() to particle.innerRadius.toPx()
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        SPARKLE_PARTICLES.forEachIndexed { index, particle ->
            val center = Offset(
                size.width * particle.relativeX,
                size.height * particle.relativeY
            )
            val (outerPx, innerPx) = particleRadiiPx[index]
            drawFourPointStar(center, outerPx, innerPx, sparkleColor, SPARKLE_STATIC_ALPHA)
        }
    }
}

@Composable
private fun AnimatedSparkles(
    sparkleColor: Color,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val particleRadiiPx = remember(density) {
        SPARKLE_PARTICLES.map { particle ->
            with(density) {
                particle.outerRadius.toPx() to particle.innerRadius.toPx()
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "fairySparkle")

    val particleAlphas: List<State<Float>> = SPARKLE_PARTICLES.map { particle ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = particle.animationDurationMs
                    0f at 0
                    SPARKLE_MAX_ALPHA at (particle.animationDurationMs * 0.3f).toInt()
                    SPARKLE_MAX_ALPHA at (particle.animationDurationMs * 0.45f).toInt()
                    0f at (particle.animationDurationMs * 0.7f).toInt()
                    0f at particle.animationDurationMs
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(particle.animationDelayMs)
            ),
            label = "sparkleAlpha"
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        SPARKLE_PARTICLES.forEachIndexed { index, particle ->
            val alpha = particleAlphas[index].value
            if (alpha > 0.01f) {
                val center = Offset(
                    size.width * particle.relativeX,
                    size.height * particle.relativeY
                )
                val (outerPx, innerPx) = particleRadiiPx[index]
                drawFourPointStar(center, outerPx, innerPx, sparkleColor, alpha)
            }
        }
    }
}

private fun DrawScope.drawFourPointStar(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    color: Color,
    alpha: Float
) {
    val starPath = Path().apply {
        for (i in 0 until FOUR_POINT_STAR_POINTS * 2) {
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val angleDegrees = STAR_ANGLE_OFFSET_DEGREES + i * DEGREES_PER_POINT
            val angleRadians = angleDegrees * DEGREES_TO_RADIANS
            val x = center.x + radius * cos(angleRadians)
            val y = center.y + radius * sin(angleRadians)
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    drawPath(starPath, color = color, alpha = alpha)
}

@Composable
private fun rememberReduceMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        } catch (_: Exception) {
            false
        }
    }
}
