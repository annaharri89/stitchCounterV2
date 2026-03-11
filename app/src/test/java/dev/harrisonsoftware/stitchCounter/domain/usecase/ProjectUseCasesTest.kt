package dev.harrisonsoftware.stitchCounter.domain.usecase

import dev.harrisonsoftware.stitchCounter.data.repo.ProjectRepository
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectUseCasesTest {

    private val repository: ProjectRepository = mockk(relaxed = true)

    @Test
    fun `upsert project returns invalid title when title is blank`() = runTest {
        val upsertProject = UpsertProject(repository)

        val result = upsertProject(Project(type = ProjectType.SINGLE, title = "   "))

        assertEquals(UpsertProjectResult.InvalidTitle, result)
        coVerify(exactly = 0) { repository.upsert(any()) }
    }

    @Test
    fun `upsert project trims title and returns success`() = runTest {
        coEvery { repository.upsert(any()) } returns 42L
        val upsertProject = UpsertProject(repository)

        val result = upsertProject(Project(type = ProjectType.SINGLE, title = "  Hat Project  "))

        assertEquals(UpsertProjectResult.Success(42L), result)
        coVerify(exactly = 1) { repository.upsert(match { it.title == "Hat Project" }) }
    }

    @Test
    fun `update project detail returns invalid title when title is blank`() = runTest {
        val updateProjectDetailValues = UpdateProjectDetailValues(repository)

        val result = updateProjectDetailValues(
            id = 1,
            title = " ",
            notes = "notes",
            totalRows = 10,
            projectType = ProjectType.DOUBLE,
            imagePaths = emptyList(),
            completedAt = null,
            updatedAt = 123L
        )

        assertEquals(UpdateProjectDetailResult.InvalidTitle, result)
        coVerify(exactly = 0) { repository.updateProjectDetailValues(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `update project detail returns invalid total rows for double counter project`() = runTest {
        val updateProjectDetailValues = UpdateProjectDetailValues(repository)

        val result = updateProjectDetailValues(
            id = 1,
            title = "Blanket",
            notes = "notes",
            totalRows = 0,
            projectType = ProjectType.DOUBLE,
            imagePaths = emptyList(),
            completedAt = null,
            updatedAt = 123L
        )

        assertEquals(UpdateProjectDetailResult.InvalidTotalRows, result)
        coVerify(exactly = 0) { repository.updateProjectDetailValues(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `update project detail returns success and updates repository for valid input`() = runTest {
        val updateProjectDetailValues = UpdateProjectDetailValues(repository)

        val result = updateProjectDetailValues(
            id = 1,
            title = "Sweater",
            notes = "notes",
            totalRows = 40,
            projectType = ProjectType.DOUBLE,
            imagePaths = listOf("image.jpg"),
            completedAt = null,
            updatedAt = 123L
        )

        assertEquals(UpdateProjectDetailResult.Success, result)
        coVerify(exactly = 1) {
            repository.updateProjectDetailValues(
                id = 1,
                title = "Sweater",
                notes = "notes",
                totalRows = 40,
                imagePaths = listOf("image.jpg"),
                completedAt = null,
                updatedAt = 123L
            )
        }
    }
}
