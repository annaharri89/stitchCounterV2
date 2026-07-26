package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

val LocalCounterHapticFeedbackEnabled = compositionLocalOf { true }

@Composable
fun CounterHapticFeedbackProvider(
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalCounterHapticFeedbackEnabled provides enabled) {
        content()
    }
}

fun performCounterButtonHapticFeedback(view: View, enabled: Boolean) {
    if (enabled) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }
}

internal fun runCounterButtonClickWithHapticFeedback(
    onClick: () -> Unit,
    hapticFeedbackEnabled: Boolean,
    performHapticFeedback: () -> Unit,
) {
    onClick()
    if (hapticFeedbackEnabled) {
        performHapticFeedback()
    }
}

@Composable
fun rememberCounterButtonClickHandler(onClick: () -> Unit): () -> Unit {
    val view = LocalView.current
    val hapticFeedbackEnabled = LocalCounterHapticFeedbackEnabled.current

    return remember(onClick, hapticFeedbackEnabled, view) {
        {
            runCounterButtonClickWithHapticFeedback(
                onClick = onClick,
                hapticFeedbackEnabled = hapticFeedbackEnabled,
                performHapticFeedback = { performCounterButtonHapticFeedback(view, enabled = true) },
            )
        }
    }
}
