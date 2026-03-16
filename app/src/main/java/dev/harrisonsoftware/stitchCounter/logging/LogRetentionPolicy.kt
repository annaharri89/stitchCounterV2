package dev.harrisonsoftware.stitchCounter.logging

import java.io.File
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private val logFileNamePattern = Regex("""^app-log-(\d{4}-\d{2}-\d{2})\.log$""")

@Singleton
class LogRetentionPolicy @Inject constructor() {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val keepDays: Long = 5L
    private val maxTotalBytes: Long = 15L * 1024L * 1024L

    fun apply(logDirectory: File, currentDate: LocalDate = LocalDate.now(ZoneOffset.UTC)) {
        if (!logDirectory.exists() || !logDirectory.isDirectory) return
        val logFiles = logDirectory.listFiles()?.filter { it.isFile && it.name.endsWith(".log") }.orEmpty()
        if (logFiles.isEmpty()) return

        val oldestDateToKeep = currentDate.minusDays(keepDays - 1L)
        logFiles.forEach { file ->
            val logDate = parseDateFromFileName(file.name)
            if (logDate == null || logDate.isBefore(oldestDateToKeep)) {
                runCatching { file.delete() }
            }
        }

        trimToMaxSize(logDirectory)
    }

    private fun trimToMaxSize(logDirectory: File) {
        val sortedFiles = logDirectory.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".log") }
            ?.sortedBy { parseDateFromFileName(it.name) ?: LocalDate.MIN }
            .orEmpty()

        var totalSize = sortedFiles.sumOf { it.length() }
        if (totalSize <= maxTotalBytes) return

        for (file in sortedFiles) {
            if (totalSize <= maxTotalBytes) return
            val fileSize = file.length()
            val deleted = runCatching { file.delete() }.getOrDefault(false)
            if (deleted) {
                totalSize -= fileSize
            }
        }
    }

    private fun parseDateFromFileName(fileName: String): LocalDate? {
        val matchResult = logFileNamePattern.matchEntire(fileName) ?: return null
        return runCatching { LocalDate.parse(matchResult.groupValues[1], dateFormatter) }.getOrNull()
    }
}
