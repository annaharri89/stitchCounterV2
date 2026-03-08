package dev.harrisonsoftware.stitchCounter.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AdjustmentAmountTest {

    @Test
    fun `fromPersistedAmount maps 1 to ONE and preserves previous custom amount`() {
        val (adjustment, customAmount) = AdjustmentAmount.fromPersistedAmount(
            amount = 1,
            previousCustomAdjustmentAmount = 42
        )
        assertEquals(AdjustmentAmount.ONE, adjustment)
        assertEquals(42, customAmount)
    }

    @Test
    fun `fromPersistedAmount maps 5 to FIVE and preserves previous custom amount`() {
        val (adjustment, customAmount) = AdjustmentAmount.fromPersistedAmount(
            amount = 5,
            previousCustomAdjustmentAmount = 99
        )
        assertEquals(AdjustmentAmount.FIVE, adjustment)
        assertEquals(99, customAmount)
    }

    @Test
    fun `fromPersistedAmount maps non-standard amount to CUSTOM with that amount`() {
        val (adjustment, customAmount) = AdjustmentAmount.fromPersistedAmount(
            amount = 7,
            previousCustomAdjustmentAmount = 10
        )
        assertEquals(AdjustmentAmount.CUSTOM, adjustment)
        assertEquals(7, customAmount)
    }

    @Test
    fun `fromPersistedAmount maps 10 to CUSTOM with amount 10`() {
        val (adjustment, customAmount) = AdjustmentAmount.fromPersistedAmount(amount = 10)
        assertEquals(AdjustmentAmount.CUSTOM, adjustment)
        assertEquals(10, customAmount)
    }

    @Test
    fun `fromPersistedAmount coerces zero to CUSTOM with amount 1`() {
        val (adjustment, customAmount) = AdjustmentAmount.fromPersistedAmount(amount = 0)
        assertEquals(AdjustmentAmount.CUSTOM, adjustment)
        assertEquals(1, customAmount)
    }

    @Test
    fun `fromPersistedAmount coerces negative to CUSTOM with amount 1`() {
        val (adjustment, customAmount) = AdjustmentAmount.fromPersistedAmount(amount = -3)
        assertEquals(AdjustmentAmount.CUSTOM, adjustment)
        assertEquals(1, customAmount)
    }

    @Test
    fun `fromPersistedAmount uses default previousCustomAdjustmentAmount when not provided`() {
        val (adjustment, customAmount) = AdjustmentAmount.fromPersistedAmount(amount = 1)
        assertEquals(AdjustmentAmount.ONE, adjustment)
        assertEquals(AdjustmentAmount.CUSTOM.defaultAmount, customAmount)
    }

    @Test
    fun `default amounts are correct`() {
        assertEquals(1, AdjustmentAmount.ONE.defaultAmount)
        assertEquals(5, AdjustmentAmount.FIVE.defaultAmount)
        assertEquals(10, AdjustmentAmount.CUSTOM.defaultAmount)
    }
}
