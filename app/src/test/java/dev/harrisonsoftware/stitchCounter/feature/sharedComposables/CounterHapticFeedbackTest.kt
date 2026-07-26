package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import android.view.HapticFeedbackConstants
import android.view.View
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class CounterHapticFeedbackTest {

    @Test
    fun `performCounterButtonHapticFeedback triggers context click when enabled`() {
        val view = mockk<View>(relaxed = true)
        every { view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK) } returns true

        performCounterButtonHapticFeedback(view, enabled = true)

        verify(exactly = 1) { view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK) }
    }

    @Test
    fun `performCounterButtonHapticFeedback does nothing when disabled`() {
        val view = mockk<View>(relaxed = true)

        performCounterButtonHapticFeedback(view, enabled = false)

        verify(exactly = 0) { view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK) }
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
