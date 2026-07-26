package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import org.junit.Assert.assertEquals
import org.junit.Test

class EdgeToEdgeInsetsTest {

    @Test
    fun `sheetHeaderInsetStrategy uses horizontal safe drawing with fixed top in landscape`() {
        assertEquals(
            SheetHeaderInsetStrategy.LANDSCAPE_HORIZONTAL_WITH_FIXED_TOP,
            sheetHeaderInsetStrategy(isWideLayout = true)
        )
    }

    @Test
    fun `sheetHeaderInsetStrategy uses status bars in portrait`() {
        assertEquals(
            SheetHeaderInsetStrategy.PORTRAIT_STATUS_BARS,
            sheetHeaderInsetStrategy(isWideLayout = false)
        )
    }
}
