package dev.harrisonsoftware.stitchCounter.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CounterStateTest {

    @Test
    fun `default state has count zero and adjustment ONE`() {
        val state = CounterState()
        assertEquals(0, state.count)
        assertEquals(AdjustmentAmount.ONE, state.adjustment)
        assertEquals(AdjustmentAmount.CUSTOM.defaultAmount, state.customAdjustmentAmount)
    }

    @Test
    fun `resolvedAdjustmentAmount returns ONE default when adjustment is ONE`() {
        val state = CounterState(adjustment = AdjustmentAmount.ONE)
        assertEquals(1, state.resolvedAdjustmentAmount)
    }

    @Test
    fun `resolvedAdjustmentAmount returns FIVE default when adjustment is FIVE`() {
        val state = CounterState(adjustment = AdjustmentAmount.FIVE)
        assertEquals(5, state.resolvedAdjustmentAmount)
    }

    @Test
    fun `resolvedAdjustmentAmount returns customAdjustmentAmount when adjustment is CUSTOM`() {
        val state = CounterState(adjustment = AdjustmentAmount.CUSTOM, customAdjustmentAmount = 7)
        assertEquals(7, state.resolvedAdjustmentAmount)
    }

    @Test
    fun `resolvedAdjustmentAmount coerces custom amount to at least 1`() {
        val state = CounterState(adjustment = AdjustmentAmount.CUSTOM, customAdjustmentAmount = 0)
        assertEquals(1, state.resolvedAdjustmentAmount)

        val negativeState = CounterState(adjustment = AdjustmentAmount.CUSTOM, customAdjustmentAmount = -5)
        assertEquals(1, negativeState.resolvedAdjustmentAmount)
    }

    @Test
    fun `increment adds resolvedAdjustmentAmount to count`() {
        val state = CounterState(count = 10, adjustment = AdjustmentAmount.ONE)
        val incremented = state.increment()
        assertEquals(11, incremented.count)
    }

    @Test
    fun `increment by FIVE adds 5`() {
        val state = CounterState(count = 3, adjustment = AdjustmentAmount.FIVE)
        assertEquals(8, state.increment().count)
    }

    @Test
    fun `increment by CUSTOM uses customAdjustmentAmount`() {
        val state = CounterState(count = 0, adjustment = AdjustmentAmount.CUSTOM, customAdjustmentAmount = 12)
        assertEquals(12, state.increment().count)
    }

    @Test
    fun `decrement subtracts resolvedAdjustmentAmount from count`() {
        val state = CounterState(count = 10, adjustment = AdjustmentAmount.ONE)
        assertEquals(9, state.decrement().count)
    }

    @Test
    fun `decrement floors at zero`() {
        val state = CounterState(count = 2, adjustment = AdjustmentAmount.FIVE)
        assertEquals(0, state.decrement().count)
    }

    @Test
    fun `decrement from zero stays at zero`() {
        val state = CounterState(count = 0, adjustment = AdjustmentAmount.ONE)
        assertEquals(0, state.decrement().count)
    }

    @Test
    fun `reset sets count to zero`() {
        val state = CounterState(count = 42, adjustment = AdjustmentAmount.FIVE)
        val resetState = state.reset()
        assertEquals(0, resetState.count)
        assertEquals(AdjustmentAmount.FIVE, resetState.adjustment)
    }

    @Test
    fun `increment preserves adjustment and customAdjustmentAmount`() {
        val state = CounterState(count = 5, adjustment = AdjustmentAmount.CUSTOM, customAdjustmentAmount = 3)
        val incremented = state.increment()
        assertEquals(AdjustmentAmount.CUSTOM, incremented.adjustment)
        assertEquals(3, incremented.customAdjustmentAmount)
    }
}
