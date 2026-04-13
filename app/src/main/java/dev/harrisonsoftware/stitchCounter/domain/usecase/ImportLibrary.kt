package dev.harrisonsoftware.stitchCounter.domain.usecase

import dev.harrisonsoftware.stitchCounter.data.backup.BackupManagerError
import dev.harrisonsoftware.stitchCounter.data.backup.BackupZipExtractionResult
import dev.harrisonsoftware.stitchCounter.data.backup.BackupManager
import dev.harrisonsoftware.stitchCounter.data.repo.ProjectRepository
import dev.harrisonsoftware.stitchCounter.domain.mapper.toEntity
import dev.harrisonsoftware.stitchCounter.domain.model.ContentUri
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.logging.projectDataError
import dev.harrisonsoftware.stitchCounter.logging.projectDataInfo
import dev.harrisonsoftware.stitchCounter.logging.projectDataWarn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val SUPPORTED_BACKUP_VERSION = 1

@Singleton
class ImportLibrary @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val backupManager: BackupManager,
) {
    suspend operator fun invoke(
        inputContentUri: ContentUri,
        replaceExisting: Boolean = false
    ): ImportLibraryResult {
        return withContext(Dispatchers.IO) {
            projectDataInfo("import_start replaceExisting=$replaceExisting inputUri=${inputContentUri.value}")
            try {
                val extractionResult = backupManager.extractBackupZip(inputContentUri)
                val extraction = when (extractionResult) {
                    is BackupZipExtractionResult.Success -> extractionResult.extraction
                    is BackupZipExtractionResult.Failure -> {
                        projectDataError("import_extract_failed replaceExisting=$replaceExisting error=${extractionResult.error}")
                        return@withContext ImportLibraryResult.Failure(
                            ImportLibraryError.BackupExtractionFailed(extractionResult.error)
                        )
                    }
                }

                if (extraction.backupData.metadata.version != SUPPORTED_BACKUP_VERSION) {
                    backupManager.cleanupTempDirectory(extraction.tempDir)
                    projectDataError("import_version_unsupported version=${extraction.backupData.metadata.version}")
                    return@withContext ImportLibraryResult.Failure(
                        ImportLibraryError.UnsupportedBackupVersion(extraction.backupData.metadata.version)
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
                                    projectDataWarn("import_missing_image projectId=${backupProject.id} path=$relativePath")
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
                            projectDataError(
                                message = "import_project_failed projectId=${backupProject.id} title=${backupProject.title}",
                                throwable = e
                            )
                        }
                    }
                    
                    val result = ImportResult(
                        importedCount = importedProjects.size,
                        failedCount = failedProjects.size,
                        failedProjectNames = failedProjects
                    )
                    projectDataInfo("import_done replaceExisting=$replaceExisting imported=${result.importedCount} failed=${result.failedCount}")
                    
                    ImportLibraryResult.Success(result)
                } finally {
                    backupManager.cleanupTempDirectory(extraction.tempDir)
                }
            } catch (e: Exception) {
                projectDataError("import_unexpected_error", e)
                ImportLibraryResult.Failure(ImportLibraryError.Unexpected(e))
            }
        }
    }
}

sealed interface ImportLibraryResult {
    data class Success(val result: ImportResult) : ImportLibraryResult
    data class Failure(val error: ImportLibraryError) : ImportLibraryResult
}

sealed interface ImportLibraryError {
    data class BackupExtractionFailed(val error: BackupManagerError) : ImportLibraryError
    data class UnsupportedBackupVersion(val version: Int) : ImportLibraryError
    data class Unexpected(val cause: Throwable) : ImportLibraryError
}

data class ImportResult(
    val importedCount: Int,
    val failedCount: Int,
    val failedProjectNames: List<String>
)
