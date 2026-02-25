package dev.harrisonsoftware.stitchCounter.domain.mapper

import dev.harrisonsoftware.stitchCounter.data.local.ProjectEntity
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType

fun ProjectEntity.toDomain(): Project = Project(
    id = id,
    type = if (type.equals("double", ignoreCase = true)) ProjectType.DOUBLE else ProjectType.SINGLE,
    title = title,
    notes = notes,
    stitchCounterNumber = stitchCounterNumber,
    stitchAdjustment = stitchAdjustment,
    rowCounterNumber = rowCounterNumber,
    rowAdjustment = rowAdjustment,
    totalRows = totalRows,
    imagePaths = imagePaths,
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt,
    totalStitchesEver = totalStitchesEver,
)

fun Project.toEntity(): ProjectEntity = ProjectEntity(
    id = id,
    type = if (type == ProjectType.DOUBLE) "double" else "single",
    title = title,
    notes = notes,
    stitchCounterNumber = stitchCounterNumber,
    stitchAdjustment = stitchAdjustment,
    rowCounterNumber = rowCounterNumber,
    rowAdjustment = rowAdjustment,
    totalRows = totalRows,
    imagePaths = imagePaths,
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt,
    totalStitchesEver = totalStitchesEver,
)
