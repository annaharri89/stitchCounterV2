package dev.harrisonsoftware.stitchCounter.logging

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticLogFormatterTest {

    @Test
    fun `formatLogLine includes app version`() {
        val line = formatLogLine(
            timestamp = "2026-03-13T10:00:00.000Z",
            levelLabel = "INFO",
            tag = "SCProjectData",
            appVersion = "1.0.0.6",
            message = "delete_bulk_start count=2"
        )

        assertEquals(
            "2026-03-13T10:00:00.000Z | INFO | SCProjectData | app=1.0.0.6 | delete_bulk_start count=2",
            line
        )
    }

    @Test
    fun `addAppVersionToLine fills legacy line`() {
        val legacyLine = "2026-03-13T10:00:00.000Z | INFO | SCProjectData | delete_bulk_start count=2"

        val stampedLine = addAppVersionToLine(legacyLine, appVersion = "3.2.1")

        assertEquals(
            "2026-03-13T10:00:00.000Z | INFO | SCProjectData | app=3.2.1 | delete_bulk_start count=2",
            stampedLine
        )
    }

    @Test
    fun `addAppVersionToLine skips line that already has version`() {
        val lineWithVersion =
            "2026-03-13T10:00:00.000Z | INFO | SCProjectData | app=1.0.0.6 | delete_bulk_start count=2"

        val stampedLine = addAppVersionToLine(lineWithVersion, appVersion = "9.9.9")

        assertEquals(lineWithVersion, stampedLine)
    }

    @Test
    fun `addAppVersionToFile stamps each line`() {
        val logFileContent = """
            2026-03-13T10:00:00.000Z | INFO | SCProjectData | first_event
            2026-03-13T10:00:01.000Z | WARN | SCProjectData | app=1.0.0.6 | already_has_version

        """.trimIndent()

        val stampedContent = addAppVersionToFile(
            logFileContent = logFileContent,
            appVersion = "3.2.1"
        )

        assertTrue(stampedContent.contains("| app=3.2.1 | first_event"))
        assertTrue(stampedContent.contains("| app=1.0.0.6 | already_has_version"))
        assertFalse(stampedContent.contains("| app=3.2.1 | app=1.0.0.6"))
    }
}
