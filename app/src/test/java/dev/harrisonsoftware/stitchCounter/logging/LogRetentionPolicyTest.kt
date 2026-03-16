package dev.harrisonsoftware.stitchCounter.logging

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.time.LocalDate

class LogRetentionPolicyTest {

    @Test
    fun `apply deletes files older than five days`() {
        val tempDirectory = Files.createTempDirectory("log_retention_test").toFile()
        val today = LocalDate.of(2026, 3, 13)
        val retainedFile = File(tempDirectory, "app-log-2026-03-09.log").apply { writeText("retain") }
        val staleFile = File(tempDirectory, "app-log-2026-03-07.log").apply { writeText("delete") }

        LogRetentionPolicy().apply(logDirectory = tempDirectory, currentDate = today)

        assertTrue(retainedFile.exists())
        assertFalse(staleFile.exists())
    }

    @Test
    fun `apply deletes files with invalid date name`() {
        val tempDirectory = Files.createTempDirectory("log_retention_invalid_name").toFile()
        val invalidFile = File(tempDirectory, "app-log-not-a-date.log").apply { writeText("invalid") }

        LogRetentionPolicy().apply(logDirectory = tempDirectory, currentDate = LocalDate.of(2026, 3, 13))

        assertFalse(invalidFile.exists())
    }
}
