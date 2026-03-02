package dev.harrisonsoftware.stitchCounter.domain.model

data class CounterState(
    val count: Int = 0,
    val adjustment: AdjustmentAmount = AdjustmentAmount.ONE,
    val customAdjustmentAmount: Int = AdjustmentAmount.CUSTOM.defaultAmount
) {
    val resolvedAdjustmentAmount: Int
        get() = if (adjustment == AdjustmentAmount.CUSTOM) {
            customAdjustmentAmount.coerceAtLeast(1)
        } else {
            adjustment.defaultAmount
        }

    fun increment(): CounterState = copy(
        count = count + resolvedAdjustmentAmount
    )
    
    fun decrement(): CounterState = copy(
        count = (count - resolvedAdjustmentAmount).coerceAtLeast(0)
    )
    
    fun reset(): CounterState = copy(count = 0)
}