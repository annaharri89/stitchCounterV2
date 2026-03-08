package dev.harrisonsoftware.stitchCounter.domain.usecase

import android.util.Log
import dev.harrisonsoftware.stitchCounter.data.backup.FileSystemProvider
import dev.harrisonsoftware.stitchCounter.data.repo.ProjectRepository
import dev.harrisonsoftware.stitchCounter.domain.mapper.toDomain
import dev.harrisonsoftware.stitchCounter.domain.mapper.toEntity
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.domain.validation.ProjectValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val LOG_TAG = "ProjectUseCases"

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
    suspend operator fun invoke(project: Project): Long {
        require(ProjectValidator.isTitleValid(project.title)) { "Project title must not be blank" }
        val trimmedTitle = project.title.trim()
        return repo.upsert(project.copy(title = trimmedTitle).toEntity())
    }
}

@Singleton
class DeleteProject @Inject constructor(
    private val repo: ProjectRepository,
    private val fileSystemProvider: FileSystemProvider
) {
    suspend operator fun invoke(project: Project) {
        deleteProjectImageFiles(project.imagePaths, fileSystemProvider)
        repo.delete(project.toEntity())
    }
}

@Singleton
class DeleteProjects @Inject constructor(
    private val repo: ProjectRepository,
    private val fileSystemProvider: FileSystemProvider
) {
    suspend operator fun invoke(projects: List<Project>) {
        if (projects.isNotEmpty()) {
            val allImagePaths = projects.flatMap { it.imagePaths }
            deleteProjectImageFiles(allImagePaths, fileSystemProvider)
            repo.deleteByIds(projects.map { it.id })
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
    ) {
        require(ProjectValidator.areTotalRowsValidForType(totalRows, projectType)) {
            "Double-counter projects require totalRows > 0"
        }
        repo.updateProjectDetailValues(id, title, notes, totalRows, imagePaths, completedAt, updatedAt)
    }
}

private fun deleteProjectImageFiles(
    imagePaths: List<String>,
    fileSystemProvider: FileSystemProvider
) {
    val filesDirectory = fileSystemProvider.getFilesDirectory()
    for (relativePath in imagePaths) {
        try {
            val imageFile = File(filesDirectory, relativePath)
            if (imageFile.exists()) {
                imageFile.delete()
            }
        } catch (e: Exception) {
            Log.w(LOG_TAG, "Failed to delete image file: $relativePath", e)
        }
    }
}
