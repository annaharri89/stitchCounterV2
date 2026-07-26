package dev.harrisonsoftware.stitchCounter.feature.settings

import dev.harrisonsoftware.stitchCounter.domain.usecase.ImportResult

internal enum class ImportResultSectionType {
    PROJECTS_IMPORTED,
    PROJECTS_FAILED,
    NOTES_IMPORTED,
    NOTES_FAILED,
}

internal data class ImportResultSection(
    val type: ImportResultSectionType,
    val count: Int,
    val itemNames: List<String> = emptyList(),
)

internal fun buildImportResultSections(result: ImportResult): List<ImportResultSection> {
    val sections = mutableListOf(
        ImportResultSection(
            type = ImportResultSectionType.PROJECTS_IMPORTED,
            count = result.importedCount,
        )
    )
    if (result.failedCount > 0) {
        sections.add(
            ImportResultSection(
                type = ImportResultSectionType.PROJECTS_FAILED,
                count = result.failedCount,
                itemNames = result.failedProjectNames,
            )
        )
    }
    if (result.importedNotesCount > 0) {
        sections.add(
            ImportResultSection(
                type = ImportResultSectionType.NOTES_IMPORTED,
                count = result.importedNotesCount,
            )
        )
    }
    if (result.failedNotesCount > 0) {
        sections.add(
            ImportResultSection(
                type = ImportResultSectionType.NOTES_FAILED,
                count = result.failedNotesCount,
                itemNames = result.failedNoteNames,
            )
        )
    }
    return sections
}
