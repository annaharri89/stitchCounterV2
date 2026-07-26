package dev.harrisonsoftware.stitchCounter.domain.mapper

import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType

sealed interface ParsedProjectType {
    data class Known(val type: ProjectType) : ParsedProjectType
    data class Unknown(val rawValue: String) : ParsedProjectType
}

fun String.parseProjectType(): ParsedProjectType = when {
    equals("single", ignoreCase = true) -> ParsedProjectType.Known(ProjectType.SINGLE)
    equals("double", ignoreCase = true) -> ParsedProjectType.Known(ProjectType.DOUBLE)
    equals("row_and_repeat", ignoreCase = true) -> ParsedProjectType.Known(ProjectType.ROW_AND_REPEAT)
    else -> ParsedProjectType.Unknown(this)
}

fun String.toProjectType(): ProjectType = when (val parsed = parseProjectType()) {
    is ParsedProjectType.Known -> parsed.type
    is ParsedProjectType.Unknown -> ProjectType.UNKNOWN
}

fun ProjectType.toEntityTypeString(): String = when (this) {
    ProjectType.SINGLE -> "single"
    ProjectType.DOUBLE -> "double"
    ProjectType.ROW_AND_REPEAT -> "row_and_repeat"
    ProjectType.UNKNOWN -> "unknown"
}

fun String.toImportProjectType(): ProjectType = when (val parsed = parseProjectType()) {
    is ParsedProjectType.Known -> parsed.type
    is ParsedProjectType.Unknown -> if (parsed.rawValue.equals("unknown", ignoreCase = true)) {
        ProjectType.UNKNOWN
    } else {
        ProjectType.SINGLE
    }
}
