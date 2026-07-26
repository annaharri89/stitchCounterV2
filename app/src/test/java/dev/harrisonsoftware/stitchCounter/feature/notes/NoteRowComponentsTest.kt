package dev.harrisonsoftware.stitchCounter.feature.notes

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteRowComponentsTest {

    @Test
    fun `noteBodyPreviewMaxLines returns two`() {
        assertEquals(2, noteBodyPreviewMaxLines())
    }

    @Test
    fun `NOTE_BODY_PREVIEW_MAX_LINES constant is two`() {
        assertEquals(2, NOTE_BODY_PREVIEW_MAX_LINES)
    }

    @Test
    fun `resolveNoteRowTapAction returns OpenNote when swipe is closed`() {
        assertEquals(NoteRowTapAction.OpenNote, resolveNoteRowTapAction(isSwipeRevealed = false))
    }

    @Test
    fun `resolveNoteRowTapAction returns ResetSwipeState when swipe is revealed`() {
        assertEquals(NoteRowTapAction.ResetSwipeState, resolveNoteRowTapAction(isSwipeRevealed = true))
    }
}
