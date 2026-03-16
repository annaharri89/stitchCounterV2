package dev.harrisonsoftware.stitchCounter.domain.usecase

import dev.harrisonsoftware.stitchCounter.data.backup.FileSystemProvider
import dev.harrisonsoftware.stitchCounter.data.repo.ProjectRepository
import dev.harrisonsoftware.stitchCounter.domain.mapper.toDomain
import dev.harrisonsoftware.stitchCounter.domain.mapper.toEntity
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.domain.validation.ProjectValidator
import dev.harrisonsoftware.stitchCounter.logging.AppLogger
import dev.harrisonsoftware.stitchCounter.logging.projectDataInfo
import dev.harrisonsoftware.stitchCounter.logging.projectDataWarn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveProjects @Inject constructor(
    private val repo: ProjectRepository
) {
    operator fun invoke(): Flow<List<Project>> = repo.observeProjects().map { list -> list.map { it.toDomain() } }
}

@Singleton
class GetProject @Inject constructor(
    private val repo: ProjectRepository
) {
    suspend operator fun invoke(id: Int): Project? = repo.getProject(id)?.toDomain()
}

@Singleton
class UpsertProject @Inject constructor(
    private val repo: ProjectRepository
) {
    suspend operator fun invoke(project: Project): UpsertProjectResult {
        if (!ProjectValidator.isTitleValid(project.title)) {
            return UpsertProjectResult.InvalidTitle
        }
        val trimmedTitle = project.title.trim()
        val projectId = repo.upsert(project.copy(title = trimmedTitle).toEntity())
        return UpsertProjectResult.Success(projectId)
    }
}

sealed interface UpsertProjectResult {
    data class Success(val projectId: Long) : UpsertProjectResult
    data object InvalidTitle : UpsertProjectResult
}

@Singleton
class DeleteProject @Inject constructor(
    private val repo: ProjectRepository,
    private val fileSystemProvider: FileSystemProvider,
    private val appLogger: AppLogger,
) {
    suspend operator fun invoke(project: Project) {
        appLogger.projectDataInfo("delete_single_start projectId=${project.id} title=${project.title}")
        deleteProjectImageFiles(project.imagePaths, fileSystemProvider, appLogger)
        repo.delete(project.toEntity())
        appLogger.projectDataInfo("delete_single_done projectId=${project.id}")
    }
}

@Singleton
class DeleteProjects @Inject constructor(
    private val repo: ProjectRepository,
    private val fileSystemProvider: FileSystemProvider,
    private val appLogger: AppLogger,
) {
    suspend operator fun invoke(projects: List<Project>) {
        if (projects.isNotEmpty()) {
            val projectIds = projects.joinToString(separator = ",") { it.id.toString() }
            appLogger.projectDataInfo("delete_bulk_start count=${projects.size} projectIds=[$projectIds]")
            val allImagePaths = projects.flatMap { it.imagePaths }
            deleteProjectImageFiles(allImagePaths, fileSystemProvider, appLogger)
            repo.deleteByIds(projects.map { it.id })
            appLogger.projectDataInfo("delete_bulk_done count=${projects.size}")
        }
    }
}

@Singleton
class UpdateSingleCounterValues @Inject constructor(
    private val repo: ProjectRepository
) {
    suspend operator fun invoke(
        id: Int,
        stitchCount: Int,
        stitchAdjustment: Int,
        totalStitchesEver: Int,
        clearCompletedAt: Boolean = false,
        updatedAt: Long
    ) = repo.updateSingleCounterValues(
        id = id,
        stitchCount = stitchCount,
        stitchAdjustment = stitchAdjustment,
        totalStitchesEver = totalStitchesEver,
        clearCompletedAt = clearCompletedAt,
        updatedAt = updatedAt
    )
}

@Singleton
class UpdateDoubleCounterValues @Inject constructor(
    private val repo: ProjectRepository
) {
    suspend operator fun invoke(
        id: Int,
        stitchCount: Int,
        stitchAdjustment: Int,
        rowCount: Int,
        rowAdjustment: Int,
        totalStitchesEver: Int,
        clearCompletedAt: Boolean = false,
        updatedAt: Long
    ) = repo.updateDoubleCounterValues(
        id = id,
        stitchCount = stitchCount,
        stitchAdjustment = stitchAdjustment,
        rowCount = rowCount,
        rowAdjustment = rowAdjustment,
        totalStitchesEver = totalStitchesEver,
        clearCompletedAt = clearCompletedAt,
        updatedAt = updatedAt
    )
}

@Singleton
class UpdateProjectDetailValues @Inject constructor(
    private val repo: ProjectRepository
) {
    suspend operator fun invoke(
        id: Int,
        title: String,
        notes: String,
        totalRows: Int,
        projectType: ProjectType,
        imagePaths: List<String>,
        completedAt: Long?,
        updatedAt: Long
    ): UpdateProjectDetailResult {
        if (!ProjectValidator.isTitleValid(title)) {
            return UpdateProjectDetailResult.InvalidTitle
        }
        if (!ProjectValidator.areTotalRowsValidForType(totalRows, projectType)) {
            return UpdateProjectDetailResult.InvalidTotalRows
        }
        repo.updateProjectDetailValues(id, title, notes, totalRows, imagePaths, completedAt, updatedAt)
        return UpdateProjectDetailResult.Success
    }
}

sealed interface UpdateProjectDetailResult {
    data object Success : UpdateProjectDetailResult
    data object InvalidTitle : UpdateProjectDetailResult
    data object InvalidTotalRows : UpdateProjectDetailResult
}

private fun deleteProjectImageFiles(
    imagePaths: List<String>,
    fileSystemProvider: FileSystemProvider,
    appLogger: AppLogger,
) {
    val filesDirectory = fileSystemProvider.getFilesDirectory()
    for (relativePath in imagePaths) {
        try {
            val imageFile = File(filesDirectory, relativePath)
            if (imageFile.exists()) {
                imageFile.delete()
            }
        } catch (e: Exception) {
            appLogger.projectDataWarn("delete_image_failed path=$relativePath", e)
        }
    }
}
