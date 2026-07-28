package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AdaptiveLayout(
    isWideLayout: Boolean,
    portraitContent: @Composable ColumnScope.() -> Unit,
    landscapeContent: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (isWideLayout) {
            landscapeContent()
        } else {
            portraitContent()
        }
    }
}
