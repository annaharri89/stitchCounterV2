package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

@Composable
fun AdaptiveLayout(
    isWideLayout: Boolean,
    portraitContent: @Composable ColumnScope.() -> Unit,
    landscapeContent: @Composable RowScope.() -> Unit
) {
    if (isWideLayout) {
        Row(content = landscapeContent)
    } else {
        Column(content = portraitContent)
    }
}