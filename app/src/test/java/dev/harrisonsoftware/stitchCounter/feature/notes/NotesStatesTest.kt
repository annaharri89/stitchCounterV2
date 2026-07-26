package dev.harrisonsoftware.stitchCounter.feature.notes

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class NotesStatesTest {

    @Test
    fun `empty notes string resources resolve expected values`() {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        assertEquals("No notes yet", context.getString(emptyNotesTitleRes()))
        assertEquals("Tap + to create your first note", context.getString(emptyNotesMessageRes()))
    }

    @Test
    fun `loading indicator description resource resolves expected value`() {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        assertEquals("Loading", context.getString(notesLoadingIndicatorDescriptionRes()))
    }
}
