package dev.harrisonsoftware.stitchCounter.domain.validation

import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectValidatorTest {

    // ── isTitleValid ──

    @Test
    fun `isTitleValid returns true for non-blank title`() {
        assertTrue(ProjectValidator.isTitleValid("My Scarf"))
    }

    @Test
    fun `isTitleValid returns true for title with leading and trailing whitespace`() {
        assertTrue(ProjectValidator.isTitleValid("  My Scarf  "))
    }

    @Test
    fun `isTitleValid returns false for empty string`() {
        assertFalse(ProjectValidator.isTitleValid(""))
    }

    @Test
    fun `isTitleValid returns false for whitespace-only string`() {
        assertFalse(ProjectValidator.isTitleValid("   "))
    }

    // ── areTotalRowsValidForType ──

    @Test
    fun `areTotalRowsValidForType returns true for SINGLE with zero rows`() {
        assertTrue(ProjectValidator.areTotalRowsValidForType(0, ProjectType.SINGLE))
    }

    @Test
    fun `areTotalRowsValidForType returns true for SINGLE with positive rows`() {
        assertTrue(ProjectValidator.areTotalRowsValidForType(10, ProjectType.SINGLE))
    }

    @Test
    fun `areTotalRowsValidForType returns true for DOUBLE with positive rows`() {
        assertTrue(ProjectValidator.areTotalRowsValidForType(1, ProjectType.DOUBLE))
    }

    @Test
    fun `areTotalRowsValidForType returns false for DOUBLE with zero rows`() {
        assertFalse(ProjectValidator.areTotalRowsValidForType(0, ProjectType.DOUBLE))
    }

    @Test
    fun `areTotalRowsValidForType returns false for DOUBLE with negative rows`() {
        assertFalse(ProjectValidator.areTotalRowsValidForType(-1, ProjectType.DOUBLE))
    }

    @Test
    fun `areTotalRowsValidForType returns false for ROW_AND_REPEAT with zero rows`() {
        assertFalse(ProjectValidator.areTotalRowsValidForType(0, ProjectType.ROW_AND_REPEAT))
    }

    @Test
    fun `areTotalRowsValidForType returns true for ROW_AND_REPEAT with positive rows`() {
        assertTrue(ProjectValidator.areTotalRowsValidForType(20, ProjectType.ROW_AND_REPEAT))
    }

    @Test
    fun `areRowsPerRepeatValid returns true for positive values`() {
        assertTrue(ProjectValidator.areRowsPerRepeatValid(8))
    }

    @Test
    fun `areRowsPerRepeatValid returns false for zero or negative values`() {
        assertFalse(ProjectValidator.areRowsPerRepeatValid(0))
        assertFalse(ProjectValidator.areRowsPerRepeatValid(-1))
    }

    @Test
    fun `areTotalRowsValidForType returns true for UNKNOWN regardless of total rows`() {
        assertTrue(ProjectValidator.areTotalRowsValidForType(0, ProjectType.UNKNOWN))
        assertTrue(ProjectValidator.areTotalRowsValidForType(-1, ProjectType.UNKNOWN))
    }
}
