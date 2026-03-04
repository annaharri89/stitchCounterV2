package dev.harrisonsoftware.stitchCounter.domain.validation

import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType

object ProjectValidator {

    fun isTitleValid(title: String): Boolean = title.trim().isNotBlank()

    fun areTotalRowsValidForType(totalRows: Int, projectType: ProjectType): Boolean =
        projectType != ProjectType.DOUBLE || totalRows > 0
}
