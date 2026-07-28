package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import android.content.Context
import android.view.View
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class CounterHapticFeedbackTest {

    @Test
    fun `performCounterButtonHapticFeedback uses vibrator click when enabled`() {
        val view = mockk<View>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        every { view.context } returns context
        var clickVibrationCount = 0

        performCounterButtonHapticFeedback(
            view = view,
            enabled = true,
            clickVibration = { clickVibrationCount++ },
        )

        assertEquals(1, clickVibrationCount)
    }

    @Test
    fun `performCounterButtonHapticFeedback does nothing when disabled`() {
        val view = mockk<View>(relaxed = true)
        var clickVibrationCount = 0

        performCounterButtonHapticFeedback(
            view = view,
            enabled = false,
            clickVibration = { clickVibrationCount++ },
        )

        assertEquals(0, clickVibrationCount)
        verify(exactly = 0) { view.context }
    }

    @Test
    fun `runCounterButtonClickWithHapticFeedback invokes click and haptic when enabled`() {
        var clickCount = 0
        var hapticCount = 0

        runCounterButtonClickWithHapticFeedback(
            onClick = { clickCount++ },
            hapticFeedbackEnabled = true,
            performHapticFeedback = { hapticCount++ },
        )

        assertEquals(1, clickCount)
        assertEquals(1, hapticCount)
    }

    @Test
    fun `runCounterButtonClickWithHapticFeedback invokes click but skips haptic when disabled`() {
        var clickCount = 0
        var hapticCount = 0

        runCounterButtonClickWithHapticFeedback(
            onClick = { clickCount++ },
            hapticFeedbackEnabled = false,
            performHapticFeedback = { hapticCount++ },
        )

        assertEquals(1, clickCount)
        assertEquals(0, hapticCount)
    }
}
