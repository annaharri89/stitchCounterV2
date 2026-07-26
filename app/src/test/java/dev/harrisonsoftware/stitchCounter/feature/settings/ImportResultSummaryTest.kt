package dev.harrisonsoftware.stitchCounter.feature.settings

import dev.harrisonsoftware.stitchCounter.domain.usecase.ImportResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportResultSummaryTest {

    @Test
    fun `buildImportResultSections includes projects imported only when no note activity`() {
        val result = ImportResult(
            importedCount = 2,
            failedCount = 0,
            failedProjectNames = emptyList(),
        )

        val sections = buildImportResultSections(result)

        assertEquals(1, sections.size)
        assertEquals(ImportResultSectionType.PROJECTS_IMPORTED, sections[0].type)
        assertEquals(2, sections[0].count)
    }

    @Test
    fun `buildImportResultSections includes project failures with names`() {
        val result = ImportResult(
            importedCount = 1,
            failedCount = 1,
            failedProjectNames = listOf("Hat (ID: 2)"),
        )

        val sections = buildImportResultSections(result)

        assertEquals(2, sections.size)
        assertEquals(ImportResultSectionType.PROJECTS_FAILED, sections[1].type)
        assertEquals(listOf("Hat (ID: 2)"), sections[1].itemNames)
    }

    @Test
    fun `buildImportResultSections includes imported notes when count is greater than zero`() {
        val result = ImportResult(
            importedCount = 1,
            failedCount = 0,
            failedProjectNames = emptyList(),
            importedNotesCount = 3,
        )

        val sections = buildImportResultSections(result)

        assertTrue(sections.any { it.type == ImportResultSectionType.NOTES_IMPORTED && it.count == 3 })
    }

    @Test
    fun `buildImportResultSections includes failed notes with names`() {
        val result = ImportResult(
            importedCount = 1,
            failedCount = 0,
            failedProjectNames = emptyList(),
            importedNotesCount = 1,
            failedNotesCount = 1,
            failedNoteNames = listOf("Bad note (ID: 4)"),
        )

        val sections = buildImportResultSections(result)

        val failedNotesSection = sections.first { it.type == ImportResultSectionType.NOTES_FAILED }
        assertEquals(1, failedNotesSection.count)
        assertEquals(listOf("Bad note (ID: 4)"), failedNotesSection.itemNames)
    }

    @Test
    fun `buildImportResultSections omits note sections when no notes imported or failed`() {
        val result = ImportResult(
            importedCount = 1,
            failedCount = 0,
            failedProjectNames = emptyList(),
            importedNotesCount = 0,
            failedNotesCount = 0,
        )

        val sections = buildImportResultSections(result)

        assertTrue(sections.none { it.type == ImportResultSectionType.NOTES_IMPORTED })
        assertTrue(sections.none { it.type == ImportResultSectionType.NOTES_FAILED })
    }
}
