package dev.harrisonsoftware.stitchCounter.domain.mapper

import dev.harrisonsoftware.stitchCounter.data.local.ProjectEntity
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.domain.mapper.toEntityTypeString
import dev.harrisonsoftware.stitchCounter.domain.mapper.toProjectType

fun ProjectEntity.toDomain(): Project = Project(
    id = id,
    type = type.toProjectType(),
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
    type = type.toEntityTypeString(),
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
