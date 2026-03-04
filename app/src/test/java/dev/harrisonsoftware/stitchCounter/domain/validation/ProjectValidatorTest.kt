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
}
