package dev.harrisonsoftware.stitchCounter.domain.model

enum class AdjustmentAmount(val text: String, val adjustmentAmount: Int) {
    ONE(
        text = "+1",
        adjustmentAmount = 1
    ),
    FIVE(
        text = "+5",
        adjustmentAmount = 5
    ),
    TEN(
        text = "+10",
        adjustmentAmount = 10
    )
}