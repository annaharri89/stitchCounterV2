package dev.harrisonsoftware.stitchCounter.domain.usecase

import android.net.Uri
import dev.harrisonsoftware.stitchCounter.data.backup.BackupData
import dev.harrisonsoftware.stitchCounter.data.backup.BackupManager
import dev.harrisonsoftware.stitchCounter.data.backup.BackupMetadata
import dev.harrisonsoftware.stitchCounter.data.backup.BackupProject
import dev.harrisonsoftware.stitchCounter.data.repo.ProjectRepository
import dev.harrisonsoftware.stitchCounter.domain.mapper.toDomain
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportLibrary @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val backupManager: BackupManager,
    private val appVersion: String
) {
    suspend operator fun invoke(outputUri: Uri? = null): Result<Uri> {
        return try {
            val projects = projectRepository.observeProjects().first().map { it.toDomain() }
            
            val backupProjects = projects.map { project ->
                BackupProject(
                    id = project.id,
                    type = if (project.type.name == "DOUBLE") "double" else "single",
                    title = project.title,
                    stitchCounterNumber = project.stitchCounterNumber,
                    stitchAdjustment = project.stitchAdjustment,
                    rowCounterNumber = project.rowCounterNumber,
                    rowAdjustment = project.rowAdjustment,
                    totalRows = project.totalRows,
                    imagePaths = project.imagePaths
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
            
            backupManager.createBackupZip(backupData, outputUri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
