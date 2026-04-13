package dev.harrisonsoftware.stitchCounter.data.backup

import android.net.Uri
import dev.harrisonsoftware.stitchCounter.Constants
import dev.harrisonsoftware.stitchCounter.domain.model.ContentUri
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import timber.log.Timber

private const val BACKUP_JSON_FILE_NAME = "backup.json"
private const val BACKUP_IMAGES_DIRECTORY_NAME = "images"
private const val INTERNAL_PROJECT_IMAGES_DIRECTORY_NAME = "project_images"
private const val PROJECT_IMAGE_FILE_PREFIX = "project_"

class BackupManager(
    private val fileSystemProvider: FileSystemProvider,
    private val uriStreamProvider: UriStreamProvider,
) {
    private val json = Json { ignoreUnknownKeys = true }
    
    fun createBackupZip(
        backupData: BackupData,
        outputContentUri: ContentUri? = null
    ): BackupZipCreationResult {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val zipFileName = "stitch_counter_backup_$timestamp.zip"
            
            val tempDir = File(fileSystemProvider.getCacheDirectory(), "backup_temp_${System.currentTimeMillis()}")
            tempDir.mkdirs()
            
            val jsonFile = File(tempDir, BACKUP_JSON_FILE_NAME)
            val imagesDir = File(tempDir, BACKUP_IMAGES_DIRECTORY_NAME)
            imagesDir.mkdirs()
            
            jsonFile.writeText(json.encodeToString(BackupData.serializer(), backupData))
            
            backupData.projects.forEach { project ->
                project.imagePaths.forEach { relativeImagePath ->
                    val sourceFile = resolveSafeInternalFile(relativeImagePath)
                    if (sourceFile != null && sourceFile.exists() && sourceFile.isFile) {
                        val destFile = File(imagesDir, relativeImagePath)
                        destFile.parentFile?.mkdirs()
                        sourceFile.copyTo(destFile, overwrite = true)
                    } else {
                        Timber.tag(Constants.LOG_TAG_BACKUP_MANAGER).w("event=export_image_missing path=$relativeImagePath")
                    }
                }
            }
            
            val outputUri = outputContentUri?.let { Uri.parse(it.value) }
            val resultUri = if (outputUri != null) {
                uriStreamProvider.openOutputStream(outputUri)?.use { outputStream ->
                    createZipFromDirectory(tempDir, outputStream)
                    outputUri
                } ?: return BackupZipCreationResult.Failure(BackupManagerError.OutputStreamUnavailable)
            } else {
                val externalFilesDir = fileSystemProvider.getExternalFilesDirectory()
                    ?: return BackupZipCreationResult.Failure(BackupManagerError.ExternalFilesDirectoryUnavailable)
                val externalZipFile = File(externalFilesDir, zipFileName)
                FileOutputStream(externalZipFile).use { outputStream ->
                    createZipFromDirectory(tempDir, outputStream)
                }
                uriStreamProvider.uriFromFile(externalZipFile)
            }
            
            tempDir.deleteRecursively()
            
            BackupZipCreationResult.Success(ContentUri(resultUri.toString()))
        } catch (e: Exception) {
            Timber.tag(Constants.LOG_TAG_BACKUP_MANAGER).e(e, "event=create_backup_failed")
            BackupZipCreationResult.Failure(BackupManagerError.Unexpected(e))
        }
    }
    
    fun extractBackupZip(inputContentUri: ContentUri): BackupZipExtractionResult {
        return try {
            val inputUri = Uri.parse(inputContentUri.value)
            val tempDir = File(fileSystemProvider.getCacheDirectory(), "backup_extract_${System.currentTimeMillis()}")
            tempDir.mkdirs()
            
            uriStreamProvider.openInputStream(inputUri)?.use { inputStream ->
                val extractionError = extractZipToDirectory(inputStream, tempDir)
                if (extractionError != null) {
                    return BackupZipExtractionResult.Failure(extractionError)
                }
            } ?: return BackupZipExtractionResult.Failure(BackupManagerError.InputStreamUnavailable)
            
            val jsonFile = File(tempDir, BACKUP_JSON_FILE_NAME)
            if (!jsonFile.exists()) {
                return BackupZipExtractionResult.Failure(BackupManagerError.BackupJsonMissing)
            }
            
            val backupData = json.decodeFromString(BackupData.serializer(), jsonFile.readText())
            val imagesDir = File(tempDir, BACKUP_IMAGES_DIRECTORY_NAME)
            
            BackupZipExtractionResult.Success(BackupExtraction(backupData, imagesDir, tempDir))
        } catch (e: Exception) {
            Timber.tag(Constants.LOG_TAG_BACKUP_MANAGER).e(e, "event=extract_backup_failed")
            BackupZipExtractionResult.Failure(BackupManagerError.Unexpected(e))
        }
    }
    
    fun copyImageToInternalStorage(sourceFile: File): String? {
        return try {
            val imagesDir = File(fileSystemProvider.getFilesDirectory(), INTERNAL_PROJECT_IMAGES_DIRECTORY_NAME)
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            
            val extension = sourceFile.extension.ifBlank { "jpg" }
            val fileName = "${PROJECT_IMAGE_FILE_PREFIX}${System.currentTimeMillis()}_${UUID.randomUUID()}.$extension"
            val destFile = File(imagesDir, fileName)
            sourceFile.copyTo(destFile, overwrite = true)
            
            "$INTERNAL_PROJECT_IMAGES_DIRECTORY_NAME/$fileName"
        } catch (e: Exception) {
            Timber.tag(Constants.LOG_TAG_BACKUP_MANAGER).e(e, "event=copy_import_image_failed source=${sourceFile.name}")
            null
        }
    }
    
    fun cleanupTempDirectory(tempDir: File) {
        try {
            tempDir.deleteRecursively()
        } catch (e: Exception) {
            Timber.tag(Constants.LOG_TAG_BACKUP_MANAGER).w(e, "event=cleanup_temp_directory_failed tempDir=${tempDir.name}")
        }
    }
    
    private fun createZipFromDirectory(sourceDir: File, outputStream: java.io.OutputStream) {
        ZipOutputStream(outputStream).use { zipOut ->
            sourceDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val relativePath = file.relativeTo(sourceDir).path.replace("\\", "/")
                    zipOut.putNextEntry(ZipEntry(relativePath))
                    file.inputStream().use { it.copyTo(zipOut) }
                    zipOut.closeEntry()
                }
            }
        }
    }
    
    private fun extractZipToDirectory(inputStream: InputStream, destDir: File): BackupManagerError? {
        ZipInputStream(inputStream).use { zipIn ->
            var entry: ZipEntry? = zipIn.nextEntry
            while (entry != null) {
                val destination = resolveSecureZipEntryDestination(destDir, entry.name)
                if (destination is SecureZipDestination.Invalid) {
                    return destination.error
                }
                val file = (destination as SecureZipDestination.Valid).file
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    file.outputStream().use { zipIn.copyTo(it) }
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }
        return null
    }

    private fun resolveSecureZipEntryDestination(
        destinationDirectory: File,
        zipEntryName: String
    ): SecureZipDestination {
        val destinationFile = File(destinationDirectory, zipEntryName)
        val canonicalDestinationDirectoryPath = destinationDirectory.canonicalPath + File.separator
        val canonicalDestinationFilePath = destinationFile.canonicalPath
        if (!canonicalDestinationFilePath.startsWith(canonicalDestinationDirectoryPath)) {
            return SecureZipDestination.Invalid(BackupManagerError.UnsafeZipEntry(zipEntryName))
        }
        return SecureZipDestination.Valid(destinationFile)
    }

    private fun resolveSafeInternalFile(relativePath: String): File? {
        return try {
            val filesDirectory = fileSystemProvider.getFilesDirectory()
            val candidateFile = File(filesDirectory, relativePath)
            val canonicalFilesDirectoryPath = filesDirectory.canonicalPath + File.separator
            val canonicalCandidatePath = candidateFile.canonicalPath
            if (canonicalCandidatePath.startsWith(canonicalFilesDirectoryPath)) {
                candidateFile
            } else {
                Timber.tag(Constants.LOG_TAG_BACKUP_MANAGER).w("event=export_image_unsafe_path path=$relativePath")
                null
            }
        } catch (e: Exception) {
            Timber.tag(Constants.LOG_TAG_BACKUP_MANAGER).e(e, "event=resolve_export_image_failed path=$relativePath")
            null
        }
    }
}

sealed interface BackupZipCreationResult {
    data class Success(val contentUri: ContentUri) : BackupZipCreationResult
    data class Failure(val error: BackupManagerError) : BackupZipCreationResult
}

sealed interface BackupZipExtractionResult {
    data class Success(val extraction: BackupExtraction) : BackupZipExtractionResult
    data class Failure(val error: BackupManagerError) : BackupZipExtractionResult
}

sealed interface BackupManagerError {
    data object OutputStreamUnavailable : BackupManagerError
    data object ExternalFilesDirectoryUnavailable : BackupManagerError
    data object InputStreamUnavailable : BackupManagerError
    data object BackupJsonMissing : BackupManagerError
    data class UnsafeZipEntry(val zipEntryName: String) : BackupManagerError
    data class Unexpected(val cause: Throwable) : BackupManagerError
}

private sealed interface SecureZipDestination {
    data class Valid(val file: File) : SecureZipDestination
    data class Invalid(val error: BackupManagerError) : SecureZipDestination
}

data class BackupExtraction(
    val backupData: BackupData,
    val imagesDir: File,
    val tempDir: File
)
