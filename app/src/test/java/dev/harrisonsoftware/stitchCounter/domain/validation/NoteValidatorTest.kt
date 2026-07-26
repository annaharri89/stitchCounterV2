package dev.harrisonsoftware.stitchCounter.domain.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteValidatorTest {

    @Test
    fun `isTitleValid returns true for non-blank title`() {
        assertTrue(NoteValidator.isTitleValid("My note"))
    }

    @Test
    fun `isTitleValid returns true for title with surrounding whitespace`() {
        assertTrue(NoteValidator.isTitleValid("  My note  "))
    }

    @Test
    fun `isTitleValid returns false for blank title`() {
        assertFalse(NoteValidator.isTitleValid(""))
    }

    @Test
    fun `isTitleValid returns false for whitespace-only title`() {
        assertFalse(NoteValidator.isTitleValid("   "))
    }
}
