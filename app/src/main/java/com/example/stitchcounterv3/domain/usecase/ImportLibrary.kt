package com.example.stitchcounterv3.domain.usecase

import android.net.Uri
import com.example.stitchcounterv3.data.backup.BackupManager
import com.example.stitchcounterv3.data.repo.ProjectRepository
import com.example.stitchcounterv3.domain.mapper.toEntity
import com.example.stitchcounterv3.domain.model.Project
import com.example.stitchcounterv3.domain.model.ProjectType
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
    suspend operator fun invoke(inputUri: Uri, replaceExisting: Boolean = false): Result<ImportResult> {
        return withContext(Dispatchers.IO) {
            try {
                val extractionResult = backupManager.extractBackupZip(inputUri)
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
