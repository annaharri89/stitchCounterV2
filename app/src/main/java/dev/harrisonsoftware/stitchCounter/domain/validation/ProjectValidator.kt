package dev.harrisonsoftware.stitchCounter.domain.validation

import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType

object ProjectValidator {

    fun isTitleValid(title: String): Boolean = title.trim().isNotBlank()

    fun areTotalRowsValidForType(totalRows: Int, projectType: ProjectType): Boolean =
        when (projectType) {
            ProjectType.DOUBLE, ProjectType.ROW_AND_REPEAT -> totalRows > 0
            ProjectType.SINGLE, ProjectType.UNKNOWN -> true
        }

    fun areRowsPerRepeatValid(rowsPerRepeat: Int): Boolean = rowsPerRepeat > 0
}
