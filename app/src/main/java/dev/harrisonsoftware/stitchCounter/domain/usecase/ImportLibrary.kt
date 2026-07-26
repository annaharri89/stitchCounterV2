package dev.harrisonsoftware.stitchCounter.domain.usecase

import dev.harrisonsoftware.stitchCounter.data.backup.BACKUP_FORMAT_VERSION_1
import dev.harrisonsoftware.stitchCounter.data.backup.BACKUP_FORMAT_VERSION_2
import dev.harrisonsoftware.stitchCounter.data.backup.BackupManagerError
import dev.harrisonsoftware.stitchCounter.data.backup.BackupNote
import dev.harrisonsoftware.stitchCounter.data.backup.BackupZipExtractionResult
import dev.harrisonsoftware.stitchCounter.data.backup.BackupManager
import dev.harrisonsoftware.stitchCounter.data.repo.NoteRepository
import dev.harrisonsoftware.stitchCounter.data.repo.ProjectRepository
import dev.harrisonsoftware.stitchCounter.domain.mapper.toEntity
import dev.harrisonsoftware.stitchCounter.domain.model.ContentUri
import dev.harrisonsoftware.stitchCounter.domain.model.Note
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.mapper.toImportProjectType
import dev.harrisonsoftware.stitchCounter.logging.projectDataError
import dev.harrisonsoftware.stitchCounter.logging.projectDataInfo
import dev.harrisonsoftware.stitchCounter.logging.projectDataWarn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private val SUPPORTED_BACKUP_VERSIONS = setOf(BACKUP_FORMAT_VERSION_1, BACKUP_FORMAT_VERSION_2)

@Singleton
class ImportLibrary @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val noteRepository: NoteRepository,
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

                if (extraction.backupData.metadata.version !in SUPPORTED_BACKUP_VERSIONS) {
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
                                type = backupProject.type.toImportProjectType(),
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
                    
                    val noteImportResult = importNotes(
                        backupNotes = extraction.backupData.notes,
                        replaceExisting = replaceExisting,
                    )
                    
                    val result = ImportResult(
                        importedCount = importedProjects.size,
                        failedCount = failedProjects.size,
                        failedProjectNames = failedProjects,
                        importedNotesCount = noteImportResult.importedCount,
                        failedNotesCount = noteImportResult.failedCount,
                        failedNoteNames = noteImportResult.failedNames,
                    )
                    projectDataInfo(
                        "import_done replaceExisting=$replaceExisting " +
                            "imported=${result.importedCount} failed=${result.failedCount} " +
                            "notesImported=${result.importedNotesCount} notesFailed=${result.failedNotesCount}"
                    )
                    
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

    private suspend fun importNotes(
        backupNotes: List<BackupNote>,
        replaceExisting: Boolean,
    ): NoteImportResult {
        val importedCount = mutableListOf<Note>()
        val failedNames = mutableListOf<String>()

        backupNotes.forEach { backupNote ->
            try {
                val now = System.currentTimeMillis()
                val note = Note(
                    id = if (replaceExisting) backupNote.id else 0,
                    title = backupNote.title,
                    body = backupNote.body,
                    createdAt = if (backupNote.createdAt > 0L) backupNote.createdAt else now,
                    updatedAt = if (backupNote.updatedAt > 0L) backupNote.updatedAt else now,
                )
                val newId = noteRepository.upsert(note.toEntity())
                importedCount.add(note.copy(id = newId.toInt()))
            } catch (e: Exception) {
                failedNames.add("${backupNote.title} (ID: ${backupNote.id})")
                projectDataError(
                    message = "import_note_failed noteId=${backupNote.id} title=${backupNote.title}",
                    throwable = e
                )
            }
        }

        return NoteImportResult(
            importedCount = importedCount.size,
            failedCount = failedNames.size,
            failedNames = failedNames,
        )
    }
}

private data class NoteImportResult(
    val importedCount: Int,
    val failedCount: Int,
    val failedNames: List<String>,
)

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
    val failedProjectNames: List<String>,
    val importedNotesCount: Int = 0,
    val failedNotesCount: Int = 0,
    val failedNoteNames: List<String> = emptyList(),
)
