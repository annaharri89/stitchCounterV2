package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import kotlin.math.min

@Composable
fun ResizableText(
    text: String,
    modifier: Modifier = Modifier,
    heightRatio: Float = 0.8f,
    widthRatio: Float = 0.4f,
    minFontSize: Float = 48f,
    maxFontSize: Float = 300f,
    fontWeight: FontWeight = FontWeight.Bold,
    textAlign: TextAlign = TextAlign.Center
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        val density = LocalDensity.current
        val fontFamilyResolver = LocalFontFamilyResolver.current
        val layoutDirection = LocalLayoutDirection.current
        val textMeasurer = remember(density, fontFamilyResolver, layoutDirection) { 
            TextMeasurer(
                defaultFontFamilyResolver = fontFamilyResolver,
                defaultDensity = density,
                defaultLayoutDirection = layoutDirection
            )
        }
        
        val baseTypography = MaterialTheme.typography.displayLarge
        
        val initialFontSize = min(
            this.maxHeight.value * heightRatio,
            this.maxWidth.value * widthRatio
        ).coerceIn(minFontSize, maxFontSize)
        
        val maxTextWidth = with(density) { maxWidth.toPx() }
        val maxTextHeight = with(density) { maxHeight.toPx() }
        
        val absoluteMinFontSize = minFontSize * 0.5f
        
        val fontSize = remember(text, maxTextWidth, maxTextHeight, initialFontSize, minFontSize, maxFontSize, fontWeight, baseTypography) {
            var currentSize = initialFontSize
            val baseStyle = TextStyle(
                fontSize = baseTypography.fontSize,
                fontWeight = fontWeight,
                fontFamily = baseTypography.fontFamily,
                letterSpacing = baseTypography.letterSpacing,
                lineHeight = baseTypography.lineHeight
            )
            
            var iterations = 0
            val maxIterations = 30
            
            // First, try to grow the font size if there's extra space
            while (currentSize < maxFontSize && iterations < maxIterations) {
                iterations++
                val testSize = (currentSize * 1.1f).coerceAtMost(maxFontSize)
                val textStyle = baseStyle.copy(fontSize = testSize.sp)
                
                val textLayoutResult = textMeasurer.measure(
                    text = text,
                    style = textStyle,
                    constraints = Constraints(maxWidth = Int.MAX_VALUE)
                )
                
                val measuredWidth = textLayoutResult.size.width.toFloat()
                val measuredHeight = textLayoutResult.size.height.toFloat()
                
                if (measuredWidth <= maxTextWidth && measuredHeight <= maxTextHeight) {
                    currentSize = testSize
                } else {
                    break
                }
            }
            
            // Then, ensure it fits (reduce if needed)
            iterations = 0
            while (currentSize >= absoluteMinFontSize && iterations < maxIterations) {
                iterations++
                val textStyle = baseStyle.copy(fontSize = currentSize.sp)
                
                val textLayoutResult = textMeasurer.measure(
                    text = text,
                    style = textStyle,
                    constraints = Constraints(maxWidth = Int.MAX_VALUE)
                )
                
                val measuredWidth = textLayoutResult.size.width.toFloat()
                val measuredHeight = textLayoutResult.size.height.toFloat()
                
                if (measuredWidth <= maxTextWidth && measuredHeight <= maxTextHeight) {
                    break
                }
                
                val widthScaleFactor = if (measuredWidth > maxTextWidth) maxTextWidth / measuredWidth else 1f
                val heightScaleFactor = if (measuredHeight > maxTextHeight) maxTextHeight / measuredHeight else 1f
                val scaleFactor = min(widthScaleFactor, heightScaleFactor)
                val newSize = currentSize * scaleFactor * 0.98f
                
                if (newSize in absoluteMinFontSize..<currentSize) {
                    currentSize = newSize
                } else if (newSize < absoluteMinFontSize) {
                    currentSize = absoluteMinFontSize
                    break
                } else {
                    break
                }
            }
            
            currentSize.coerceIn(absoluteMinFontSize, maxFontSize)
        }
        
        val finalFontSize = remember { mutableFloatStateOf(fontSize) }
        
        LaunchedEffect(text, fontSize) {
            finalFontSize.floatValue = fontSize
        }
        
        Text(
            text = text,
            maxLines = 1,
            softWrap = false,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = finalFontSize.floatValue.sp,
                fontWeight = fontWeight
            ),
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth(),
            onTextLayout = { layoutResult: TextLayoutResult ->
                val measuredWidth = layoutResult.size.width.toFloat()
                val didOverflow = layoutResult.didOverflowWidth || measuredWidth > maxTextWidth
                val absoluteMin = minFontSize * 0.5f
                
                if (didOverflow && finalFontSize.floatValue > absoluteMin) {
                    val scaleFactor = if (measuredWidth > 0) {
                        (maxTextWidth / measuredWidth * 0.95f).coerceIn(0.5f, 0.95f)
                    } else {
                        0.9f
                    }
                    val newSize = (finalFontSize.floatValue * scaleFactor).coerceAtLeast(absoluteMin)
                    if (newSize < finalFontSize.floatValue) {
                        finalFontSize.floatValue = newSize
                    }
                }
            }
        )
    }
}