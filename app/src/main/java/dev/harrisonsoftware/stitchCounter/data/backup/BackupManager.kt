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
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

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
            
            val jsonFile = File(tempDir, "backup.json")
            val imagesDir = File(tempDir, "images")
            imagesDir.mkdirs()
            
            jsonFile.writeText(json.encodeToString(BackupData.serializer(), backupData))
            
            backupData.projects.forEach { project ->
                project.imagePaths.forEachIndexed { index, imagePath ->
                    val sourceFile = File(imagePath)
                    if (sourceFile.exists()) {
                        val fileName = "project_${project.id}_${index}_${sourceFile.name}"
                        val destFile = File(imagesDir, fileName)
                        sourceFile.copyTo(destFile, overwrite = true)
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
            
            val jsonFile = File(tempDir, "backup.json")
            if (!jsonFile.exists()) {
                throw Exception("backup.json not found in archive")
            }
            
            val backupData = json.decodeFromString(BackupData.serializer(), jsonFile.readText())
            val imagesDir = File(tempDir, "images")
            
            Result.success(BackupExtraction(backupData, imagesDir, tempDir))
        } catch (e: Exception) {
            Log.e(tag, "Error extracting backup", e)
            Result.failure(e)
        }
    }
    
    fun copyImageToInternalStorage(sourceFile: File, projectId: Int, imageIndex: Int): String? {
        return try {
            val imagesDir = File(fileSystemProvider.getFilesDirectory(), "project_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            
            val fileName = "project_${projectId}_${System.currentTimeMillis()}_${imageIndex}.jpg"
            val destFile = File(imagesDir, fileName)
            sourceFile.copyTo(destFile, overwrite = true)
            
            destFile.absolutePath
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
                val file = File(destDir, entry.name)
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
}

data class BackupExtraction(
    val backupData: BackupData,
    val imagesDir: File,
    val tempDir: File
)
