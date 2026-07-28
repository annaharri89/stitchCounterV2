package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import dev.harrisonsoftware.stitchCounter.Constants
import timber.log.Timber

val LocalCounterHapticFeedbackEnabled = compositionLocalOf { true }

private const val COUNTER_CLICK_VIBRATION_DURATION_MS = 40L

@Composable
fun CounterHapticFeedbackProvider(
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalCounterHapticFeedbackEnabled provides enabled) {
        content()
    }
}

fun performCounterButtonHapticFeedback(
    view: View,
    enabled: Boolean,
    clickVibration: (Context) -> Unit = ::performClickVibration,
) {
    if (!enabled) {
        return
    }
    clickVibration(view.context)
}

internal fun performClickVibration(context: Context) {
    runCatching {
        if (!isSystemHapticFeedbackEnabled(context)) {
            return
        }
        val vibrator = context.defaultVibrator() ?: return
        if (!vibrator.hasVibrator()) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            vibrator.vibrate(COUNTER_CLICK_VIBRATION_DURATION_MS)
            return
        }
        val effect = VibrationEffect.createOneShot(
            COUNTER_CLICK_VIBRATION_DURATION_MS,
            VibrationEffect.DEFAULT_AMPLITUDE,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val attributes = VibrationAttributes.Builder()
                .setUsage(VibrationAttributes.USAGE_TOUCH)
                .build()
            vibrator.vibrate(effect, attributes)
        } else {
            vibrator.vibrate(effect)
        }
    }.onFailure { error ->
        Timber.tag(Constants.LOG_TAG_COUNTER_HAPTIC)
            .w(error, "event=vibrator_failed")
    }
}

@Suppress("DEPRECATION")
internal fun isSystemHapticFeedbackEnabled(context: Context): Boolean {
    return Settings.System.getInt(
        context.contentResolver,
        Settings.System.HAPTIC_FEEDBACK_ENABLED,
        1,
    ) != 0
}

private fun Context.defaultVibrator(): Vibrator? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        getSystemService(Vibrator::class.java)
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
