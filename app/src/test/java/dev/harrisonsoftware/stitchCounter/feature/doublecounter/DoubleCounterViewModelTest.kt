package dev.harrisonsoftware.stitchCounter.feature.doublecounter

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import dev.harrisonsoftware.stitchCounter.data.repo.AppPreferencesRepository
import dev.harrisonsoftware.stitchCounter.domain.model.AdjustmentAmount
import dev.harrisonsoftware.stitchCounter.domain.model.DismissalResult
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.domain.usecase.GetProject
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpdateDoubleCounterValues
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
class DoubleCounterViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var getProject: GetProject
    private lateinit var updateDoubleCounterValues: UpdateDoubleCounterValues
    private lateinit var appPreferencesRepository: AppPreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        savedStateHandle = SavedStateHandle()
        getProject = mockk()
        updateDoubleCounterValues = mockk(relaxed = true)
        appPreferencesRepository = mockk()
        coEvery { appPreferencesRepository.consumeShouldShowCustomAdjustmentTip() } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = DoubleCounterViewModel(
        savedStateHandle = savedStateHandle,
        getProject = getProject,
        updateDoubleCounterValues = updateDoubleCounterValues,
        appPreferencesRepository = appPreferencesRepository,
    )

    private fun sampleProject(id: Int = 1) = Project(
        id = id,
        type = ProjectType.DOUBLE,
        title = "Test Blanket",
        stitchCounterNumber = 10,
        stitchAdjustment = 1,
        rowCounterNumber = 5,
        rowAdjustment = 1,
        totalRows = 20,
        totalStitchesEver = 100,
        createdAt = 1_000_000L,
        updatedAt = 1_000_000L,
    )

    @Test
    fun `initial state has zero counts`() {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value
        assertEquals(0, state.stitchCounterState.count)
        assertEquals(0, state.rowCounterState.count)
        assertEquals(0, state.totalRows)
    }

    @Test
    fun `loadProject populates state from project`() = runTest {
        coEvery { getProject(1) } returns sampleProject()
        val viewModel = createViewModel()
        viewModel.loadProject(1)

        val state = viewModel.uiState.value
        assertEquals(1, state.id)
        assertEquals("Test Blanket", state.title)
        assertEquals(10, state.stitchCounterState.count)
        assertEquals(5, state.rowCounterState.count)
        assertEquals(20, state.totalRows)
        assertEquals(100, state.totalStitchesEver)
    }

    @Test
    fun `increment STITCH increases stitch count`() = runTest {
        coEvery { getProject(1) } returns sampleProject()
        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.increment(CounterType.STITCH)

        assertEquals(11, viewModel.uiState.value.stitchCounterState.count)
    }

    @Test
    fun `increment STITCH increases totalStitchesEver`() = runTest {
        coEvery { getProject(1) } returns sampleProject()
        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.increment(CounterType.STITCH)

        assertEquals(101, viewModel.uiState.value.totalStitchesEver)
    }

    @Test
    fun `increment ROW increases row count`() = runTest {
        coEvery { getProject(1) } returns sampleProject()
        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.increment(CounterType.ROW)

        assertEquals(6, viewModel.uiState.value.rowCounterState.count)
    }

    @Test
    fun `increment ROW does not affect totalStitchesEver`() = runTest {
        coEvery { getProject(1) } returns sampleProject()
        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.increment(CounterType.ROW)

        assertEquals(100, viewModel.uiState.value.totalStitchesEver)
    }

    @Test
    fun `row count is capped at totalRows`() = runTest {
        val project = sampleProject().copy(rowCounterNumber = 19, totalRows = 20)
        coEvery { getProject(1) } returns project
        val viewModel = createViewModel()
        viewModel.loadProject(1)

        viewModel.increment(CounterType.ROW)
        assertEquals(20, viewModel.uiState.value.rowCounterState.count)

        viewModel.increment(CounterType.ROW)
        assertEquals(20, viewModel.uiState.value.rowCounterState.count)
    }

    @Test
    fun `row count is not capped when totalRows is zero`() = runTest {
        val project = sampleProject().copy(rowCounterNumber = 100, totalRows = 0)
        coEvery { getProject(1) } returns project
        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.increment(CounterType.ROW)

        assertEquals(101, viewModel.uiState.value.rowCounterState.count)
    }

    @Test
    fun `decrement STITCH decreases stitch count`() = runTest {
        coEvery { getProject(1) } returns sampleProject()
        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.decrement(CounterType.STITCH)

        assertEquals(9, viewModel.uiState.value.stitchCounterState.count)
    }

    @Test
    fun `decrement ROW decreases row count`() = runTest {
        coEvery { getProject(1) } returns sampleProject()
        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.decrement(CounterType.ROW)

        assertEquals(4, viewModel.uiState.value.rowCounterState.count)
    }

    @Test
    fun `decrement floors at zero for both counters`() = runTest {
        val project = sampleProject().copy(stitchCounterNumber = 0, rowCounterNumber = 0)
        coEvery { getProject(1) } returns project
        val viewModel = createViewModel()
        viewModel.loadProject(1)

        viewModel.decrement(CounterType.STITCH)
        viewModel.decrement(CounterType.ROW)

        assertEquals(0, viewModel.uiState.value.stitchCounterState.count)
        assertEquals(0, viewModel.uiState.value.rowCounterState.count)
    }

    @Test
    fun `reset STITCH sets stitch count to zero`() = runTest {
        coEvery { getProject(1) } returns sampleProject()
        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.reset(CounterType.STITCH)

        assertEquals(0, viewModel.uiState.value.stitchCounterState.count)
        assertEquals(5, viewModel.uiState.value.rowCounterState.count)
        coVerify {
            updateDoubleCounterValues(
                id = 1,
                stitchCount = any(),
                stitchAdjustment = any(),
                rowCount = any(),
                rowAdjustment = any(),
                totalStitchesEver = any(),
                clearCompletedAt = true,
                updatedAt = any()
            )
        }
    }

    @Test
    fun `reset ROW sets row count to zero`() = runTest {
        coEvery { getProject(1) } returns sampleProject()
        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.reset(CounterType.ROW)

        assertEquals(10, viewModel.uiState.value.stitchCounterState.count)
        assertEquals(0, viewModel.uiState.value.rowCounterState.count)
        coVerify {
            updateDoubleCounterValues(
                id = 1,
                stitchCount = any(),
                stitchAdjustment = any(),
                rowCount = any(),
                rowAdjustment = any(),
                totalStitchesEver = any(),
                clearCompletedAt = true,
                updatedAt = any()
            )
        }
    }

    @Test
    fun `resetAll resets both counters`() = runTest {
        coEvery { getProject(1) } returns sampleProject()
        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.resetAll()

        assertEquals(0, viewModel.uiState.value.stitchCounterState.count)
        assertEquals(0, viewModel.uiState.value.rowCounterState.count)
        coVerify(exactly = 2) {
            updateDoubleCounterValues(
                id = 1,
                stitchCount = any(),
                stitchAdjustment = any(),
                rowCount = any(),
                rowAdjustment = any(),
                totalStitchesEver = any(),
                clearCompletedAt = true,
                updatedAt = any()
            )
        }
    }

    @Test
    fun `changeAdjustment updates correct counter`() = runTest {
        val viewModel = createViewModel()
        viewModel.changeAdjustment(CounterType.STITCH, AdjustmentAmount.FIVE)

        assertEquals(AdjustmentAmount.FIVE, viewModel.uiState.value.stitchCounterState.adjustment)
        assertEquals(AdjustmentAmount.ONE, viewModel.uiState.value.rowCounterState.adjustment)
    }

    @Test
    fun `setCustomAdjustmentAmount updates correct counter`() = runTest {
        val viewModel = createViewModel()
        viewModel.setCustomAdjustmentAmount(CounterType.ROW, 3)

        val rowState = viewModel.uiState.value.rowCounterState
        assertEquals(AdjustmentAmount.CUSTOM, rowState.adjustment)
        assertEquals(3, rowState.customAdjustmentAmount)
    }

    @Test
    fun `setCustomAdjustmentAmount coerces value to at least 1`() = runTest {
        val viewModel = createViewModel()
        viewModel.setCustomAdjustmentAmount(CounterType.STITCH, 0)

        assertEquals(1, viewModel.uiState.value.stitchCounterState.customAdjustmentAmount)
    }

    @Test
    fun `rowProgress is null when totalRows is zero`() {
        val state = DoubleCounterUiState(totalRows = 0)
        assertNull(state.rowProgress)
    }

    @Test
    fun `rowProgress is calculated correctly`() {
        val state = DoubleCounterUiState(
            rowCounterState = dev.harrisonsoftware.stitchCounter.domain.model.CounterState(count = 10),
            totalRows = 20
        )
        assertEquals(0.5f, state.rowProgress!!, 0.001f)
    }

    @Test
    fun `rowProgress is clamped to 1`() {
        val state = DoubleCounterUiState(
            rowCounterState = dev.harrisonsoftware.stitchCounter.domain.model.CounterState(count = 25),
            totalRows = 20
        )
        assertEquals(1.0f, state.rowProgress!!, 0.001f)
    }

    @Test
    fun `rowProgress is zero when row count is zero`() {
        val state = DoubleCounterUiState(
            rowCounterState = dev.harrisonsoftware.stitchCounter.domain.model.CounterState(count = 0),
            totalRows = 20
        )
        assertEquals(0.0f, state.rowProgress!!, 0.001f)
    }

    @Test
    fun `attemptDismissal sends Allowed`() = runTest {
        coEvery { getProject(1) } returns sampleProject()
        val viewModel = createViewModel()
        viewModel.loadProject(1)

        viewModel.dismissalResult.test {
            viewModel.attemptDismissal()
            assertEquals(DismissalResult.Allowed, awaitItem())
        }
    }

    @Test
    fun `loadProject with null resets state`() = runTest {
        val viewModel = createViewModel()
        viewModel.loadProject(null)

        assertEquals(DoubleCounterUiState(), viewModel.uiState.value)
    }

    @Test
    fun `showCustomAdjustmentDialog sets active counter type and populates input`() = runTest {
        val viewModel = createViewModel()
        viewModel.setCustomAdjustmentAmount(CounterType.STITCH, 7)
        viewModel.showCustomAdjustmentDialog(CounterType.STITCH)

        val state = viewModel.uiState.value
        assertEquals(CounterType.STITCH, state.activeCustomAdjustmentDialogCounterType)
        assertEquals("7", state.customAdjustmentDialogInput)
    }

    @Test
    fun `showCustomAdjustmentDialog for ROW uses row custom amount`() = runTest {
        val viewModel = createViewModel()
        viewModel.setCustomAdjustmentAmount(CounterType.ROW, 3)
        viewModel.showCustomAdjustmentDialog(CounterType.ROW)

        val state = viewModel.uiState.value
        assertEquals(CounterType.ROW, state.activeCustomAdjustmentDialogCounterType)
        assertEquals("3", state.customAdjustmentDialogInput)
    }

    @Test
    fun `dismissCustomAdjustmentDialog clears active type and input`() = runTest {
        val viewModel = createViewModel()
        viewModel.showCustomAdjustmentDialog(CounterType.STITCH)
        viewModel.dismissCustomAdjustmentDialog()

        val state = viewModel.uiState.value
        assertNull(state.activeCustomAdjustmentDialogCounterType)
        assertEquals("", state.customAdjustmentDialogInput)
    }

    @Test
    fun `updateCustomAdjustmentDialogInput updates the input`() = runTest {
        val viewModel = createViewModel()
        viewModel.showCustomAdjustmentDialog(CounterType.ROW)
        viewModel.updateCustomAdjustmentDialogInput("99")

        assertEquals("99", viewModel.uiState.value.customAdjustmentDialogInput)
    }

    @Test
    fun `dialog state is not tied to wrong counter after switching`() = runTest {
        val viewModel = createViewModel()
        viewModel.setCustomAdjustmentAmount(CounterType.STITCH, 5)
        viewModel.setCustomAdjustmentAmount(CounterType.ROW, 10)

        viewModel.showCustomAdjustmentDialog(CounterType.STITCH)
        assertEquals("5", viewModel.uiState.value.customAdjustmentDialogInput)
        viewModel.dismissCustomAdjustmentDialog()

        viewModel.showCustomAdjustmentDialog(CounterType.ROW)
        assertEquals("10", viewModel.uiState.value.customAdjustmentDialogInput)
        assertEquals(CounterType.ROW, viewModel.uiState.value.activeCustomAdjustmentDialogCounterType)
    }

    @Test
    fun `increment persists to Room`() = runTest {
        coEvery { getProject(1) } returns sampleProject()
        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.increment(CounterType.STITCH)

        coVerify {
            updateDoubleCounterValues(
                id = 1,
                stitchCount = any(),
                stitchAdjustment = any(),
                rowCount = any(),
                rowAdjustment = any(),
                totalStitchesEver = any(),
                clearCompletedAt = false,
                updatedAt = any()
            )
        }
    }
}
