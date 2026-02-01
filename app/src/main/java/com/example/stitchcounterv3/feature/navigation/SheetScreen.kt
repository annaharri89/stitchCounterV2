package com.example.stitchcounterv3.feature.navigation

import com.example.stitchcounterv3.domain.model.ProjectType

sealed class SheetScreen {
    data class SingleCounter(val projectId: Int? = null) : SheetScreen()
    data class DoubleCounter(val projectId: Int? = null) : SheetScreen()
    data class ProjectDetail(val projectId: Int? = null, val projectType: ProjectType) : SheetScreen()
}
