package dev.harrisonsoftware.stitchCounter.data.repo

import dev.harrisonsoftware.stitchCounter.data.local.ProjectDao
import dev.harrisonsoftware.stitchCounter.data.local.ProjectEntity
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

class ProjectRepositoryTest {

    private lateinit var projectDao: ProjectDao
    private lateinit var repository: ProjectRepository

    private val sampleEntity = ProjectEntity(
        id = 1,
        type = "single",
        title = "Test",
        notes = "",
        stitchCounterNumber = 0,
        stitchAdjustment = 1,
        rowCounterNumber = 0,
        rowAdjustment = 1,
        totalRows = 0,
        imagePaths = emptyList(),
        createdAt = 1L,
        updatedAt = 2L,
        completedAt = null,
        totalStitchesEver = 0,
    )

    @Before
    fun setUp() {
        projectDao = mockk(relaxed = true)
        repository = ProjectRepository(projectDao)
    }

    @Test
    fun `observeProjects delegates to dao observeAll`() {
        val flow = flowOf(emptyList<ProjectEntity>())
        every { projectDao.observeAll() } returns flow

        assertSame(flow, repository.observeProjects())
    }

    @Test
    fun `getProject delegates to dao getById`() = runTest {
        coEvery { projectDao.getById(7) } returns sampleEntity

        assertEquals(sampleEntity, repository.getProject(7))
        coVerify { projectDao.getById(7) }
    }

    @Test
    fun `getProject returns null when dao returns null`() = runTest {
        coEvery { projectDao.getById(0) } returns null

        assertNull(repository.getProject(0))
        coVerify { projectDao.getById(0) }
    }

    @Test
    fun `upsert delegates to dao`() = runTest {
        coEvery { projectDao.upsert(sampleEntity) } returns 1L

        assertEquals(1L, repository.upsert(sampleEntity))
        coVerify { projectDao.upsert(sampleEntity) }
    }

    @Test
    fun `delete delegates to dao`() = runTest {
        repository.delete(sampleEntity)
        coVerify { projectDao.delete(sampleEntity) }
    }

    @Test
    fun `deleteByIds delegates to dao`() = runTest {
        val ids = listOf(1, 2, 3)
        repository.deleteByIds(ids)
        coVerify { projectDao.deleteByIds(ids) }
    }

    @Test
    fun `updateProjectDetailValues delegates to dao`() = runTest {
        repository.updateProjectDetailValues(
            id = 1,
            title = "t",
            notes = "n",
            totalRows = 5,
            imagePaths = listOf("a"),
            completedAt = null,
            updatedAt = 99L,
        )
        coVerify {
            projectDao.updateProjectDetailValues(
                id = 1,
                title = "t",
                notes = "n",
                totalRows = 5,
                imagePaths = listOf("a"),
                completedAt = null,
                updatedAt = 99L,
            )
        }
    }

    @Test
    fun `updateSingleCounterValues delegates to dao`() = runTest {
        repository.updateSingleCounterValues(
            id = 2,
            stitchCount = 10,
            stitchAdjustment = 1,
            totalStitchesEver = 100,
            clearCompletedAt = true,
            updatedAt = 3L,
        )
        coVerify {
            projectDao.updateSingleCounterValues(
                id = 2,
                stitchCount = 10,
                stitchAdjustment = 1,
                totalStitchesEver = 100,
                clearCompletedAt = true,
                updatedAt = 3L,
            )
        }
    }

    @Test
    fun `updateDoubleCounterValues delegates to dao`() = runTest {
        repository.updateDoubleCounterValues(
            id = 3,
            stitchCount = 1,
            stitchAdjustment = 1,
            rowCount = 2,
            rowAdjustment = 1,
            totalStitchesEver = 50,
            clearCompletedAt = false,
            updatedAt = 4L,
        )
        coVerify {
            projectDao.updateDoubleCounterValues(
                id = 3,
                stitchCount = 1,
                stitchAdjustment = 1,
                rowCount = 2,
                rowAdjustment = 1,
                totalStitchesEver = 50,
                clearCompletedAt = false,
                updatedAt = 4L,
            )
        }
    }
}
