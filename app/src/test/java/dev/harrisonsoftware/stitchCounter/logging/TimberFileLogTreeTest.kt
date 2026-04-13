package dev.harrisonsoftware.stitchCounter.logging

import dev.harrisonsoftware.stitchCounter.data.backup.FileSystemProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import timber.log.Timber

class TimberFileLogTreeTest {

    @Test
    fun `file sink persists info warn and error but skips debug`() = runTest {
        val filesDirectory = Files.createTempDirectory("file_log_sink_files").toFile()
        val cacheDirectory = Files.createTempDirectory("file_log_sink_cache").toFile()
        val fileSystemProvider = TestFileSystemProvider(filesDirectory, cacheDirectory)
        val fileLogTree = TimberFileLogTree(fileSystemProvider, LogRetentionPolicy())
        Timber.plant(fileLogTree)

        Timber.tag("TestTag").d("debug_entry_should_not_persist")
        Timber.tag("TestTag").i("info_entry_should_persist")
        Timber.tag("TestTag").w("warn_entry_should_persist")
        Timber.tag("TestTag").e("error_entry_should_persist")
        fileLogTree.flushAndWait()

        val persistedContent = fileLogTree.resolveLogDirectory()
            .listFiles()
            ?.filter { it.isFile && it.extension == "log" }
            .orEmpty()
            .joinToString("\n") { it.readText() }

        assertFalse(persistedContent.contains("debug_entry_should_not_persist"))
        assertTrue(persistedContent.contains("info_entry_should_persist"))
        assertTrue(persistedContent.contains("warn_entry_should_persist"))
        assertTrue(persistedContent.contains("error_entry_should_persist"))
        Timber.uproot(fileLogTree)
    }

    @Test
    fun `flushAndSyncForPackaging preserves latest entry`() = runTest {
        val filesDirectory = Files.createTempDirectory("file_log_sync_files").toFile()
        val cacheDirectory = Files.createTempDirectory("file_log_sync_cache").toFile()
        val fileSystemProvider = TestFileSystemProvider(filesDirectory, cacheDirectory)
        val fileLogTree = TimberFileLogTree(fileSystemProvider, LogRetentionPolicy())
        Timber.plant(fileLogTree)

        Timber.tag("TestTag").i("entry_before_sync_packaging")
        fileLogTree.flushAndSyncForPackaging()

        val persistedContent = fileLogTree.resolveLogDirectory()
            .listFiles()
            ?.filter { it.isFile && it.extension == "log" }
            .orEmpty()
            .joinToString("\n") { it.readText() }

        assertTrue(persistedContent.contains("entry_before_sync_packaging"))
        Timber.uproot(fileLogTree)
    }
}

private class TestFileSystemProvider(
    private val filesDirectory: File,
    private val cacheDirectory: File
) : FileSystemProvider {
    override fun getCacheDirectory(): File = cacheDirectory
    override fun getFilesDirectory(): File = filesDirectory
    override fun getExternalFilesDirectory(): File? = null
}
