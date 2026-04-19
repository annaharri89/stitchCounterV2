package dev.harrisonsoftware.stitchCounter.data.backup

import android.net.Uri
import dev.harrisonsoftware.stitchCounter.domain.model.ContentUri
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BackupManagerTest {

    private lateinit var rootDir: File
    private lateinit var cacheDir: File
    private lateinit var filesDir: File
    private lateinit var externalFilesDir: File
    private lateinit var fakeFileSystemProvider: FakeFileSystemProvider
    private lateinit var fakeUriStreamProvider: FakeUriStreamProvider
    private lateinit var backupManager: BackupManager

    @Before
    fun setUp() {
        rootDir = Files.createTempDirectory("backup-manager-test").toFile()
        cacheDir = File(rootDir, "cache").apply { mkdirs() }
        filesDir = File(rootDir, "files").apply { mkdirs() }
        externalFilesDir = File(rootDir, "external").apply { mkdirs() }

        fakeFileSystemProvider = FakeFileSystemProvider(
            cacheDirectory = cacheDir,
            filesDirectory = filesDir,
            externalFilesDirectory = externalFilesDir
        )
        fakeUriStreamProvider = FakeUriStreamProvider()
        backupManager = BackupManager(fakeFileSystemProvider, fakeUriStreamProvider)
    }

    @After
    fun tearDown() {
        rootDir.deleteRecursively()
    }

    @Test
    fun `createBackupZip returns ExternalFilesDirectoryUnavailable when external directory is null`() {
        val backupManagerWithoutExternalDirectory = BackupManager(
            fileSystemProvider = FakeFileSystemProvider(
                cacheDirectory = cacheDir,
                filesDirectory = filesDir,
                externalFilesDirectory = null
            ),
            uriStreamProvider = fakeUriStreamProvider
        )
        val result = backupManagerWithoutExternalDirectory.createBackupZip(sampleBackupData())

        assertTrue(result is BackupZipCreationResult.Failure)
        result as BackupZipCreationResult.Failure
        assertEquals(BackupManagerError.ExternalFilesDirectoryUnavailable, result.error)
    }

    @Test
    fun `createBackupZip writes backup json and existing project image`() {
        val imageRelativePath = "project_images/sample.jpg"
        val sourceImage = File(filesDir, imageRelativePath).apply {
            parentFile?.mkdirs()
            writeBytes(byteArrayOf(1, 2, 3, 4))
        }
        assertTrue(sourceImage.exists())

        val backupData = sampleBackupData(imagePaths = listOf(imageRelativePath, "project_images/missing.jpg"))
        val result = backupManager.createBackupZip(backupData)

        assertTrue(result is BackupZipCreationResult.Success)
        val exportedZip = fakeUriStreamProvider.lastUriFromFileInput
        assertNotNull(exportedZip)
        val entryNames = unzipEntryNames(exportedZip!!.readBytes())
        assertTrue(entryNames.contains("backup.json"))
        assertTrue(entryNames.contains("images/$imageRelativePath"))
    }

    @Test
    fun `copyImageToInternalStorage stores image under project_images with same extension`() {
        val importSource = File(rootDir, "import-source.png").apply {
            writeBytes(byteArrayOf(9, 8, 7))
        }

        val relativePath = backupManager.copyImageToInternalStorage(importSource)

        assertNotNull(relativePath)
        assertTrue(relativePath!!.startsWith("project_images/project_"))
        assertTrue(relativePath.endsWith(".png"))
        assertTrue(File(filesDir, relativePath).exists())
    }

    @Test
    fun `copyImageToInternalStorage returns null when source file does not exist`() {
        val missingSource = File(rootDir, "does-not-exist.bin")
        assertFalse(missingSource.exists())

        assertNull(backupManager.copyImageToInternalStorage(missingSource))
    }

    @Test
    fun `cleanupTempDirectory removes directory contents`() {
        val tempDir = File(cacheDir, "cleanup_me").apply {
            mkdirs()
            File(this, "nested.txt").writeText("x")
        }
        assertTrue(tempDir.exists())

        backupManager.cleanupTempDirectory(tempDir)

        assertFalse(tempDir.exists())
    }

    @Test
    fun `extractBackupZip returns success with backup json and parsed data`() {
        val json = """{"metadata":{"version":1,"export_date":1,"app_version":"1","project_count":0},"projects":[]}"""
        val inputUri = "content://extract-ok"
        fakeUriStreamProvider.inputStreams[inputUri] = zipBytesForEntries(
            "backup.json" to json.toByteArray()
        )

        val result = backupManager.extractBackupZip(ContentUri(inputUri))

        assertTrue(result is BackupZipExtractionResult.Success)
        result as BackupZipExtractionResult.Success
        assertEquals(0, result.extraction.backupData.projects.size)
        assertEquals(1, result.extraction.backupData.metadata.version)
    }

    @Test
    fun `extractBackupZip returns InputStreamUnavailable when stream missing`() {
        val result = backupManager.extractBackupZip(ContentUri("content://no-stream"))

        assertTrue(result is BackupZipExtractionResult.Failure)
        result as BackupZipExtractionResult.Failure
        assertEquals(BackupManagerError.InputStreamUnavailable, result.error)
    }

    @Test
    fun `extractBackupZip returns BackupJsonMissing when json absent`() {
        val inputUri = "content://no-json"
        fakeUriStreamProvider.inputStreams[inputUri] = zipBytesForEntries(
            "readme.txt" to "hello".toByteArray()
        )

        val result = backupManager.extractBackupZip(ContentUri(inputUri))

        assertTrue(result is BackupZipExtractionResult.Failure)
        result as BackupZipExtractionResult.Failure
        assertEquals(BackupManagerError.BackupJsonMissing, result.error)
    }

    @Test
    fun `extractBackupZip returns failure for path traversal zip entry`() {
        val json = """{"metadata":{"version":1,"export_date":1,"app_version":"1","project_count":0},"projects":[]}"""
        val inputUri = "content://unsafe-zip"
        fakeUriStreamProvider.inputStreams[inputUri] = zipBytesForEntries(
            "../escape.txt" to "bad".toByteArray(),
            "backup.json" to json.toByteArray()
        )

        val result = backupManager.extractBackupZip(ContentUri(inputUri))

        assertTrue(result is BackupZipExtractionResult.Failure)
        result as BackupZipExtractionResult.Failure
        assertTrue(result.error is BackupManagerError.UnsafeZipEntry)
    }

    @Test
    fun `createBackupZip skips unsafe image paths without failing`() {
        val result = backupManager.createBackupZip(
            sampleBackupData(imagePaths = listOf("../../../outside.jpg"))
        )

        assertTrue(result is BackupZipCreationResult.Success)
    }

    private fun sampleBackupData(imagePaths: List<String> = emptyList()): BackupData {
        return BackupData(
            metadata = BackupMetadata(
                version = 1,
                exportDate = 123456789L,
                appVersion = "test",
                projectCount = 1
            ),
            projects = listOf(
                BackupProject(
                    id = 1,
                    type = "single",
                    title = "Project",
                    notes = "",
                    stitchCounterNumber = 1,
                    stitchAdjustment = 1,
                    rowCounterNumber = 0,
                    rowAdjustment = 1,
                    totalRows = 0,
                    imagePaths = imagePaths,
                    createdAt = 1L,
                    updatedAt = 2L,
                    completedAt = null,
                    totalStitchesEver = 1
                )
            )
        )
    }

    private fun unzipEntryNames(zipBytes: ByteArray): Set<String> {
        val names = mutableSetOf<String>()
        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zipInputStream ->
            var entry: ZipEntry? = zipInputStream.nextEntry
            while (entry != null) {
                names.add(entry.name)
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
        }
        return names
    }

    private fun zipBytesForEntries(vararg entries: Pair<String, ByteArray>): ByteArray {
        val outputStream = ByteArrayOutputStream()
        ZipOutputStream(outputStream).use { zipOutputStream ->
            entries.forEach { (entryName, entryBytes) ->
                zipOutputStream.putNextEntry(ZipEntry(entryName))
                zipOutputStream.write(entryBytes)
                zipOutputStream.closeEntry()
            }
        }
        return outputStream.toByteArray()
    }

    private class FakeFileSystemProvider(
        private val cacheDirectory: File,
        private val filesDirectory: File,
        private val externalFilesDirectory: File?,
    ) : FileSystemProvider {
        override fun getCacheDirectory(): File = cacheDirectory
        override fun getFilesDirectory(): File = filesDirectory
        override fun getExternalFilesDirectory(): File? = externalFilesDirectory
    }

    private class FakeUriStreamProvider : UriStreamProvider {
        val inputStreams = mutableMapOf<String, ByteArray>()
        val outputStreams = mutableMapOf<String, ByteArrayOutputStream>()
        var lastUriFromFileInput: File? = null

        override fun openInputStream(uri: Uri): InputStream? {
            val bytes = inputStreams[uri.toString()] ?: return null
            return ByteArrayInputStream(bytes)
        }

        override fun openOutputStream(uri: Uri): OutputStream? {
            return outputStreams[uri.toString()]
        }

        override fun uriFromFile(file: File): Uri {
            lastUriFromFileInput = file
            return mockk(relaxed = true)
        }
    }
}
