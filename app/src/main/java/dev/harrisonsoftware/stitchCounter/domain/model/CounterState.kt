package dev.harrisonsoftware.stitchCounter.domain.model

data class CounterState(
    val count: Int = 0,
    val adjustment: AdjustmentAmount = AdjustmentAmount.ONE
) {
    fun increment(): CounterState = copy(
        count = count + adjustment.adjustmentAmount
    )
    
    fun decrement(): CounterState = copy(
        count = (count - adjustment.adjustmentAmount).coerceAtLeast(0)
    )
    
    fun reset(): CounterState = copy(count = 0)
}