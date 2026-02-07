package dev.harrisonsoftware.stitchCounter.feature.navigation

import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType

sealed class SheetScreen {
    data class SingleCounter(val projectId: Int? = null) : SheetScreen()
    data class DoubleCounter(val projectId: Int? = null) : SheetScreen()
    data class ProjectDetail(val projectId: Int? = null, val projectType: ProjectType) : SheetScreen()
}
