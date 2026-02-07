package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable

@Composable
fun AdaptiveLayout(
    windowSizeClass: WindowSizeClass,
    portraitContent: @Composable ColumnScope.() -> Unit,
    landscapeContent: @Composable RowScope.() -> Unit
) {
    val isLandscape = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded ||
                      windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium
    
    if (isLandscape) {
        Row(content = landscapeContent)
    } else {
        Column(content = portraitContent)
    }
}