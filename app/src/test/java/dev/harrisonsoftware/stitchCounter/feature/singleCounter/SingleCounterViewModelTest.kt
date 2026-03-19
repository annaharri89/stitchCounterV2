package dev.harrisonsoftware.stitchCounter.feature.singleCounter

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import dev.harrisonsoftware.stitchCounter.data.repo.AppPreferencesRepository
import dev.harrisonsoftware.stitchCounter.domain.model.AdjustmentAmount
import dev.harrisonsoftware.stitchCounter.domain.model.CounterState
import dev.harrisonsoftware.stitchCounter.domain.model.DismissalResult
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.domain.usecase.GetProject
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpdateSingleCounterValues
import dev.harrisonsoftware.stitchCounter.logging.AppLogger
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SingleCounterViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var getProject: GetProject
    private lateinit var updateSingleCounterValues: UpdateSingleCounterValues
    private lateinit var appPreferencesRepository: AppPreferencesRepository
    private lateinit var appLogger: AppLogger

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        savedStateHandle = SavedStateHandle()
        getProject = mockk()
        updateSingleCounterValues = mockk(relaxed = true)
        appPreferencesRepository = mockk()
        appLogger = mockk(relaxed = true)
        coEvery { appPreferencesRepository.consumeShouldShowCustomAdjustmentTip() } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SingleCounterViewModel(
        savedStateHandle = savedStateHandle,
        getProject = getProject,
        updateSingleCounterValues = updateSingleCounterValues,
        appPreferencesRepository = appPreferencesRepository,
        appLogger = appLogger,
    )

    private fun sampleProject(id: Int = 1) = Project(
        id = id,
        type = ProjectType.SINGLE,
        title = "Test Scarf",
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
    fun `initial state has zero count and default adjustment`() {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value
        assertEquals(0, state.id)
        assertEquals(0, state.counterState.count)
        assertEquals(AdjustmentAmount.ONE, state.counterState.adjustment)
    }

    @Test
    fun `loadProject populates state from project`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)

        val state = viewModel.uiState.value
        assertEquals(1, state.id)
        assertEquals("Test Scarf", state.title)
        assertEquals(10, state.counterState.count)
        assertEquals(50, state.totalStitchesEver)
    }

    @Test
    fun `loadProject with null resets state`() = runTest {
        val viewModel = createViewModel()
        viewModel.loadProject(null)

        assertEquals(SingleCounterUiState(), viewModel.uiState.value)
    }

    @Test
    fun `loadProject with zero resets state`() = runTest {
        val viewModel = createViewModel()
        viewModel.loadProject(0)

        assertEquals(SingleCounterUiState(), viewModel.uiState.value)
    }

    @Test
    fun `increment increases count by adjustment amount`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.increment()

        assertEquals(11, viewModel.uiState.value.counterState.count)
    }

    @Test
    fun `increment increases totalStitchesEver`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.increment()

        assertEquals(51, viewModel.uiState.value.totalStitchesEver)
    }

    @Test
    fun `increment by FIVE adds 5 to count and totalStitchesEver`() = runTest {
        val project = sampleProject().copy(stitchAdjustment = 5)
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.increment()

        assertEquals(15, viewModel.uiState.value.counterState.count)
        assertEquals(55, viewModel.uiState.value.totalStitchesEver)
    }

    @Test
    fun `decrement decreases count by adjustment amount`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.decrement()

        assertEquals(9, viewModel.uiState.value.counterState.count)
    }

    @Test
    fun `decrement floors at zero`() = runTest {
        val project = sampleProject().copy(stitchCounterNumber = 0)
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.decrement()

        assertEquals(0, viewModel.uiState.value.counterState.count)
    }

    @Test
    fun `resetCount sets count to zero but preserves other state`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.increment()
        viewModel.resetCount()

        val state = viewModel.uiState.value
        assertEquals(0, state.counterState.count)
        assertEquals(1, state.id)
        assertEquals("Test Scarf", state.title)
        coVerify {
            updateSingleCounterValues(
                id = 1,
                stitchCount = 0,
                stitchAdjustment = any(),
                totalStitchesEver = any(),
                clearCompletedAt = true,
                updatedAt = any()
            )
        }
    }

    @Test
    fun `changeAdjustment updates the adjustment amount`() = runTest {
        val viewModel = createViewModel()
        viewModel.changeAdjustment(AdjustmentAmount.FIVE)

        assertEquals(AdjustmentAmount.FIVE, viewModel.uiState.value.counterState.adjustment)
    }

    @Test
    fun `setCustomAdjustmentAmount sets CUSTOM adjustment with given value`() = runTest {
        val viewModel = createViewModel()
        viewModel.setCustomAdjustmentAmount(7)

        val state = viewModel.uiState.value
        assertEquals(AdjustmentAmount.CUSTOM, state.counterState.adjustment)
        assertEquals(7, state.counterState.customAdjustmentAmount)
    }

    @Test
    fun `setCustomAdjustmentAmount coerces zero to 1`() = runTest {
        val viewModel = createViewModel()
        viewModel.setCustomAdjustmentAmount(0)

        assertEquals(1, viewModel.uiState.value.counterState.customAdjustmentAmount)
    }

    @Test
    fun `setCustomAdjustmentAmount coerces negative to 1`() = runTest {
        val viewModel = createViewModel()
        viewModel.setCustomAdjustmentAmount(-5)

        assertEquals(1, viewModel.uiState.value.counterState.customAdjustmentAmount)
    }

    @Test
    fun `attemptDismissal sends Allowed result`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)

        viewModel.dismissalResult.test {
            viewModel.attemptDismissal()
            assertEquals(DismissalResult.Allowed, awaitItem())
        }
    }

    @Test
    fun `attemptDismissal calls saveToRoom`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.attemptDismissal()

        coVerify {
            updateSingleCounterValues(
                id = 1,
                stitchCount = any(),
                stitchAdjustment = any(),
                totalStitchesEver = any(),
                clearCompletedAt = false,
                updatedAt = any()
            )
        }
    }

    @Test
    fun `init shows custom adjustment tip when repository returns true`() = runTest {
        coEvery { appPreferencesRepository.consumeShouldShowCustomAdjustmentTip() } returns true
        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.shouldShowCustomAdjustmentTip)
    }

    @Test
    fun `init does not show custom adjustment tip when repository returns false`() = runTest {
        coEvery { appPreferencesRepository.consumeShouldShowCustomAdjustmentTip() } returns false
        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.shouldShowCustomAdjustmentTip)
    }

    @Test
    fun `onCustomAdjustmentTipShown hides the tip`() = runTest {
        coEvery { appPreferencesRepository.consumeShouldShowCustomAdjustmentTip() } returns true
        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.shouldShowCustomAdjustmentTip)
        viewModel.onCustomAdjustmentTipShown()
        assertFalse(viewModel.uiState.value.shouldShowCustomAdjustmentTip)
    }

    @Test
    fun `resetState clears all state`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.increment()
        viewModel.resetState()

        assertEquals(SingleCounterUiState(), viewModel.uiState.value)
    }

    @Test
    fun `increment persists to Room`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        val viewModel = createViewModel()
        viewModel.loadProject(1)
        viewModel.increment()

        coVerify {
            updateSingleCounterValues(
                id = 1,
                stitchCount = any(),
                stitchAdjustment = any(),
                totalStitchesEver = any(),
                clearCompletedAt = false,
                updatedAt = any()
            )
        }
    }

    @Test
    fun `showCustomAdjustmentDialog sets visible and populates input`() = runTest {
        val viewModel = createViewModel()
        viewModel.setCustomAdjustmentAmount(7)
        viewModel.showCustomAdjustmentDialog()

        val state = viewModel.uiState.value
        assertTrue(state.customAdjustmentDialogState.isVisible)
        assertEquals("7", state.customAdjustmentDialogState.input)
    }

    @Test
    fun `dismissCustomAdjustmentDialog clears visibility and input`() = runTest {
        val viewModel = createViewModel()
        viewModel.showCustomAdjustmentDialog()
        viewModel.dismissCustomAdjustmentDialog()

        val state = viewModel.uiState.value
        assertFalse(state.customAdjustmentDialogState.isVisible)
        assertEquals("", state.customAdjustmentDialogState.input)
    }

    @Test
    fun `updateCustomAdjustmentDialogInput updates the input`() = runTest {
        val viewModel = createViewModel()
        viewModel.showCustomAdjustmentDialog()
        viewModel.updateCustomAdjustmentDialogInput("42")

        assertEquals("42", viewModel.uiState.value.customAdjustmentDialogState.input)
    }

    @Test
    fun `loadProject restores from SavedStateHandle when available`() = runTest {
        val project = sampleProject()
        coEvery { getProject(1) } returns project

        savedStateHandle["single_project_id"] = 1
        savedStateHandle["single_counter_count"] = 99
        savedStateHandle["single_counter_adjustment"] = 1
        savedStateHandle["single_total_stitches_ever"] = 200

        val viewModel = createViewModel()
        viewModel.loadProject(1)

        val state = viewModel.uiState.value
        assertEquals(99, state.counterState.count)
        assertEquals(200, state.totalStitchesEver)
    }
}
