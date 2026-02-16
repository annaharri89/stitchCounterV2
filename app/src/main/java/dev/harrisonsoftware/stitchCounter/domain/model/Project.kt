package dev.harrisonsoftware.stitchCounter.domain.model

import androidx.compose.runtime.Immutable

@Immutable
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
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val totalStitchesEver: Int = 0,
)

enum class ProjectType { SINGLE, DOUBLE }

