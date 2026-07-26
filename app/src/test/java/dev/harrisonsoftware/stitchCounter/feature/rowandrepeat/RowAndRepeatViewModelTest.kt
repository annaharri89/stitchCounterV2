package dev.harrisonsoftware.stitchCounter.feature.rowandrepeat

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.data.repo.AppPreferencesRepository
import dev.harrisonsoftware.stitchCounter.domain.model.DismissalResult
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.domain.usecase.GetProject
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpdateRowAndRepeatValues
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class RowAndRepeatViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var getProject: GetProject
    private lateinit var updateRowAndRepeatValues: UpdateRowAndRepeatValues
    private lateinit var appPreferencesRepository: AppPreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        savedStateHandle = SavedStateHandle()
        getProject = mockk()
        updateRowAndRepeatValues = mockk(relaxed = true)
        appPreferencesRepository = mockk()
        every { appPreferencesRepository.forceCounterScreensOn } returns flowOf(false)
        every { appPreferencesRepository.counterHapticFeedbackEnabled } returns flowOf(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = RowAndRepeatViewModel(
        savedStateHandle = savedStateHandle,
        getProject = getProject,
        updateRowAndRepeatValues = updateRowAndRepeatValues,
        appPreferencesRepository = appPreferencesRepository,
    )

    private fun sampleProject(id: Int = 1) = Project(
        id = id,
        type = ProjectType.ROW_AND_REPEAT,
        title = "Cable Repeat Scarf",
        stitchCounterNumber = 3,
        stitchAdjustment = 1,
        rowCounterNumber = 4,
        rowAdjustment = 8,
        totalRows = 20,
        createdAt = 1_000_000L,
        updatedAt = 1_000_000L,
    )

    @Test
    fun `initial state uses row and repeat defaults`() {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertEquals(0, state.id)
        assertEquals(RowAndRepeatUiState.ROW_COUNT_MIN, state.rowCount)
        assertEquals(RowAndRepeatUiState.DEFAULT_ROWS_PER_REPEAT, state.rowsPerRepeat)
        assertEquals(0, state.repeatCount)
        assertEquals(RowAndRepeatUiState.DEFAULT_REPEAT_GOAL, state.repeatGoal)
    }

    @Test
    fun `initial state defaults counter haptic feedback to enabled`() {
        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.counterHapticFeedbackEnabled)
    }

    @Test
    fun `init observes keep screen on`() {
        every { appPreferencesRepository.forceCounterScreensOn } returns flowOf(true)
        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.forceCounterScreensOn)
    }

    @Test
    fun `init observes counter haptic feedback enabled when true`() {
        every { appPreferencesRepository.counterHapticFeedbackEnabled } returns flowOf(true)
        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.counterHapticFeedbackEnabled)
    }

    @Test
    fun `init observes counter haptic feedback enabled when false`() {
        every { appPreferencesRepository.counterHapticFeedbackEnabled } returns flowOf(false)
        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.counterHapticFeedbackEnabled)
    }

    @Test
    fun `resetState keeps counter haptic feedback preference`() = runTest {
        every { appPreferencesRepository.counterHapticFeedbackEnabled } returns flowOf(false)
        coEvery { getProject(1) } returns sampleProject()

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.resetState()

        assertFalse(viewModel.uiState.value.counterHapticFeedbackEnabled)
    }

    @Test
    fun `loadProject with missing project sets load error`() = runTest {
        coEvery { getProject(99) } returns null
        val viewModel = createViewModel()

        viewModel.loadProject(99)

        assertEquals(R.string.error_project_not_found, viewModel.uiState.value.loadError)
        assertEquals(0, viewModel.uiState.value.id)
    }

    @Test
    fun `loadProject populates state from project fields`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)

        val state = viewModel.uiState.value
        assertEquals(1, state.id)
        assertEquals("Cable Repeat Scarf", state.title)
        assertEquals(4, state.rowCount)
        assertEquals(3, state.repeatCount)
        assertEquals(8, state.rowsPerRepeat)
        assertEquals(20, state.repeatGoal)
        assertEquals(0.15f, state.repeatProgress!!, 0.001f)
    }

    @Test
    fun `incrementRow advances repeat when row exceeds rows per repeat`() = runTest {
        val project = sampleProject().copy(rowCounterNumber = 8)
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.incrementRow()

        val state = viewModel.uiState.value
        assertEquals(RowAndRepeatUiState.ROW_COUNT_MIN, state.rowCount)
        assertEquals(4, state.repeatCount)
    }

    @Test
    fun `incrementRow does not advance repeat beyond goal`() = runTest {
        val project = sampleProject().copy(rowCounterNumber = 8, stitchCounterNumber = 20)
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.incrementRow()

        val state = viewModel.uiState.value
        assertEquals(RowAndRepeatUiState.ROW_COUNT_MIN, state.rowCount)
        assertEquals(20, state.repeatCount)
    }

    @Test
    fun `decrementRow does not go below one`() = runTest {
        val project = sampleProject().copy(rowCounterNumber = 1)
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.decrementRow()

        assertEquals(RowAndRepeatUiState.ROW_COUNT_MIN, viewModel.uiState.value.rowCount)
    }

    @Test
    fun `resetRow sets row count to one`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.resetRow()

        assertEquals(RowAndRepeatUiState.ROW_COUNT_MIN, viewModel.uiState.value.rowCount)
    }

    @Test
    fun `resetRow does not change repeat count`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        val repeatCountBeforeReset = viewModel.uiState.value.repeatCount

        viewModel.resetRow()

        assertEquals(repeatCountBeforeReset, viewModel.uiState.value.repeatCount)
    }

    @Test
    fun `resetAll clears repeat and resets row`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.resetAll()

        val state = viewModel.uiState.value
        assertEquals(RowAndRepeatUiState.ROW_COUNT_MIN, state.rowCount)
        assertEquals(0, state.repeatCount)
    }

    @Test
    fun `incrementRepeat advances repeat count`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.incrementRepeat()

        assertEquals(4, viewModel.uiState.value.repeatCount)
    }

    @Test
    fun `incrementRepeat does not exceed repeat goal`() = runTest {
        val project = sampleProject().copy(stitchCounterNumber = 20)
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.incrementRepeat()

        assertEquals(20, viewModel.uiState.value.repeatCount)
    }

    @Test
    fun `decrementRepeat lowers repeat count`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.decrementRepeat()

        assertEquals(2, viewModel.uiState.value.repeatCount)
    }

    @Test
    fun `decrementRepeat does not go below zero`() = runTest {
        val project = sampleProject().copy(stitchCounterNumber = 0)
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.decrementRepeat()

        assertEquals(RowAndRepeatUiState.REPEAT_COUNT_MIN, viewModel.uiState.value.repeatCount)
    }

    @Test
    fun `reloadProject refreshes rows per repeat and repeat goal while preserving counters`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.incrementRow()

        val updatedProject = sampleProject().copy(
            rowAdjustment = 12,
            totalRows = 30,
        )
        coEvery { getProject(1) } returns updatedProject

        viewModel.loadProject(1)

        val state = viewModel.uiState.value
        assertEquals(12, state.rowsPerRepeat)
        assertEquals(30, state.repeatGoal)
        assertEquals(5, state.rowCount)
        assertEquals(3, state.repeatCount)
    }

    @Test
    fun `persist writes mapped values to repository`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.incrementRow()

        coVerify {
            updateRowAndRepeatValues(
                id = 1,
                repeatCount = any(),
                rowCount = any(),
                rowsPerRepeat = 8,
                repeatGoal = 20,
                clearCompletedAt = false,
                updatedAt = any()
            )
        }
    }

    @Test
    fun `attemptDismissal emits allowed result`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)

        viewModel.dismissalResult.test {
            viewModel.attemptDismissal()
            assertEquals(DismissalResult.Allowed, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetState clears counter values but keeps preferences`() = runTest {
        every { appPreferencesRepository.forceCounterScreensOn } returns flowOf(true)
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.resetState()

        val state = viewModel.uiState.value
        assertEquals(0, state.id)
        assertEquals(0, state.repeatCount)
        assertTrue(state.forceCounterScreensOn)
    }
}
