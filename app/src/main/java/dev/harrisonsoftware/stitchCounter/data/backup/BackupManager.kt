package dev.harrisonsoftware.stitchCounter.data.backup

import android.net.Uri
import android.util.Log
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

private const val BACKUP_JSON_FILE_NAME = "backup.json"
private const val BACKUP_IMAGES_DIRECTORY_NAME = "images"
private const val INTERNAL_PROJECT_IMAGES_DIRECTORY_NAME = "project_images"
private const val PROJECT_IMAGE_FILE_PREFIX = "project_"

class BackupManager(
    private val fileSystemProvider: FileSystemProvider,
    private val uriStreamProvider: UriStreamProvider
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val tag = "BackupManager"
    
    fun createBackupZip(backupData: BackupData, outputContentUri: ContentUri? = null): Result<ContentUri> {
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
                        Log.w(tag, "Skipping missing image during export: $relativeImagePath")
                    }
                }
            }
            
            val outputUri = outputContentUri?.let { Uri.parse(it.value) }
            val resultUri = if (outputUri != null) {
                uriStreamProvider.openOutputStream(outputUri)?.use { outputStream ->
                    createZipFromDirectory(tempDir, outputStream)
                    outputUri
                } ?: throw Exception("Failed to open output stream")
            } else {
                val externalFilesDir = fileSystemProvider.getExternalFilesDirectory()
                    ?: throw Exception("External files directory not available")
                val externalZipFile = File(externalFilesDir, zipFileName)
                FileOutputStream(externalZipFile).use { outputStream ->
                    createZipFromDirectory(tempDir, outputStream)
                }
                uriStreamProvider.uriFromFile(externalZipFile)
            }
            
            tempDir.deleteRecursively()
            
            Result.success(ContentUri(resultUri.toString()))
        } catch (e: Exception) {
            Log.e(tag, "Error creating backup", e)
            Result.failure(e)
        }
    }
    
    fun extractBackupZip(inputContentUri: ContentUri): Result<BackupExtraction> {
        return try {
            val inputUri = Uri.parse(inputContentUri.value)
            val tempDir = File(fileSystemProvider.getCacheDirectory(), "backup_extract_${System.currentTimeMillis()}")
            tempDir.mkdirs()
            
            uriStreamProvider.openInputStream(inputUri)?.use { inputStream ->
                extractZipToDirectory(inputStream, tempDir)
            } ?: throw Exception("Failed to open input stream")
            
            val jsonFile = File(tempDir, BACKUP_JSON_FILE_NAME)
            if (!jsonFile.exists()) {
                throw Exception("$BACKUP_JSON_FILE_NAME not found in archive")
            }
            
            val backupData = json.decodeFromString(BackupData.serializer(), jsonFile.readText())
            val imagesDir = File(tempDir, BACKUP_IMAGES_DIRECTORY_NAME)
            
            Result.success(BackupExtraction(backupData, imagesDir, tempDir))
        } catch (e: Exception) {
            Log.e(tag, "Error extracting backup", e)
            Result.failure(e)
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
            Log.e(tag, "Error copying image", e)
            null
        }
    }
    
    fun cleanupTempDirectory(tempDir: File) {
        try {
            tempDir.deleteRecursively()
        } catch (e: Exception) {
            Log.e(tag, "Error cleaning up temp directory", e)
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
    
    private fun extractZipToDirectory(inputStream: InputStream, destDir: File) {
        ZipInputStream(inputStream).use { zipIn ->
            var entry: ZipEntry? = zipIn.nextEntry
            while (entry != null) {
                val file = resolveSecureZipEntryDestination(destDir, entry.name)
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
    }

    private fun resolveSecureZipEntryDestination(destinationDirectory: File, zipEntryName: String): File {
        val destinationFile = File(destinationDirectory, zipEntryName)
        val canonicalDestinationDirectoryPath = destinationDirectory.canonicalPath + File.separator
        val canonicalDestinationFilePath = destinationFile.canonicalPath
        if (!canonicalDestinationFilePath.startsWith(canonicalDestinationDirectoryPath)) {
            throw IllegalArgumentException("Zip entry is outside destination directory: $zipEntryName")
        }
        return destinationFile
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
                Log.w(tag, "Skipping unsafe image path in export: $relativePath")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error resolving image path for export", e)
            null
        }
    }
}

data class BackupExtraction(
    val backupData: BackupData,
    val imagesDir: File,
    val tempDir: File
)
