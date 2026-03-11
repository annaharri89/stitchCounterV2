package dev.harrisonsoftware.stitchCounter.feature.projectDetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.domain.model.DismissalResult
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.domain.usecase.GetProject
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpdateProjectDetailResult
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpdateProjectDetailValues
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpsertProjectResult
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpsertProject
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var getProject: GetProject
    private lateinit var upsertProject: UpsertProject
    private lateinit var updateProjectDetailValues: UpdateProjectDetailValues

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        savedStateHandle = SavedStateHandle()
        getProject = mockk()
        upsertProject = mockk()
        updateProjectDetailValues = mockk()
        coEvery { upsertProject(any()) } returns UpsertProjectResult.Success(1L)
        coEvery {
            updateProjectDetailValues(
                id = any(),
                title = any(),
                notes = any(),
                totalRows = any(),
                projectType = any(),
                imagePaths = any(),
                completedAt = any(),
                updatedAt = any()
            )
        } returns UpdateProjectDetailResult.Success
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ProjectDetailViewModel(
        savedStateHandle = savedStateHandle,
        getProject = getProject,
        upsertProject = upsertProject,
        updateProjectDetailValues = updateProjectDetailValues,
    )

    private fun sampleProject(id: Int = 1) = Project(
        id = id,
        type = ProjectType.SINGLE,
        title = "Test Scarf",
        notes = "Some notes",
        stitchCounterNumber = 10,
        stitchAdjustment = 1,
        rowCounterNumber = 0,
        rowAdjustment = 1,
        totalRows = 0,
        totalStitchesEver = 50,
        createdAt = 1_000_000L,
        updatedAt = 1_000_000L,
    )

    @Test
    fun `loadProject with existing project populates state`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1, ProjectType.SINGLE)

        val state = viewModel.uiState.value
        assertEquals("Test Scarf", state.title)
        assertEquals("Some notes", state.notes)
        assertEquals(ProjectType.SINGLE, state.projectType)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadProject with null id creates new project state`() = runTest {
        val viewModel = createViewModel()
        viewModel.loadProject(null, ProjectType.DOUBLE)

        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertEquals(ProjectType.DOUBLE, state.projectType)
        assertNotNull(state.project)
        assertEquals(0, state.project!!.id)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadProject with zero id creates new project state`() = runTest {
        val viewModel = createViewModel()
        viewModel.loadProject(0, ProjectType.SINGLE)

        val state = viewModel.uiState.value
        assertEquals(0, state.project!!.id)
        assertFalse(state.isLoading)
    }

    @Test
    fun `updateTitle sets title and marks unsaved changes`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1, ProjectType.SINGLE)
        viewModel.updateTitle("New Title")

        val state = viewModel.uiState.value
        assertEquals("New Title", state.title)
        assertTrue(state.hasUnsavedChanges)
    }

    @Test
    fun `updateTitle with blank sets titleError`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1, ProjectType.SINGLE)
        viewModel.updateTitle("")

        assertEquals(R.string.error_title_required, viewModel.uiState.value.titleError)
    }

    @Test
    fun `updateTitle with non-blank clears titleError`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1, ProjectType.SINGLE)
        viewModel.updateTitle("")
        viewModel.updateTitle("Valid")

        assertNull(viewModel.uiState.value.titleError)
    }

    @Test
    fun `updateNotes marks unsaved changes`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1, ProjectType.SINGLE)
        viewModel.updateNotes("Updated notes")

        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
        assertEquals("Updated notes", viewModel.uiState.value.notes)
    }

    @Test
    fun `updateTotalRows for DOUBLE with blank shows required error`() = runTest {
        val project = sampleProject().copy(type = ProjectType.DOUBLE, totalRows = 20)
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1, ProjectType.DOUBLE)
        viewModel.updateTotalRows("")

        assertEquals(R.string.error_total_rows_required, viewModel.uiState.value.totalRowsError)
    }

    @Test
    fun `updateTotalRows for DOUBLE with zero shows greater-than-zero error`() = runTest {
        val project = sampleProject().copy(type = ProjectType.DOUBLE, totalRows = 20)
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1, ProjectType.DOUBLE)
        viewModel.updateTotalRows("0")

        assertEquals(R.string.error_total_rows_greater_than_zero, viewModel.uiState.value.totalRowsError)
    }

    @Test
    fun `updateTotalRows for SINGLE has no validation errors`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1, ProjectType.SINGLE)
        viewModel.updateTotalRows("")

        assertNull(viewModel.uiState.value.totalRowsError)
    }

    @Test
    fun `toggleCompleted marks unsaved changes`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1, ProjectType.SINGLE)
        viewModel.toggleCompleted(true)

        assertTrue(viewModel.uiState.value.isCompleted)
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `hasUnsavedChanges is false when value matches original`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1, ProjectType.SINGLE)
        viewModel.updateTitle("Changed")
        viewModel.updateTitle("Test Scarf")

        assertFalse(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `attemptDismissal with blank title sends ShowDiscardDialog`() = runTest {
        val viewModel = createViewModel()
        viewModel.loadProject(null, ProjectType.SINGLE)

        viewModel.dismissalResult.test {
            viewModel.attemptDismissal()
            assertEquals(DismissalResult.ShowDiscardDialog, awaitItem())
        }
    }

    @Test
    fun `attemptDismissal with valid title sends Allowed`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1, ProjectType.SINGLE)

        viewModel.dismissalResult.test {
            viewModel.attemptDismissal()
            assertEquals(DismissalResult.Allowed, awaitItem())
        }
    }

    @Test
    fun `attemptDismissal with invalid totalRows sends ShowDiscardDialog`() = runTest {
        val project = sampleProject().copy(type = ProjectType.DOUBLE, totalRows = 10)
        coEvery { getProject(1) } returns project
        coEvery {
            updateProjectDetailValues(
                id = any(),
                title = any(),
                notes = any(),
                totalRows = any(),
                projectType = any(),
                imagePaths = any(),
                completedAt = any(),
                updatedAt = any()
            )
        } returns UpdateProjectDetailResult.InvalidTotalRows

        val viewModel = createViewModel()
        viewModel.loadProject(1, ProjectType.DOUBLE)
        viewModel.updateTitle("Valid title")
        viewModel.updateTotalRows("0")

        viewModel.dismissalResult.test {
            viewModel.attemptDismissal()
            assertEquals(DismissalResult.ShowDiscardDialog, awaitItem())
        }
    }

    @Test
    fun `discardChanges reverts to original values`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1, ProjectType.SINGLE)
        viewModel.updateTitle("Changed Title")
        viewModel.updateNotes("Changed Notes")
        viewModel.discardChanges()

        val state = viewModel.uiState.value
        assertEquals("Test Scarf", state.title)
        assertEquals("Some notes", state.notes)
        assertFalse(state.hasUnsavedChanges)
        assertNull(state.titleError)
    }

    @Test
    fun `createProject with blank title sets error and does not save`() = runTest {
        val viewModel = createViewModel()
        viewModel.loadProject(null, ProjectType.SINGLE)
        viewModel.createProject()

        assertEquals(R.string.error_title_required, viewModel.uiState.value.titleError)
        coVerify(exactly = 0) { upsertProject(any()) }
    }

    @Test
    fun `createProject with DOUBLE and zero totalRows sets error`() = runTest {
        val viewModel = createViewModel()
        viewModel.loadProject(null, ProjectType.DOUBLE)
        viewModel.updateTitle("My Blanket")
        viewModel.createProject()

        assertEquals(R.string.error_total_rows_required_and_greater, viewModel.uiState.value.totalRowsError)
        coVerify(exactly = 0) { upsertProject(any()) }
    }

    @Test
    fun `createProject with valid data calls upsert and updates state`() = runTest {
        coEvery { upsertProject(any()) } returns UpsertProjectResult.Success(42L)

        val viewModel = createViewModel()
        viewModel.loadProject(null, ProjectType.SINGLE)
        viewModel.updateTitle("New Project")
        viewModel.createProject()

        val state = viewModel.uiState.value
        assertEquals(42, state.project!!.id)
        assertFalse(state.hasUnsavedChanges)
        assertNull(state.titleError)
    }

    @Test
    fun `save for existing project maps invalid total rows result to ui error`() = runTest {
        val project = sampleProject().copy(type = ProjectType.DOUBLE)
        coEvery { getProject(1) } returns project
        coEvery {
            updateProjectDetailValues(
                id = any(),
                title = any(),
                notes = any(),
                totalRows = any(),
                projectType = any(),
                imagePaths = any(),
                completedAt = any(),
                updatedAt = any()
            )
        } returns UpdateProjectDetailResult.InvalidTotalRows

        val viewModel = createViewModel()
        viewModel.loadProject(1, ProjectType.DOUBLE)
        viewModel.updateTitle("Valid title")
        viewModel.updateTotalRows("0")
        viewModel.save()

        assertEquals(R.string.error_total_rows_required_and_greater, viewModel.uiState.value.totalRowsError)
    }

    @Test
    fun `createProject maps invalid title result to ui error`() = runTest {
        coEvery { upsertProject(any()) } returns UpsertProjectResult.InvalidTitle

        val viewModel = createViewModel()
        viewModel.loadProject(null, ProjectType.SINGLE)
        viewModel.updateTitle("   ")
        viewModel.createProject()

        assertEquals(R.string.error_title_required, viewModel.uiState.value.titleError)
    }

    @Test
    fun `addImagePath updates imagePaths and marks unsaved`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1, ProjectType.SINGLE)
        viewModel.addImagePath("new_image.jpg")

        assertTrue(viewModel.uiState.value.imagePaths.contains("new_image.jpg"))
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `removeImagePath updates imagePaths`() = runTest {
        val project = sampleProject().copy(imagePaths = listOf("a.jpg", "b.jpg"))
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1, ProjectType.SINGLE)
        viewModel.removeImagePath("a.jpg")

        assertEquals(listOf("b.jpg"), viewModel.uiState.value.imagePaths)
    }

    @Test
    fun `addImagePath persists imagePaths and recreated ViewModel restores them`() = runTest {
        val project = sampleProject().copy(imagePaths = listOf("existing.jpg"))
        coEvery { getProject(1) } returns project

        val firstViewModel = createViewModel()
        firstViewModel.loadProject(1, ProjectType.SINGLE)
        firstViewModel.addImagePath("new.jpg")
        assertEquals(listOf("existing.jpg", "new.jpg"), firstViewModel.uiState.value.imagePaths)

        val recreatedViewModel = createViewModel()
        recreatedViewModel.loadProject(1, ProjectType.SINGLE)

        assertEquals(listOf("existing.jpg", "new.jpg"), recreatedViewModel.uiState.value.imagePaths)
    }

    @Test
    fun `removeImagePath persists removal and recreated ViewModel restores updated list`() = runTest {
        val project = sampleProject().copy(imagePaths = listOf("a.jpg", "b.jpg"))
        coEvery { getProject(1) } returns project

        val firstViewModel = createViewModel()
        firstViewModel.loadProject(1, ProjectType.SINGLE)
        firstViewModel.removeImagePath("a.jpg")
        assertEquals(listOf("b.jpg"), firstViewModel.uiState.value.imagePaths)

        val recreatedViewModel = createViewModel()
        recreatedViewModel.loadProject(1, ProjectType.SINGLE)

        assertEquals(listOf("b.jpg"), recreatedViewModel.uiState.value.imagePaths)
    }

    @Test
    fun `loadProjectById restores saved imagePaths instead of database imagePaths`() = runTest {
        val project = sampleProject().copy(imagePaths = listOf("database.jpg"))
        coEvery { getProject(1) } returns project

        val firstViewModel = createViewModel()
        firstViewModel.loadProjectById(1)
        firstViewModel.addImagePath("saved.jpg")
        assertEquals(listOf("database.jpg", "saved.jpg"), firstViewModel.uiState.value.imagePaths)

        val recreatedViewModel = createViewModel()
        recreatedViewModel.loadProjectById(1)

        assertEquals(listOf("database.jpg", "saved.jpg"), recreatedViewModel.uiState.value.imagePaths)
    }

    @Test
    fun `loadProject for existing project shows totalRows as string`() = runTest {
        val project = sampleProject().copy(totalRows = 50)
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1, ProjectType.SINGLE)

        assertEquals("50", viewModel.uiState.value.totalRows)
    }

    @Test
    fun `loadProject for existing project with zero totalRows shows empty string`() = runTest {
        val project = sampleProject().copy(totalRows = 0)
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1, ProjectType.SINGLE)

        assertEquals("", viewModel.uiState.value.totalRows)
    }
}
