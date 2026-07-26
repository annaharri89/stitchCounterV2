package dev.harrisonsoftware.stitchCounter.data.repo

import dev.harrisonsoftware.stitchCounter.data.local.NoteDao
import dev.harrisonsoftware.stitchCounter.data.local.NoteEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class NoteRepositoryTest {

    private lateinit var noteDao: NoteDao
    private lateinit var repository: NoteRepository

    private val sampleEntity = NoteEntity(
        id = 1,
        title = "Test note",
        body = "Body text",
        createdAt = 1L,
        updatedAt = 2L,
    )

    @Before
    fun setUp() {
        noteDao = mockk(relaxed = true)
        repository = NoteRepository(noteDao)
    }

    @Test
    fun `observeNotes delegates to dao observeAll`() {
        val flow = flowOf(emptyList<NoteEntity>())
        every { noteDao.observeAll() } returns flow

        assertSame(flow, repository.observeNotes())
    }

    @Test
    fun `getNote delegates to dao getById`() = runTest {
        coEvery { noteDao.getById(7) } returns sampleEntity

        assertEquals(sampleEntity, repository.getNote(7))
        coVerify { noteDao.getById(7) }
    }

    @Test
    fun `getNote returns null when dao returns null`() = runTest {
        coEvery { noteDao.getById(0) } returns null

        assertNull(repository.getNote(0))
        coVerify { noteDao.getById(0) }
    }

    @Test
    fun `upsert delegates to dao`() = runTest {
        coEvery { noteDao.upsert(sampleEntity) } returns 1L

        assertEquals(1L, repository.upsert(sampleEntity))
        coVerify { noteDao.upsert(sampleEntity) }
    }

    @Test
    fun `delete delegates to dao`() = runTest {
        repository.delete(sampleEntity)
        coVerify { noteDao.delete(sampleEntity) }
    }
}
