package dev.harrisonsoftware.stitchCounter.domain.mapper

import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectTypeMappersTest {

    @Test
    fun `parseProjectType maps row_and_repeat`() {
        assertEquals(
            ParsedProjectType.Known(ProjectType.ROW_AND_REPEAT),
            "row_and_repeat".parseProjectType()
        )
        assertEquals(
            ParsedProjectType.Known(ProjectType.ROW_AND_REPEAT),
            "ROW_AND_REPEAT".parseProjectType()
        )
    }

    @Test
    fun `parseProjectType maps double and single`() {
        assertEquals(ParsedProjectType.Known(ProjectType.DOUBLE), "double".parseProjectType())
        assertEquals(ParsedProjectType.Known(ProjectType.SINGLE), "single".parseProjectType())
    }

    @Test
    fun `parseProjectType returns unknown for unsupported values`() {
        val parsed = "triple".parseProjectType()
        assertTrue(parsed is ParsedProjectType.Unknown)
        assertEquals("triple", (parsed as ParsedProjectType.Unknown).rawValue)
    }

    @Test
    fun `toProjectType maps known values`() {
        assertEquals(ProjectType.SINGLE, "single".toProjectType())
        assertEquals(ProjectType.DOUBLE, "double".toProjectType())
        assertEquals(ProjectType.ROW_AND_REPEAT, "row_and_repeat".toProjectType())
    }

    @Test
    fun `toProjectType maps unknown values to UNKNOWN`() {
        assertEquals(ProjectType.UNKNOWN, "triple".toProjectType())
        assertEquals(ProjectType.UNKNOWN, "".toProjectType())
    }

    @Test
    fun `toEntityTypeString maps all project types`() {
        assertEquals("single", ProjectType.SINGLE.toEntityTypeString())
        assertEquals("double", ProjectType.DOUBLE.toEntityTypeString())
        assertEquals("row_and_repeat", ProjectType.ROW_AND_REPEAT.toEntityTypeString())
        assertEquals("unknown", ProjectType.UNKNOWN.toEntityTypeString())
    }

    @Test
    fun `toImportProjectType maps known backup types`() {
        assertEquals(ProjectType.SINGLE, "single".toImportProjectType())
        assertEquals(ProjectType.DOUBLE, "double".toImportProjectType())
        assertEquals(ProjectType.ROW_AND_REPEAT, "row_and_repeat".toImportProjectType())
    }

    @Test
    fun `toImportProjectType maps unknown export round trip to UNKNOWN`() {
        assertEquals(ProjectType.UNKNOWN, "unknown".toImportProjectType())
        assertEquals(ProjectType.UNKNOWN, "UNKNOWN".toImportProjectType())
    }

    @Test
    fun `toImportProjectType falls back unsupported values to SINGLE`() {
        assertEquals(ProjectType.SINGLE, "triple".toImportProjectType())
        assertEquals(ProjectType.SINGLE, "".toImportProjectType())
    }
}
