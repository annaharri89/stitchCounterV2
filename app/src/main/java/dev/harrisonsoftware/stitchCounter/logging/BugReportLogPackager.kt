package dev.harrisonsoftware.stitchCounter.logging

import android.os.Build
import dev.harrisonsoftware.stitchCounter.data.backup.FileSystemProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private val bugReportTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

data class DeviceMetadata(
    val appVersion: String,
    val androidVersion: String,
    val deviceModel: String,
)

interface DeviceMetadataProvider {
    fun getCurrentMetadata(): DeviceMetadata
}

@Singleton
class AndroidDeviceMetadataProvider @Inject constructor(
    private val appVersion: String,
) : DeviceMetadataProvider {
    override fun getCurrentMetadata(): DeviceMetadata {
        val androidVersion = resolveAndroidVersion(
            sdkInt = Build.VERSION.SDK_INT,
            release = Build.VERSION.RELEASE,
            releaseOrCodename = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Build.VERSION.RELEASE_OR_CODENAME
            } else {
                null
            }
        )
        val manufacturer = Build.MANUFACTURER ?: "unknown"
        val model = Build.MODEL ?: "unknown"
        return DeviceMetadata(
            appVersion = appVersion,
            androidVersion = androidVersion,
            deviceModel = "$manufacturer $model"
        )
    }
}

internal fun resolveAndroidVersion(
    sdkInt: Int,
    release: String?,
    releaseOrCodename: String?
): String {
    return if (sdkInt >= Build.VERSION_CODES.R) {
        releaseOrCodename ?: release ?: "unknown"
    } else {
        release ?: "unknown"
    }
}

sealed interface BugReportLogPackagerResult {
    data class Success(val zipFile: File) : BugReportLogPackagerResult
    data object NoLogsAvailable : BugReportLogPackagerResult
    data class Failure(val throwable: Throwable) : BugReportLogPackagerResult
}

@Singleton
class BugReportLogPackager @Inject constructor(
    private val fileLogSink: FileLogSink,
    private val fileSystemProvider: FileSystemProvider,
    private val deviceMetadataProvider: DeviceMetadataProvider,
) {
    suspend fun packageLogsAsHtmlZip(
        retentionCurrentDate: LocalDate = LocalDate.now(ZoneOffset.UTC)
    ): BugReportLogPackagerResult = withContext(Dispatchers.IO) {
        runCatching {
            fileLogSink.flushAndWait()
            fileLogSink.runRetention(currentDate = retentionCurrentDate)
            val logDirectory = fileLogSink.resolveLogDirectory()
            val logFiles = logDirectory.listFiles()
                ?.filter { it.isFile && it.extension == "log" }
                ?.sortedBy { it.name }
                .orEmpty()

            if (logFiles.isEmpty()) {
                return@withContext BugReportLogPackagerResult.NoLogsAvailable
            }

            val metadata = deviceMetadataProvider.getCurrentMetadata()
            val tempHtmlDirectory = File(fileSystemProvider.getCacheDirectory(), "bug_report_html_logs")
            tempHtmlDirectory.deleteRecursively()
            tempHtmlDirectory.mkdirs()

            val htmlFiles = logFiles.map { logFile ->
                convertLogFileToHtml(logFile, tempHtmlDirectory, metadata)
            }

            val timestamp = Instant.now().atOffset(ZoneOffset.UTC).format(bugReportTimeFormatter)
            val zipFile = File(fileSystemProvider.getCacheDirectory(), "stitch_diagnostics_$timestamp.zip")
            createZipFromFiles(htmlFiles, zipFile)
            zipFile
        }.fold(
            onSuccess = { zipFile -> BugReportLogPackagerResult.Success(zipFile) },
            onFailure = { throwable ->
                BugReportLogPackagerResult.Failure(throwable)
            }
        )
    }

    private fun convertLogFileToHtml(
        logFile: File,
        outputDirectory: File,
        metadata: DeviceMetadata
    ): File {
        val outputFileName = "${logFile.nameWithoutExtension}.html"
        val outputFile = File(outputDirectory, outputFileName)
        val escapedLogContent = runCatching { logFile.readText() }.getOrDefault("").escapeHtml()

        val html = buildString {
            append("<!doctype html><html><head><meta charset=\"utf-8\">")
            append("<title>${outputFileName.escapeHtml()}</title></head><body>")
            append("<h2>Stitch Counter Diagnostics</h2>")
            append("<p><strong>App version:</strong> ${metadata.appVersion.escapeHtml()}</p>")
            append("<p><strong>Android version:</strong> ${metadata.androidVersion.escapeHtml()}</p>")
            append("<p><strong>Device model:</strong> ${metadata.deviceModel.escapeHtml()}</p>")
            append("<hr/>")
            append("<pre>$escapedLogContent</pre>")
            append("</body></html>")
        }

        outputFile.writeText(html)
        return outputFile
    }

    private fun createZipFromFiles(files: List<File>, outputZipFile: File) {
        ZipOutputStream(FileOutputStream(outputZipFile)).use { zipOutputStream ->
            files.forEach { file ->
                val entry = ZipEntry(file.name)
                zipOutputStream.putNextEntry(entry)
                file.inputStream().use { inputStream ->
                    inputStream.copyTo(zipOutputStream)
                }
                zipOutputStream.closeEntry()
            }
        }
    }
}

private fun String.escapeHtml(): String {
    return this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
}
