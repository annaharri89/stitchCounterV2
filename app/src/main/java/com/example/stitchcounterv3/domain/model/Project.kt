package com.example.stitchcounterv3.domain.model

data class Project(
    val id: Int = 0,
    val type: ProjectType,
    val title: String = "",
    val stitchCounterNumber: Int = 0,
    val stitchAdjustment: Int = 1,
    val rowCounterNumber: Int = 0,
    val rowAdjustment: Int = 1,
    val totalRows: Int = 0,
    val imagePaths: List<String> = emptyList(),
)

enum class ProjectType { SINGLE, DOUBLE }

