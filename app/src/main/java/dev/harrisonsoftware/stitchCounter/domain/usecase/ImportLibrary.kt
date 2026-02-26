package dev.harrisonsoftware.stitchCounter.domain.usecase

import android.util.Log
import dev.harrisonsoftware.stitchCounter.data.backup.BackupManager
import dev.harrisonsoftware.stitchCounter.data.repo.ProjectRepository
import dev.harrisonsoftware.stitchCounter.domain.mapper.toEntity
import dev.harrisonsoftware.stitchCounter.domain.model.ContentUri
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val SUPPORTED_BACKUP_VERSION = 1
private const val IMPORT_LIBRARY_LOG_TAG = "ImportLibrary"

@Singleton
class ImportLibrary @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val backupManager: BackupManager
) {
    suspend operator fun invoke(inputContentUri: ContentUri, replaceExisting: Boolean = false): Result<ImportResult> {
        return withContext(Dispatchers.IO) {
            try {
                val extractionResult = backupManager.extractBackupZip(inputContentUri)
                val extraction = extractionResult.getOrElse {
                    return@withContext Result.failure(it)
                }

                if (extraction.backupData.metadata.version != SUPPORTED_BACKUP_VERSION) {
                    backupManager.cleanupTempDirectory(extraction.tempDir)
                    return@withContext Result.failure(
                        IllegalArgumentException("Unsupported backup version: ${extraction.backupData.metadata.version}")
                    )
                }
                
                try {
                    val importedProjects = mutableListOf<Project>()
                    val failedProjects = mutableListOf<String>()
                    
                    extraction.backupData.projects.forEach { backupProject ->
                        try {
                            val imagePaths = mutableListOf<String>()
                            
                            backupProject.imagePaths.forEach { relativePath ->
                                val sourceImageFile = File(extraction.imagesDir, relativePath)
                                if (sourceImageFile.exists()) {
                                    val newImagePath = backupManager.copyImageToInternalStorage(sourceImageFile)
                                    if (newImagePath != null) {
                                        imagePaths.add(newImagePath)
                                    }
                                } else {
                                    Log.w(
                                        IMPORT_LIBRARY_LOG_TAG,
                                        "Skipping missing image for project ${backupProject.id}: $relativePath"
                                    )
                                }
                            }
                            
                            val now = System.currentTimeMillis()
                            val project = Project(
                                id = if (replaceExisting) backupProject.id else 0,
                                type = if (backupProject.type == "double") ProjectType.DOUBLE else ProjectType.SINGLE,
                                title = backupProject.title,
                                notes = backupProject.notes,
                                stitchCounterNumber = backupProject.stitchCounterNumber,
                                stitchAdjustment = backupProject.stitchAdjustment,
                                rowCounterNumber = backupProject.rowCounterNumber,
                                rowAdjustment = backupProject.rowAdjustment,
                                totalRows = backupProject.totalRows,
                                imagePaths = imagePaths,
                                createdAt = if (backupProject.createdAt > 0L) backupProject.createdAt else now,
                                updatedAt = if (backupProject.updatedAt > 0L) backupProject.updatedAt else now,
                                completedAt = backupProject.completedAt,
                                totalStitchesEver = backupProject.totalStitchesEver,
                            )
                            
                            val newId = projectRepository.upsert(project.toEntity())
                            importedProjects.add(project.copy(id = newId.toInt()))
                        } catch (e: Exception) {
                            failedProjects.add("${backupProject.title} (ID: ${backupProject.id})")
                        }
                    }
                    
                    val result = ImportResult(
                        importedCount = importedProjects.size,
                        failedCount = failedProjects.size,
                        failedProjectNames = failedProjects
                    )
                    
                    Result.success(result)
                } finally {
                    backupManager.cleanupTempDirectory(extraction.tempDir)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

data class ImportResult(
    val importedCount: Int,
    val failedCount: Int,
    val failedProjectNames: List<String>
)
