package dev.harrisonsoftware.stitchCounter.feature.rowandrepeat

import org.junit.Assert.assertEquals
import org.junit.Test

class RowAndRepeatProjectValuesTest {

    @Test
    fun `rowsPerRepeatFromStoredValue uses default when row adjustment is one`() {
        assertEquals(8, RowAndRepeatProjectValues.rowsPerRepeatFromStoredValue(1))
    }

    @Test
    fun `rowsPerRepeatFromStoredValue uses stored value when greater than one`() {
        assertEquals(12, RowAndRepeatProjectValues.rowsPerRepeatFromStoredValue(12))
    }

    @Test
    fun `repeatGoalFromStoredValue uses default when total rows is zero`() {
        assertEquals(20, RowAndRepeatProjectValues.repeatGoalFromStoredValue(0))
    }

    @Test
    fun `repeatGoalFromStoredValue uses stored value when greater than zero`() {
        assertEquals(30, RowAndRepeatProjectValues.repeatGoalFromStoredValue(30))
    }

    @Test
    fun `rowsPerRepeatInputFromStoredValue returns string value`() {
        assertEquals("12", RowAndRepeatProjectValues.rowsPerRepeatInputFromStoredValue(12))
    }

    @Test
    fun `repeatGoalInputFromStoredValue returns empty string when total rows is zero`() {
        assertEquals("", RowAndRepeatProjectValues.repeatGoalInputFromStoredValue(0))
    }
}
