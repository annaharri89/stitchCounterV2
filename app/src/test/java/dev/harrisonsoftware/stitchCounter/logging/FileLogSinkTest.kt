package dev.harrisonsoftware.stitchCounter.logging

import dev.harrisonsoftware.stitchCounter.data.backup.FileSystemProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class FileLogSinkTest {

    @Test
    fun `file sink persists info warn and error but skips debug`() = runTest {
        val filesDirectory = Files.createTempDirectory("file_log_sink_files").toFile()
        val cacheDirectory = Files.createTempDirectory("file_log_sink_cache").toFile()
        val fileSystemProvider = TestFileSystemProvider(filesDirectory, cacheDirectory)
        val fileLogSink = FileLogSink(fileSystemProvider, LogRetentionPolicy())
        val appLogger = AppLoggerImpl(setOf(fileLogSink))

        appLogger.debug("TestTag", "debug_entry_should_not_persist")
        appLogger.info("TestTag", "info_entry_should_persist")
        appLogger.warn("TestTag", "warn_entry_should_persist")
        appLogger.error("TestTag", "error_entry_should_persist")
        fileLogSink.flushAndWait()

        val persistedContent = fileLogSink.resolveLogDirectory()
            .listFiles()
            ?.filter { it.isFile && it.extension == "log" }
            .orEmpty()
            .joinToString("\n") { it.readText() }

        assertFalse(persistedContent.contains("debug_entry_should_not_persist"))
        assertTrue(persistedContent.contains("info_entry_should_persist"))
        assertTrue(persistedContent.contains("warn_entry_should_persist"))
        assertTrue(persistedContent.contains("error_entry_should_persist"))
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
