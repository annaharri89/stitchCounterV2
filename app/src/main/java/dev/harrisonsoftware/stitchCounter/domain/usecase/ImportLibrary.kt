package dev.harrisonsoftware.stitchCounter.domain.usecase

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
                
                try {
                    val importedProjects = mutableListOf<Project>()
                    val failedProjects = mutableListOf<String>()
                    
                    extraction.backupData.projects.forEach { backupProject ->
                        try {
                            val imagePaths = mutableListOf<String>()
                            
                            backupProject.imagePaths.forEachIndexed { index, relativePath ->
                                val imageFileName = "project_${backupProject.id}_${index}_${File(relativePath).name}"
                                val sourceImageFile = File(extraction.imagesDir, imageFileName)
                                
                                if (sourceImageFile.exists()) {
                                    val newImagePath = backupManager.copyImageToInternalStorage(
                                        sourceImageFile,
                                        backupProject.id,
                                        index
                                    )
                                    if (newImagePath != null) {
                                        imagePaths.add(newImagePath)
                                    }
                                } else {
                                    val fallbackImageFile = File(extraction.imagesDir, File(relativePath).name)
                                    if (fallbackImageFile.exists()) {
                                        val newImagePath = backupManager.copyImageToInternalStorage(
                                            fallbackImageFile,
                                            backupProject.id,
                                            index
                                        )
                                        if (newImagePath != null) {
                                            imagePaths.add(newImagePath)
                                        }
                                    }
                                }
                            }
                            
                            val project = Project(
                                id = if (replaceExisting) backupProject.id else 0,
                                type = if (backupProject.type == "double") ProjectType.DOUBLE else ProjectType.SINGLE,
                                title = backupProject.title,
                                stitchCounterNumber = backupProject.stitchCounterNumber,
                                stitchAdjustment = backupProject.stitchAdjustment,
                                rowCounterNumber = backupProject.rowCounterNumber,
                                rowAdjustment = backupProject.rowAdjustment,
                                totalRows = backupProject.totalRows,
                                imagePaths = imagePaths
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
