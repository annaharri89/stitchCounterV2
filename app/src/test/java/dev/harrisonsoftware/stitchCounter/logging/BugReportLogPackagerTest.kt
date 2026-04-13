package dev.harrisonsoftware.stitchCounter.logging

import dev.harrisonsoftware.stitchCounter.data.backup.FileSystemProvider
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.time.LocalDate
import java.util.zip.ZipFile
import timber.log.Timber

class BugReportLogPackagerTest {

    @Test
    fun `packageLogsAsHtmlZip creates html zip with metadata`() = runTest {
        val filesDirectory = Files.createTempDirectory("bug_report_files").toFile()
        val cacheDirectory = Files.createTempDirectory("bug_report_cache").toFile()
        val retentionCurrentDate = LocalDate.of(2026, 3, 13)
        val logFileName = "app-log-$retentionCurrentDate.log"
        val expectedHtmlFileName = "app-log-$retentionCurrentDate.html"

        val fileSystemProvider = FakeFileSystemProvider(filesDirectory, cacheDirectory)
        val fileLogTree = TimberFileLogTree(fileSystemProvider, LogRetentionPolicy())
        val logDirectory = fileLogTree.resolveLogDirectory()
        File(logDirectory, logFileName).writeText(
            "2026-03-13T10:00:00.000Z | INFO | SCProjectData | delete_bulk_start count=2"
        )
        val packager = BugReportLogPackager(
            fileLogTree = fileLogTree,
            fileSystemProvider = fileSystemProvider,
            deviceMetadataProvider = FakeDeviceMetadataProvider()
        )

        val result = packager.packageLogsAsHtmlZip(retentionCurrentDate = retentionCurrentDate)

        assertTrue(result is BugReportLogPackagerResult.Success)
        val zipFile = (result as BugReportLogPackagerResult.Success).zipFile
        assertTrue(zipFile.exists())

        ZipFile(zipFile).use { diagnosticsZip ->
            val entries = diagnosticsZip.entries().asSequence().toList()
            assertEquals(1, entries.size)
            assertEquals(expectedHtmlFileName, entries.first().name)
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
        val fileLogTree = TimberFileLogTree(fileSystemProvider, LogRetentionPolicy())
        Timber.plant(fileLogTree)
        val packager = BugReportLogPackager(
            fileLogTree = fileLogTree,
            fileSystemProvider = fileSystemProvider,
            deviceMetadataProvider = FakeDeviceMetadataProvider()
        )

        Timber.tag("SCProjectData").i("latest_log_before_packaging")

        val result = packager.packageLogsAsHtmlZip()

        assertTrue(result is BugReportLogPackagerResult.Success)
        val zipFile = (result as BugReportLogPackagerResult.Success).zipFile
        ZipFile(zipFile).use { diagnosticsZip ->
            val entry = diagnosticsZip.entries().asSequence().first()
            val htmlContent = diagnosticsZip.getInputStream(entry).bufferedReader().readText()
            assertTrue(htmlContent.contains("latest_log_before_packaging"))
        }
        Timber.uproot(fileLogTree)
    }

    @Test
    fun `packageLogsAsHtmlZip returns no logs when none exist`() = runTest {
        val filesDirectory = Files.createTempDirectory("bug_report_no_logs_files").toFile()
        val cacheDirectory = Files.createTempDirectory("bug_report_no_logs_cache").toFile()
        val fileSystemProvider = FakeFileSystemProvider(filesDirectory, cacheDirectory)
        val fileLogTree = TimberFileLogTree(fileSystemProvider, LogRetentionPolicy())
        val packager = BugReportLogPackager(
            fileLogTree = fileLogTree,
            fileSystemProvider = fileSystemProvider,
            deviceMetadataProvider = FakeDeviceMetadataProvider()
        )

        val result = packager.packageLogsAsHtmlZip()

        assertTrue(result is BugReportLogPackagerResult.NoLogsAvailable)
    }

    @Test
    fun `packageLogsAsHtmlZip includes final marker under concurrent logging`() = runTest {
        val filesDirectory = Files.createTempDirectory("bug_report_concurrent_log_files").toFile()
        val cacheDirectory = Files.createTempDirectory("bug_report_concurrent_log_cache").toFile()
        val fileSystemProvider = FakeFileSystemProvider(filesDirectory, cacheDirectory)
        val fileLogTree = TimberFileLogTree(fileSystemProvider, LogRetentionPolicy())
        Timber.plant(fileLogTree)
        val packager = BugReportLogPackager(
            fileLogTree = fileLogTree,
            fileSystemProvider = fileSystemProvider,
            deviceMetadataProvider = FakeDeviceMetadataProvider()
        )

        coroutineScope {
            repeat(4) { workerIndex ->
                launch {
                    repeat(75) { messageIndex ->
                        Timber.tag("SCProjectData").i("concurrent_log worker=$workerIndex message=$messageIndex")
                    }
                }
            }
        }
        Timber.tag("SCProjectData").i("packaging_final_marker")

        val result = packager.packageLogsAsHtmlZip()

        assertTrue(result is BugReportLogPackagerResult.Success)
        val zipFile = (result as BugReportLogPackagerResult.Success).zipFile
        ZipFile(zipFile).use { diagnosticsZip ->
            val htmlJoined = diagnosticsZip.entries().asSequence()
                .map { diagnosticsZip.getInputStream(it).bufferedReader().readText() }
                .joinToString(separator = "\n")
            assertTrue(htmlJoined.contains("packaging_final_marker"))
        }
        Timber.uproot(fileLogTree)
    }

    @Test
    fun `resolveAndroidVersion uses release before api 30`() {
        val androidVersion = resolveAndroidVersion(
            sdkInt = 29,
            release = "10",
            releaseOrCodename = "11"
        )

        assertEquals("10", androidVersion)
    }

    @Test
    fun `resolveAndroidVersion uses releaseOrCodename on api 30 plus`() {
        val androidVersion = resolveAndroidVersion(
            sdkInt = 30,
            release = "11",
            releaseOrCodename = "11"
        )

        assertEquals("11", androidVersion)
    }

    @Test
    fun `resolveAndroidVersion falls back to unknown when all values missing`() {
        val androidVersion = resolveAndroidVersion(
            sdkInt = 29,
            release = null,
            releaseOrCodename = null
        )

        assertEquals("unknown", androidVersion)
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
