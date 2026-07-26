package dev.harrisonsoftware.stitchCounter.domain.validation

object NoteValidator {

    fun isTitleValid(title: String): Boolean = title.trim().isNotBlank()
}
