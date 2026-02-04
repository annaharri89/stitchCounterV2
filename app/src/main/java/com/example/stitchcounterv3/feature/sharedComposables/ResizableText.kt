package com.example.stitchcounterv3.feature.sharedComposables

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
        
        val absoluteMinFontSize = minFontSize * 0.5f
        
        val fontSize = remember(text, maxTextWidth, initialFontSize, minFontSize, maxFontSize, fontWeight, baseTypography) {
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
            
            while (currentSize >= absoluteMinFontSize && iterations < maxIterations) {
                iterations++
                val textStyle = baseStyle.copy(fontSize = currentSize.sp)
                
                val textLayoutResult = textMeasurer.measure(
                    text = text,
                    style = textStyle,
                    constraints = Constraints(maxWidth = Int.MAX_VALUE)
                )
                
                val measuredWidth = textLayoutResult.size.width.toFloat()
                
                if (measuredWidth <= maxTextWidth) {
                    break
                }
                
                val scaleFactor = maxTextWidth / measuredWidth
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
        
        var finalFontSize by remember { mutableStateOf(fontSize) }
        
        LaunchedEffect(text, fontSize) {
            finalFontSize = fontSize
        }
        
        Text(
            text = text,
            maxLines = 1,
            softWrap = false,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = finalFontSize.sp,
                fontWeight = fontWeight
            ),
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth(),
            onTextLayout = { layoutResult: TextLayoutResult ->
                val measuredWidth = layoutResult.size.width.toFloat()
                val didOverflow = layoutResult.didOverflowWidth || measuredWidth > maxTextWidth
                val absoluteMin = minFontSize * 0.5f
                
                if (didOverflow && finalFontSize > absoluteMin) {
                    val scaleFactor = if (measuredWidth > 0) {
                        (maxTextWidth / measuredWidth * 0.95f).coerceIn(0.5f, 0.95f)
                    } else {
                        0.9f
                    }
                    val newSize = (finalFontSize * scaleFactor).coerceAtLeast(absoluteMin)
                    if (newSize < finalFontSize) {
                        finalFontSize = newSize
                    }
                }
            }
        )
    }
}