package dev.harrisonsoftware.stitchCounter.feature.notes

import app.cash.turbine.test
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.domain.model.DismissalResult
import dev.harrisonsoftware.stitchCounter.domain.model.Note
import dev.harrisonsoftware.stitchCounter.domain.usecase.CreateNote
import dev.harrisonsoftware.stitchCounter.domain.usecase.CreateNoteResult
import dev.harrisonsoftware.stitchCounter.domain.usecase.GetNote
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpdateNote
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpdateNoteResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateNoteViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var createNote: CreateNote
    private lateinit var getNote: GetNote
    private lateinit var updateNote: UpdateNote

    private val sampleNote = Note(
        id = 7,
        title = "Existing title",
        body = "Existing body",
        createdAt = 100L,
        updatedAt = 200L,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        createNote = mockk()
        getNote = mockk()
        updateNote = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = CreateNoteViewModel(
        createNote = createNote,
        getNote = getNote,
        updateNote = updateNote,
    )

    @Test
    fun `loadNote with null preserves create draft across relaunch`() = runTest {
        val viewModel = createViewModel()
        viewModel.updateTitle("Draft title")
        viewModel.updateBody("Draft body")

        viewModel.loadNote(null)

        val state = viewModel.uiState.value
        assertNull(state.noteId)
        assertEquals("Draft title", state.title)
        assertEquals("Draft body", state.body)
        assertFalse(state.isEditMode)
        assertFalse(state.isLoading)
        assertFalse(state.isSaved)
    }

    @Test
    fun `loadNote with null resets after edit mode`() = runTest {
        coEvery { getNote(7) } returns sampleNote
        val viewModel = createViewModel()
        viewModel.loadNote(7)
        viewModel.updateTitle("Edited title")

        viewModel.loadNote(null)

        val state = viewModel.uiState.value
        assertNull(state.noteId)
        assertEquals("", state.title)
        assertEquals("", state.body)
        assertFalse(state.isEditMode)
    }

    @Test
    fun `loadNote with same id preserves edit draft across relaunch`() = runTest {
        coEvery { getNote(7) } returns sampleNote
        val viewModel = createViewModel()
        viewModel.loadNote(7)
        viewModel.updateTitle("Edited title")
        viewModel.updateBody("Edited body")

        viewModel.loadNote(7)

        val state = viewModel.uiState.value
        assertEquals(7, state.noteId)
        assertEquals("Edited title", state.title)
        assertEquals("Edited body", state.body)
        coVerify(exactly = 1) { getNote(7) }
    }

    @Test
    fun `confirmDiscard clears create draft before allowing dismissal`() = runTest {
        val viewModel = createViewModel()
        viewModel.updateTitle("Draft title")
        viewModel.updateBody("Draft body")

        viewModel.dismissalResult.test {
            viewModel.confirmDiscard()
            assertEquals(DismissalResult.Allowed, awaitItem())
        }

        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertEquals("", state.body)
        assertNull(state.noteId)
    }

    @Test
    fun `loadNote with id pre-fills title and body`() = runTest {
        coEvery { getNote(7) } returns sampleNote
        val viewModel = createViewModel()

        viewModel.loadNote(7)

        val state = viewModel.uiState.value
        assertEquals(7, state.noteId)
        assertEquals("Existing title", state.title)
        assertEquals("Existing body", state.body)
        assertTrue(state.isEditMode)
        assertFalse(state.isLoading)
    }

    @Test
    fun `reopening create after successful save has empty fields`() = runTest {
        coEvery { createNote("Title", "Body", any()) } returns CreateNoteResult.Success(1L)
        val viewModel = createViewModel()
        viewModel.updateTitle("Title")
        viewModel.updateBody("Body")

        viewModel.dismissalResult.test {
            viewModel.saveNote()
            assertEquals(DismissalResult.Allowed, awaitItem())
        }

        viewModel.loadNote(null)

        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertEquals("", state.body)
        assertNull(state.noteId)
        assertFalse(state.isSaved)
    }

    @Test
    fun `editing note then reopening create does not leak edit content`() = runTest {
        coEvery { getNote(7) } returns sampleNote
        coEvery {
            updateNote(7, "Updated title", "Updated body", any())
        } returns UpdateNoteResult.Success
        val viewModel = createViewModel()

        viewModel.loadNote(7)
        viewModel.updateTitle("Updated title")
        viewModel.updateBody("Updated body")

        viewModel.dismissalResult.test {
            viewModel.saveNote()
            assertEquals(DismissalResult.Allowed, awaitItem())
        }

        viewModel.loadNote(null)

        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertEquals("", state.body)
        assertNull(state.noteId)
        assertFalse(state.isEditMode)
    }

    @Test
    fun `updateTitle clears title error`() = runTest {
        coEvery { createNote(any(), any(), any()) } returns CreateNoteResult.InvalidTitle
        val viewModel = createViewModel()
        viewModel.saveNote()
        assertEquals(R.string.error_title_required, viewModel.uiState.value.titleError)

        viewModel.updateTitle("Valid title")
        assertNull(viewModel.uiState.value.titleError)
    }

    @Test
    fun `saveNote with invalid title sets title error`() = runTest {
        coEvery { createNote("", "", any()) } returns CreateNoteResult.InvalidTitle
        val viewModel = createViewModel()

        viewModel.saveNote()

        assertEquals(R.string.error_title_required, viewModel.uiState.value.titleError)
        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `saveNote success marks saved and sends Allowed dismissal`() = runTest {
        coEvery { createNote("Title", "Body", any()) } returns CreateNoteResult.Success(1L)
        val viewModel = createViewModel()
        viewModel.updateTitle("Title")
        viewModel.updateBody("Body")

        viewModel.dismissalResult.test {
            viewModel.saveNote()
            assertTrue(viewModel.uiState.value.isSaved)
            assertEquals(DismissalResult.Allowed, awaitItem())
        }
    }

    @Test
    fun `saveNote in edit mode invokes updateNote`() = runTest {
        coEvery { getNote(7) } returns sampleNote
        coEvery { updateNote(7, "New title", "New body", any()) } returns UpdateNoteResult.Success
        val viewModel = createViewModel()
        viewModel.loadNote(7)
        viewModel.updateTitle("New title")
        viewModel.updateBody("New body")

        viewModel.dismissalResult.test {
            viewModel.saveNote()
            assertTrue(viewModel.uiState.value.isSaved)
            assertEquals(DismissalResult.Allowed, awaitItem())
        }

        coVerify { updateNote(7, "New title", "New body", any()) }
        coVerify(exactly = 0) { createNote(any(), any(), any()) }
    }

    @Test
    fun `attemptDismissal with draft content sends ShowDiscardDialog`() = runTest {
        val viewModel = createViewModel()
        viewModel.updateTitle("Draft")

        viewModel.dismissalResult.test {
            viewModel.attemptDismissal()
            assertEquals(DismissalResult.ShowDiscardDialog, awaitItem())
        }
    }

    @Test
    fun `attemptDismissal with empty draft sends Allowed`() = runTest {
        val viewModel = createViewModel()

        viewModel.dismissalResult.test {
            viewModel.attemptDismissal()
            assertEquals(DismissalResult.Allowed, awaitItem())
        }
    }

    @Test
    fun `attemptDismissal in edit mode with unchanged note sends Allowed`() = runTest {
        coEvery { getNote(7) } returns sampleNote
        val viewModel = createViewModel()
        viewModel.loadNote(7)

        viewModel.dismissalResult.test {
            viewModel.attemptDismissal()
            assertEquals(DismissalResult.Allowed, awaitItem())
        }
    }

    @Test
    fun `attemptDismissal in edit mode with changes sends ShowDiscardDialog`() = runTest {
        coEvery { getNote(7) } returns sampleNote
        val viewModel = createViewModel()
        viewModel.loadNote(7)
        viewModel.updateTitle("Changed title")

        viewModel.dismissalResult.test {
            viewModel.attemptDismissal()
            assertEquals(DismissalResult.ShowDiscardDialog, awaitItem())
        }
    }

    @Test
    fun `confirmDiscard sends Allowed dismissal`() = runTest {
        val viewModel = createViewModel()

        viewModel.dismissalResult.test {
            viewModel.confirmDiscard()
            assertEquals(DismissalResult.Allowed, awaitItem())
        }
    }

    @Test
    fun `loadNote with null after save resets create form`() = runTest {
        coEvery { createNote("Title", "Body", any()) } returns CreateNoteResult.Success(1L)
        val viewModel = createViewModel()
        viewModel.updateTitle("Title")
        viewModel.updateBody("Body")
        viewModel.saveNote()

        viewModel.loadNote(null)

        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertEquals("", state.body)
        assertNull(state.noteId)
        assertFalse(state.isSaved)
    }

    @Test
    fun `loadNote with id after save reloads note from repository`() = runTest {
        coEvery { getNote(7) } returns sampleNote
        coEvery { updateNote(7, "New title", "New body", any()) } returns UpdateNoteResult.Success
        val viewModel = createViewModel()
        viewModel.loadNote(7)
        viewModel.updateTitle("New title")
        viewModel.updateBody("New body")
        viewModel.saveNote()

        viewModel.loadNote(7)

        val state = viewModel.uiState.value
        assertEquals(7, state.noteId)
        assertEquals("Existing title", state.title)
        assertEquals("Existing body", state.body)
        assertFalse(state.isSaved)
        coVerify(exactly = 2) { getNote(7) }
    }

    @Test
    fun `updateBody updates body in uiState`() {
        val viewModel = createViewModel()

        viewModel.updateBody("Draft body")

        assertEquals("Draft body", viewModel.uiState.value.body)
    }

    @Test
    fun `saveNote does not invoke createNote when already saving`() = runTest {
        coEvery { createNote(any(), any(), any()) } coAnswers {
            kotlinx.coroutines.awaitCancellation()
        }
        val viewModel = createViewModel()
        viewModel.updateTitle("Title")

        viewModel.saveNote()
        viewModel.saveNote()

        coVerify(exactly = 1) { createNote(any(), any(), any()) }
    }

    @Test
    fun `saveNote does not invoke createNote when already saved`() = runTest {
        coEvery { createNote("Title", "Body", any()) } returns CreateNoteResult.Success(1L)
        val viewModel = createViewModel()
        viewModel.updateTitle("Title")
        viewModel.updateBody("Body")
        viewModel.saveNote()

        viewModel.saveNote()

        coVerify(exactly = 1) { createNote(any(), any(), any()) }
    }

    @Test
    fun `attemptDismissal when saved sends Allowed`() = runTest {
        coEvery { createNote("Title", "", any()) } returns CreateNoteResult.Success(1L)
        val viewModel = createViewModel()
        viewModel.updateTitle("Title")

        viewModel.dismissalResult.test {
            viewModel.saveNote()
            assertEquals(DismissalResult.Allowed, awaitItem())

            viewModel.attemptDismissal()
            assertEquals(DismissalResult.Allowed, awaitItem())
        }
    }

    @Test
    fun `attemptDismissal with body-only draft sends ShowDiscardDialog`() = runTest {
        val viewModel = createViewModel()
        viewModel.updateBody("Draft body only")

        viewModel.dismissalResult.test {
            viewModel.attemptDismissal()
            assertEquals(DismissalResult.ShowDiscardDialog, awaitItem())
        }
    }

    @Test
    fun `loadNote with missing note sets load error`() = runTest {
        coEvery { getNote(99) } returns null
        val viewModel = createViewModel()
        viewModel.updateTitle("Stale title")
        viewModel.updateBody("Stale body")

        viewModel.loadNote(99)

        val state = viewModel.uiState.value
        assertEquals(R.string.error_note_not_found, state.loadError)
        assertFalse(state.isLoading)
        assertNull(state.noteId)
    }

    @Test
    fun `attemptDismissal with load error sends Allowed`() = runTest {
        coEvery { getNote(99) } returns null
        val viewModel = createViewModel()
        viewModel.loadNote(99)

        viewModel.dismissalResult.test {
            viewModel.attemptDismissal()
            assertEquals(DismissalResult.Allowed, awaitItem())
        }
    }
}
