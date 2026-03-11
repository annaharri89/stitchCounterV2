package dev.harrisonsoftware.stitchCounter.domain.usecase

import dev.harrisonsoftware.stitchCounter.data.backup.BackupData
import dev.harrisonsoftware.stitchCounter.data.backup.BackupManagerError
import dev.harrisonsoftware.stitchCounter.data.backup.BackupManager
import dev.harrisonsoftware.stitchCounter.data.backup.BackupZipCreationResult
import dev.harrisonsoftware.stitchCounter.data.backup.BackupMetadata
import dev.harrisonsoftware.stitchCounter.data.backup.BackupProject
import dev.harrisonsoftware.stitchCounter.data.repo.ProjectRepository
import dev.harrisonsoftware.stitchCounter.domain.mapper.toDomain
import dev.harrisonsoftware.stitchCounter.domain.model.ContentUri
import dev.harrisonsoftware.stitchCounter.logging.projectDataError
import dev.harrisonsoftware.stitchCounter.logging.projectDataInfo
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportLibrary @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val backupManager: BackupManager,
    private val appVersion: String
) {
    suspend operator fun invoke(outputContentUri: ContentUri? = null): ExportLibraryResult {
        return try {
            projectDataInfo("export_start outputUri=${outputContentUri?.value ?: "app_storage"}")
            val projects = projectRepository.observeProjects().first().map { it.toDomain() }
            
            val backupProjects = projects.map { project ->
                BackupProject(
                    id = project.id,
                    type = if (project.type.name == "DOUBLE") "double" else "single",
                    title = project.title,
                    notes = project.notes,
                    stitchCounterNumber = project.stitchCounterNumber,
                    stitchAdjustment = project.stitchAdjustment,
                    rowCounterNumber = project.rowCounterNumber,
                    rowAdjustment = project.rowAdjustment,
                    totalRows = project.totalRows,
                    imagePaths = project.imagePaths,
                    createdAt = project.createdAt,
                    updatedAt = project.updatedAt,
                    completedAt = project.completedAt,
                    totalStitchesEver = project.totalStitchesEver,
                )
            }
            
            val metadata = BackupMetadata(
                version = 1,
                exportDate = System.currentTimeMillis(),
                appVersion = appVersion,
                projectCount = projects.size
            )
            
            val backupData = BackupData(
                metadata = metadata,
                projects = backupProjects
            )
            
            when (val backupResult = backupManager.createBackupZip(backupData, outputContentUri)) {
                is BackupZipCreationResult.Success -> {
                    projectDataInfo("export_done projectCount=${projects.size} outputUri=${backupResult.contentUri.value}")
                    ExportLibraryResult.Success(backupResult.contentUri)
                }
                is BackupZipCreationResult.Failure -> ExportLibraryResult.Failure(
                    ExportLibraryError.BackupCreationFailed(backupResult.error)
                )
            }
        } catch (e: Exception) {
            projectDataError("export_unexpected_error", e)
            ExportLibraryResult.Failure(ExportLibraryError.Unexpected(e))
        }
    }
}

sealed interface ExportLibraryResult {
    data class Success(val contentUri: ContentUri) : ExportLibraryResult
    data class Failure(val error: ExportLibraryError) : ExportLibraryResult
}

sealed interface ExportLibraryError {
    data class BackupCreationFailed(val error: BackupManagerError) : ExportLibraryError
    data class Unexpected(val cause: Throwable) : ExportLibraryError
}
