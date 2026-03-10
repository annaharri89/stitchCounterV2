package dev.harrisonsoftware.stitchCounter.feature.settings

import app.cash.turbine.test
import dev.harrisonsoftware.stitchCounter.Constants
import dev.harrisonsoftware.stitchCounter.data.repo.AppPreferencesRepository
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme
import dev.harrisonsoftware.stitchCounter.domain.model.ContentUri
import dev.harrisonsoftware.stitchCounter.domain.usecase.ExportLibrary
import dev.harrisonsoftware.stitchCounter.domain.usecase.ImportLibrary
import dev.harrisonsoftware.stitchCounter.domain.usecase.ImportResult
import dev.harrisonsoftware.stitchCounter.feature.theme.LauncherIconManager
import dev.harrisonsoftware.stitchCounter.feature.theme.ThemeManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var appPreferencesRepository: AppPreferencesRepository
    private lateinit var themeManager: ThemeManager
    private lateinit var launcherIconManager: LauncherIconManager
    private lateinit var exportLibrary: ExportLibrary
    private lateinit var importLibrary: ImportLibrary

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        appPreferencesRepository = mockk(relaxed = true)
        themeManager = mockk(relaxed = true)
        launcherIconManager = mockk(relaxed = true)
        exportLibrary = mockk()
        importLibrary = mockk()
        every { appPreferencesRepository.selectedTheme } returns flowOf(AppTheme.FOREST_FIBER)
        every { themeManager.getThemeColors(any()) } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SettingsViewModel(
        appPreferencesRepository = appPreferencesRepository,
        themeManager = themeManager,
        launcherIconManager = launcherIconManager,
        exportLibraryUseCase = exportLibrary,
        importLibraryUseCase = importLibrary,
    )

    @Test
    fun `settings ui state defaults to forest fiber theme`() {
        assertEquals(AppTheme.FOREST_FIBER, SettingsUiState().selectedTheme)
    }

    @Test
    fun `init observes theme and updates state`() {
        every { appPreferencesRepository.selectedTheme } returns flowOf(AppTheme.SEA_COTTAGE)
        val viewModel = createViewModel()

        assertEquals(AppTheme.SEA_COTTAGE, viewModel.uiState.value.selectedTheme)
    }

    @Test
    fun `exportLibrary sets isExporting then exportSuccess on success`() = runTest {
        coEvery { exportLibrary(any()) } returns Result.success(ContentUri("content://test"))

        val viewModel = createViewModel()
        viewModel.exportLibrary()

        val state = viewModel.uiState.value
        assertFalse(state.isExporting)
        assertTrue(state.exportSuccess)
        assertNull(state.exportError)
    }

    @Test
    fun `exportLibrary sets exportError on failure`() = runTest {
        coEvery { exportLibrary(any()) } returns Result.failure(Exception("Disk full"))

        val viewModel = createViewModel()
        viewModel.exportLibrary()

        val state = viewModel.uiState.value
        assertFalse(state.isExporting)
        assertFalse(state.exportSuccess)
        assertEquals("Disk full", state.exportError)
    }

    @Test
    fun `importLibrary sets importSuccess and importResult on success`() = runTest {
        val importResult = ImportResult(importedCount = 5, failedCount = 0, failedProjectNames = emptyList())
        coEvery { importLibrary(any(), any()) } returns Result.success(importResult)

        val viewModel = createViewModel()
        viewModel.importLibrary(mockk(relaxed = true))

        val state = viewModel.uiState.value
        assertFalse(state.isImporting)
        assertTrue(state.importSuccess)
        assertEquals(importResult, state.importResult)
        assertNull(state.importError)
    }

    @Test
    fun `importLibrary sets importError on failure`() = runTest {
        coEvery { importLibrary(any(), any()) } returns Result.failure(Exception("Corrupt file"))

        val viewModel = createViewModel()
        viewModel.importLibrary(mockk(relaxed = true))

        val state = viewModel.uiState.value
        assertFalse(state.isImporting)
        assertFalse(state.importSuccess)
        assertEquals("Corrupt file", state.importError)
    }

    @Test
    fun `clearExportStatus resets export flags`() = runTest {
        coEvery { exportLibrary(any()) } returns Result.success(ContentUri("content://test"))

        val viewModel = createViewModel()
        viewModel.exportLibrary()
        viewModel.clearExportStatus()

        val state = viewModel.uiState.value
        assertFalse(state.exportSuccess)
        assertNull(state.exportError)
    }

    @Test
    fun `clearImportStatus resets import flags`() = runTest {
        val importResult = ImportResult(importedCount = 3, failedCount = 0, failedProjectNames = emptyList())
        coEvery { importLibrary(any(), any()) } returns Result.success(importResult)

        val viewModel = createViewModel()
        viewModel.importLibrary(mockk(relaxed = true))
        viewModel.clearImportStatus()

        val state = viewModel.uiState.value
        assertFalse(state.importSuccess)
        assertNull(state.importError)
        assertNull(state.importResult)
    }

    @Test
    fun `onReportBug sends OpenEmailClient effect with bug report subject`() = runTest {
        val viewModel = createViewModel()
        viewModel.effect.test {
            viewModel.onReportBug()
            val effect = awaitItem()
            assertTrue(effect is SettingsEffect.OpenEmailClient)
            assertEquals(Constants.BUG_REPORT_SUBJECT, (effect as SettingsEffect.OpenEmailClient).subject)
        }
    }

    @Test
    fun `onGiveFeedback sends OpenEmailClient effect with feedback subject`() = runTest {
        val viewModel = createViewModel()
        viewModel.effect.test {
            viewModel.onGiveFeedback()
            val effect = awaitItem()
            assertTrue(effect is SettingsEffect.OpenEmailClient)
            assertEquals(Constants.FEEDBACK_SUBJECT, (effect as SettingsEffect.OpenEmailClient).subject)
        }
    }

    @Test
    fun `onRequestFeature sends OpenEmailClient effect with feature request subject`() = runTest {
        val viewModel = createViewModel()
        viewModel.effect.test {
            viewModel.onRequestFeature()
            val effect = awaitItem()
            assertTrue(effect is SettingsEffect.OpenEmailClient)
            assertEquals(Constants.FEATURE_REQUEST_SUBJECT, (effect as SettingsEffect.OpenEmailClient).subject)
        }
    }

    @Test
    fun `onOpenPrivacyPolicy sends OpenPrivacyPolicy effect`() = runTest {
        val viewModel = createViewModel()
        viewModel.effect.test {
            viewModel.onOpenPrivacyPolicy()
            assertEquals(SettingsEffect.OpenPrivacyPolicy, awaitItem())
        }
    }

    @Test
    fun `onOpenEULA sends OpenEULA effect`() = runTest {
        val viewModel = createViewModel()
        viewModel.effect.test {
            viewModel.onOpenEULA()
            assertEquals(SettingsEffect.OpenEULA, awaitItem())
        }
    }

    @Test
    fun `onLaunchingExternalActivity delegates to launcherIconManager skip guard`() {
        val viewModel = createViewModel()

        viewModel.onLaunchingExternalActivity()

        verify(exactly = 1) { launcherIconManager.skipNextPendingIconApply() }
    }
}
