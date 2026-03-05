package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.runtime.Immutable

@Immutable
data class CustomAdjustmentDialogState(
    val isVisible: Boolean = false,
    val input: String = "",
)
