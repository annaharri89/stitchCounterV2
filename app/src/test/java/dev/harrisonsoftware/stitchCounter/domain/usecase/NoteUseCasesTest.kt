package dev.harrisonsoftware.stitchCounter.domain.usecase

import dev.harrisonsoftware.stitchCounter.data.local.NoteEntity
import dev.harrisonsoftware.stitchCounter.domain.mapper.toDomain
import dev.harrisonsoftware.stitchCounter.data.repo.NoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NoteUseCasesTest {

    private lateinit var repository: NoteRepository
    private lateinit var observeNotes: ObserveNotes
    private lateinit var getNote: GetNote
    private lateinit var createNote: CreateNote
    private lateinit var updateNote: UpdateNote
    private lateinit var deleteNote: DeleteNote

    private val existingEntity = NoteEntity(
        id = 3,
        title = "Original title",
        body = "Original body",
        createdAt = 100L,
        updatedAt = 200L,
    )

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        observeNotes = ObserveNotes(repository)
        getNote = GetNote(repository)
        createNote = CreateNote(repository)
        updateNote = UpdateNote(repository)
        deleteNote = DeleteNote(repository)
    }

    @Test
    fun `ObserveNotes maps entities to domain notes`() = runTest {
        val entity = NoteEntity(
            id = 1,
            title = "Title",
            body = "Body",
            createdAt = 10L,
            updatedAt = 20L,
        )
        every { repository.observeNotes() } returns flowOf(listOf(entity))

        val notes = observeNotes().let { flow ->
            var result = emptyList<dev.harrisonsoftware.stitchCounter.domain.model.Note>()
            flow.collect { result = it }
            result
        }

        assertEquals(1, notes.size)
        assertEquals("Title", notes.first().title)
        assertEquals("Body", notes.first().body)
    }

    @Test
    fun `GetNote maps entity to domain note`() = runTest {
        coEvery { repository.getNote(3) } returns existingEntity

        val note = getNote(3)

        assertEquals("Original title", note?.title)
        assertEquals("Original body", note?.body)
        assertEquals(100L, note?.createdAt)
    }

    @Test
    fun `GetNote returns null when note is missing`() = runTest {
        coEvery { repository.getNote(0) } returns null

        assertNull(getNote(0))
    }

    @Test
    fun `CreateNote returns InvalidTitle for blank title`() = runTest {
        val result = createNote("", "body", timestampMillis = 100L)

        assertEquals(CreateNoteResult.InvalidTitle, result)
    }

    @Test
    fun `CreateNote returns InvalidTitle for whitespace-only title`() = runTest {
        val result = createNote("   ", "body", timestampMillis = 100L)

        assertEquals(CreateNoteResult.InvalidTitle, result)
        coVerify(exactly = 0) { repository.upsert(any()) }
    }

    @Test
    fun `ObserveNotes maps empty list`() = runTest {
        every { repository.observeNotes() } returns flowOf(emptyList())

        val notes = observeNotes().let { flow ->
            var result = emptyList<dev.harrisonsoftware.stitchCounter.domain.model.Note>()
            flow.collect { result = it }
            result
        }

        assertTrue(notes.isEmpty())
    }

    @Test
    fun `CreateNote trims title and persists note`() = runTest {
        val entitySlot = slot<NoteEntity>()
        coEvery { repository.upsert(capture(entitySlot)) } returns 5L

        val result = createNote("  My note  ", "Body text", timestampMillis = 500L)

        assertTrue(result is CreateNoteResult.Success)
        assertEquals(5L, (result as CreateNoteResult.Success).noteId)
        assertEquals("My note", entitySlot.captured.title)
        assertEquals("Body text", entitySlot.captured.body)
        assertEquals(500L, entitySlot.captured.createdAt)
        assertEquals(500L, entitySlot.captured.updatedAt)
        coVerify { repository.upsert(any()) }
    }

    @Test
    fun `UpdateNote returns InvalidTitle for blank title`() = runTest {
        val result = updateNote(
            noteId = 3,
            title = "   ",
            body = "body",
            timestampMillis = 300L,
        )

        assertEquals(UpdateNoteResult.InvalidTitle, result)
        coVerify(exactly = 0) { repository.upsert(any()) }
    }

    @Test
    fun `UpdateNote returns NotFound when note is missing`() = runTest {
        coEvery { repository.getNote(99) } returns null

        val result = updateNote(
            noteId = 99,
            title = "Title",
            body = "Body",
            timestampMillis = 300L,
        )

        assertEquals(UpdateNoteResult.NotFound, result)
        coVerify(exactly = 0) { repository.upsert(any()) }
    }

    @Test
    fun `UpdateNote trims title preserves createdAt and updates updatedAt`() = runTest {
        coEvery { repository.getNote(3) } returns existingEntity
        val entitySlot = slot<NoteEntity>()
        coEvery { repository.upsert(capture(entitySlot)) } returns 3L

        val result = updateNote(
            noteId = 3,
            title = "  Updated title  ",
            body = "Updated body",
            timestampMillis = 900L,
        )

        assertEquals(UpdateNoteResult.Success, result)
        assertEquals(3, entitySlot.captured.id)
        assertEquals("Updated title", entitySlot.captured.title)
        assertEquals("Updated body", entitySlot.captured.body)
        assertEquals(100L, entitySlot.captured.createdAt)
        assertEquals(900L, entitySlot.captured.updatedAt)
        coVerify { repository.upsert(any()) }
    }

    @Test
    fun `DeleteNote deletes note entity`() = runTest {
        val note = existingEntity.toDomain()

        deleteNote(note)

        coVerify { repository.delete(existingEntity) }
    }
}
