package dev.harrisonsoftware.stitchCounter.feature.library

import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.domain.usecase.DeleteProject
import dev.harrisonsoftware.stitchCounter.domain.usecase.DeleteProjects
import dev.harrisonsoftware.stitchCounter.domain.usecase.ObserveProjects
import dev.harrisonsoftware.stitchCounter.logging.AppLogger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var observeProjects: ObserveProjects
    private lateinit var deleteProject: DeleteProject
    private lateinit var deleteProjects: DeleteProjects
    private lateinit var appLogger: AppLogger

    private val sampleProjects = listOf(
        Project(id = 1, type = ProjectType.SINGLE, title = "Scarf", createdAt = 1L, updatedAt = 1L),
        Project(id = 2, type = ProjectType.DOUBLE, title = "Blanket", createdAt = 2L, updatedAt = 2L),
        Project(id = 3, type = ProjectType.SINGLE, title = "Hat", createdAt = 3L, updatedAt = 3L),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        observeProjects = mockk()
        deleteProject = mockk(relaxed = true)
        deleteProjects = mockk(relaxed = true)
        appLogger = mockk(relaxed = true)
        every { observeProjects() } returns flowOf(sampleProjects)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = LibraryViewModel(
        observeProjects = observeProjects,
        deleteProject = deleteProject,
        deleteProjects = deleteProjects,
        appLogger = appLogger,
    )

    @Test
    fun `initial uiState has isLoading true`() {
        every { observeProjects() } returns flowOf()
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `projects flow sets isLoading to false`() = runTest {
        val viewModel = createViewModel()
        // Subscribe to projects so WhileSubscribed starts collecting
        backgroundScope.launch(testDispatcher) { viewModel.projects.collect {} }
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `toggleMultiSelectMode enables multi-select and clears selection`() {
        val viewModel = createViewModel()
        viewModel.toggleProjectSelection(1)
        viewModel.toggleMultiSelectMode()

        assertTrue(viewModel.uiState.value.isMultiSelectMode)
        assertTrue(viewModel.uiState.value.selectedProjectIds.isEmpty())
    }

    @Test
    fun `toggleMultiSelectMode disables multi-select on second call`() {
        val viewModel = createViewModel()
        viewModel.toggleMultiSelectMode()
        viewModel.toggleMultiSelectMode()

        assertFalse(viewModel.uiState.value.isMultiSelectMode)
    }

    @Test
    fun `toggleProjectSelection adds project id to selection`() {
        val viewModel = createViewModel()
        viewModel.toggleProjectSelection(1)

        assertTrue(viewModel.uiState.value.selectedProjectIds.contains(1))
    }

    @Test
    fun `toggleProjectSelection removes already-selected project id`() {
        val viewModel = createViewModel()
        viewModel.toggleProjectSelection(1)
        viewModel.toggleProjectSelection(1)

        assertFalse(viewModel.uiState.value.selectedProjectIds.contains(1))
    }

    @Test
    fun `selectAllProjects selects all project ids`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(testDispatcher) { viewModel.projects.collect {} }
        viewModel.selectAllProjects()

        assertEquals(setOf(1, 2, 3), viewModel.uiState.value.selectedProjectIds)
    }

    @Test
    fun `clearSelection empties selection`() {
        val viewModel = createViewModel()
        viewModel.toggleProjectSelection(1)
        viewModel.toggleProjectSelection(2)
        viewModel.clearSelection()

        assertTrue(viewModel.uiState.value.selectedProjectIds.isEmpty())
    }

    @Test
    fun `requestDelete shows confirmation for single project`() {
        val viewModel = createViewModel()
        val project = sampleProjects[0]
        viewModel.requestDelete(project)

        val state = viewModel.uiState.value
        assertTrue(state.showDeleteConfirmation)
        assertEquals(listOf(project), state.projectsToDelete)
    }

    @Test
    fun `requestBulkDelete shows confirmation for selected projects`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(testDispatcher) { viewModel.projects.collect {} }
        viewModel.toggleProjectSelection(1)
        viewModel.toggleProjectSelection(3)
        viewModel.requestBulkDelete()

        val state = viewModel.uiState.value
        assertTrue(state.showDeleteConfirmation)
        assertEquals(2, state.projectsToDelete.size)
        assertTrue(state.projectsToDelete.any { it.id == 1 })
        assertTrue(state.projectsToDelete.any { it.id == 3 })
    }

    @Test
    fun `requestBulkDelete does nothing when no projects selected`() {
        val viewModel = createViewModel()
        viewModel.requestBulkDelete()

        assertFalse(viewModel.uiState.value.showDeleteConfirmation)
    }

    @Test
    fun `confirmDelete for single project calls deleteProject`() = runTest {
        val viewModel = createViewModel()
        val project = sampleProjects[0]
        viewModel.requestDelete(project)
        viewModel.confirmDelete()

        coVerify { deleteProject(project) }
    }

    @Test
    fun `confirmDelete for multiple projects calls deleteProjects`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(testDispatcher) { viewModel.projects.collect {} }
        viewModel.toggleProjectSelection(1)
        viewModel.toggleProjectSelection(2)
        viewModel.requestBulkDelete()
        viewModel.confirmDelete()

        coVerify { deleteProjects(any()) }
    }

    @Test
    fun `confirmDelete resets state after deleting`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(testDispatcher) { viewModel.projects.collect {} }
        viewModel.toggleMultiSelectMode()
        viewModel.toggleProjectSelection(1)
        viewModel.requestBulkDelete()
        viewModel.confirmDelete()

        val state = viewModel.uiState.value
        assertFalse(state.showDeleteConfirmation)
        assertTrue(state.projectsToDelete.isEmpty())
        assertTrue(state.selectedProjectIds.isEmpty())
        assertFalse(state.isMultiSelectMode)
    }

    @Test
    fun `cancelDelete hides confirmation and clears projectsToDelete`() {
        val viewModel = createViewModel()
        viewModel.requestDelete(sampleProjects[0])
        viewModel.cancelDelete()

        val state = viewModel.uiState.value
        assertFalse(state.showDeleteConfirmation)
        assertTrue(state.projectsToDelete.isEmpty())
    }
}
