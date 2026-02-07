package dev.harrisonsoftware.stitchCounter.domain.model

sealed class DismissalResult {
    object Allowed : DismissalResult()
    object Blocked : DismissalResult()
    object ShowDiscardDialog : DismissalResult()
}
