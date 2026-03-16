package dev.harrisonsoftware.stitchCounter.logging

import dev.harrisonsoftware.stitchCounter.data.backup.FileSystemProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipFile

class BugReportLogPackagerTest {

    @Test
    fun `packageLogsAsHtmlZip creates html zip with metadata`() = runTest {
        val filesDirectory = Files.createTempDirectory("bug_report_files").toFile()
        val cacheDirectory = Files.createTempDirectory("bug_report_cache").toFile()
        val logDirectory = File(filesDirectory, "logs").apply { mkdirs() }
        File(logDirectory, "app-log-2026-03-13.log").writeText(
            "2026-03-13T10:00:00.000Z | INFO | SCProjectData | delete_bulk_start count=2"
        )

        val fileSystemProvider = FakeFileSystemProvider(filesDirectory, cacheDirectory)
        val fileLogSink = FileLogSink(fileSystemProvider, LogRetentionPolicy())
        val packager = BugReportLogPackager(
            fileLogSink = fileLogSink,
            fileSystemProvider = fileSystemProvider,
            deviceMetadataProvider = FakeDeviceMetadataProvider()
        )

        val result = packager.packageLogsAsHtmlZip()

        assertTrue(result is BugReportLogPackagerResult.Success)
        val zipFile = (result as BugReportLogPackagerResult.Success).zipFile
        assertTrue(zipFile.exists())

        ZipFile(zipFile).use { diagnosticsZip ->
            val entries = diagnosticsZip.entries().asSequence().toList()
            assertEquals(1, entries.size)
            assertEquals("app-log-2026-03-13.html", entries.first().name)
            val htmlContent = diagnosticsZip.getInputStream(entries.first()).bufferedReader().readText()
            assertTrue(htmlContent.contains("App version:</strong> 3.2.1"))
            assertTrue(htmlContent.contains("Android version:</strong> 14"))
            assertTrue(htmlContent.contains("Device model:</strong> Pixel Test"))
            assertTrue(htmlContent.contains("SCProjectData"))
        }
    }

    @Test
    fun `packageLogsAsHtmlZip includes latest enqueued log entry`() = runTest {
        val filesDirectory = Files.createTempDirectory("bug_report_latest_log_files").toFile()
        val cacheDirectory = Files.createTempDirectory("bug_report_latest_log_cache").toFile()
        val fileSystemProvider = FakeFileSystemProvider(filesDirectory, cacheDirectory)
        val fileLogSink = FileLogSink(fileSystemProvider, LogRetentionPolicy())
        val appLogger = AppLoggerImpl(setOf(fileLogSink))
        val packager = BugReportLogPackager(
            fileLogSink = fileLogSink,
            fileSystemProvider = fileSystemProvider,
            deviceMetadataProvider = FakeDeviceMetadataProvider()
        )

        appLogger.info("SCProjectData", "latest_log_before_packaging")

        val result = packager.packageLogsAsHtmlZip()

        assertTrue(result is BugReportLogPackagerResult.Success)
        val zipFile = (result as BugReportLogPackagerResult.Success).zipFile
        ZipFile(zipFile).use { diagnosticsZip ->
            val entry = diagnosticsZip.entries().asSequence().first()
            val htmlContent = diagnosticsZip.getInputStream(entry).bufferedReader().readText()
            assertTrue(htmlContent.contains("latest_log_before_packaging"))
        }
    }

    @Test
    fun `packageLogsAsHtmlZip returns no logs when none exist`() = runTest {
        val filesDirectory = Files.createTempDirectory("bug_report_no_logs_files").toFile()
        val cacheDirectory = Files.createTempDirectory("bug_report_no_logs_cache").toFile()
        val fileSystemProvider = FakeFileSystemProvider(filesDirectory, cacheDirectory)
        val fileLogSink = FileLogSink(fileSystemProvider, LogRetentionPolicy())
        val packager = BugReportLogPackager(
            fileLogSink = fileLogSink,
            fileSystemProvider = fileSystemProvider,
            deviceMetadataProvider = FakeDeviceMetadataProvider()
        )

        val result = packager.packageLogsAsHtmlZip()

        assertTrue(result is BugReportLogPackagerResult.NoLogsAvailable)
    }
}

private class FakeFileSystemProvider(
    private val filesDirectory: File,
    private val cacheDirectory: File,
) : FileSystemProvider {
    override fun getCacheDirectory(): File = cacheDirectory
    override fun getFilesDirectory(): File = filesDirectory
    override fun getExternalFilesDirectory(): File? = null
}

private class FakeDeviceMetadataProvider : DeviceMetadataProvider {
    override fun getCurrentMetadata(): DeviceMetadata = DeviceMetadata(
        appVersion = "3.2.1",
        androidVersion = "14",
        deviceModel = "Pixel Test"
    )
}
