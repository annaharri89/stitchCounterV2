package dev.harrisonsoftware.stitchCounter.domain.model

enum class AdjustmentAmount(val defaultAmount: Int) {
    ONE(defaultAmount = 1),
    FIVE(defaultAmount = 5),
    CUSTOM(defaultAmount = 10);

    companion object {
        fun fromPersistedAmount(
            amount: Int,
            previousCustomAdjustmentAmount: Int = CUSTOM.defaultAmount
        ): Pair<AdjustmentAmount, Int> {
            return when (amount) {
                ONE.defaultAmount -> ONE to previousCustomAdjustmentAmount
                FIVE.defaultAmount -> FIVE to previousCustomAdjustmentAmount
                else -> CUSTOM to amount.coerceAtLeast(1)
            }
        }
    }
}