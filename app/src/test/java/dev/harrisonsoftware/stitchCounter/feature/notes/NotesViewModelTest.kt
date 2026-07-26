package dev.harrisonsoftware.stitchCounter.feature.notes

import dev.harrisonsoftware.stitchCounter.domain.model.Note
import dev.harrisonsoftware.stitchCounter.domain.usecase.DeleteNote
import dev.harrisonsoftware.stitchCounter.domain.usecase.ObserveNotes
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
class NotesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var observeNotes: ObserveNotes
    private lateinit var deleteNote: DeleteNote
    private lateinit var notesFlow: MutableStateFlow<List<Note>>

    private val sampleNotes = listOf(
        Note(id = 1, title = "First", body = "Body one", createdAt = 1L, updatedAt = 1L),
        Note(id = 2, title = "Second", body = "Body two", createdAt = 2L, updatedAt = 2L),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        observeNotes = mockk()
        deleteNote = mockk(relaxed = true)
        notesFlow = MutableStateFlow(sampleNotes)
        every { observeNotes() } returns notesFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = NotesViewModel(
        observeNotes = observeNotes,
        deleteNote = deleteNote,
    )

    @Test
    fun `initial uiState has isLoading true`() {
        every { observeNotes() } returns flowOf()
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `notes flow sets isLoading to false`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(testDispatcher) { viewModel.notes.collect {} }
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `notes flow emits notes from use case`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(testDispatcher) { viewModel.notes.collect {} }
        assertEqualsNotes(sampleNotes, viewModel.notes.value)
    }

    @Test
    fun `requestDelete shows confirmation for note`() {
        val viewModel = createViewModel()
        val note = sampleNotes[0]
        viewModel.requestDelete(note)

        val state = viewModel.uiState.value
        assertTrue(state.showDeleteConfirmation)
        assertEquals(note, state.noteToDelete)
    }

    @Test
    fun `confirmDelete calls deleteNote and clears state`() = runTest {
        coEvery { deleteNote(any()) } returns Unit
        val viewModel = createViewModel()
        val note = sampleNotes[0]
        viewModel.requestDelete(note)
        viewModel.confirmDelete()

        coVerify { deleteNote(note) }
        val state = viewModel.uiState.value
        assertFalse(state.showDeleteConfirmation)
        assertNull(state.noteToDelete)
    }

    @Test
    fun `cancelDelete hides confirmation and clears noteToDelete`() {
        val viewModel = createViewModel()
        viewModel.requestDelete(sampleNotes[0])
        viewModel.cancelDelete()

        val state = viewModel.uiState.value
        assertFalse(state.showDeleteConfirmation)
        assertNull(state.noteToDelete)
    }

    private fun assertEqualsNotes(expected: List<Note>, actual: List<Note>) {
        org.junit.Assert.assertEquals(expected.size, actual.size)
        org.junit.Assert.assertEquals(expected.first().title, actual.first().title)
    }
}
