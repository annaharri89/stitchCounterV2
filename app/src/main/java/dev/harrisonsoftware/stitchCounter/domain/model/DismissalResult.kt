package dev.harrisonsoftware.stitchCounter.domain.model

sealed class DismissalResult {
    object Allowed : DismissalResult()
    object ShowDiscardDialog : DismissalResult()
}
