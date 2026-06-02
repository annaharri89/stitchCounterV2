package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

@Composable
fun KeepScreenOnEffect(enabled: Boolean) {
    val view = LocalView.current

    DisposableEffect(view, enabled) {
        if (enabled) {
            view.keepScreenOn = true
        }

        onDispose {
            if (enabled) {
                view.keepScreenOn = false
            }
        }
    }
}
