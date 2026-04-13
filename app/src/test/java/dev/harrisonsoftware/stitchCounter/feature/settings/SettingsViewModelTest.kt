package dev.harrisonsoftware.stitchCounter.feature.settings

import app.cash.turbine.test
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.Constants
import dev.harrisonsoftware.stitchCounter.data.backup.BackupManagerError
import dev.harrisonsoftware.stitchCounter.data.repo.AppPreferencesRepository
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme
import dev.harrisonsoftware.stitchCounter.domain.model.ContentUri
import dev.harrisonsoftware.stitchCounter.domain.usecase.ExportLibrary
import dev.harrisonsoftware.stitchCounter.domain.usecase.ExportLibraryError
import dev.harrisonsoftware.stitchCounter.domain.usecase.ExportLibraryResult
import dev.harrisonsoftware.stitchCounter.domain.usecase.ImportLibrary
import dev.harrisonsoftware.stitchCounter.domain.usecase.ImportLibraryError
import dev.harrisonsoftware.stitchCounter.domain.usecase.ImportLibraryResult
import dev.harrisonsoftware.stitchCounter.domain.usecase.ImportResult
import dev.harrisonsoftware.stitchCounter.feature.theme.LauncherIconManager
import dev.harrisonsoftware.stitchCounter.feature.theme.ThemeManager
import dev.harrisonsoftware.stitchCounter.logging.BugReportLogPackager
import dev.harrisonsoftware.stitchCounter.logging.BugReportLogPackagerResult
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
    private lateinit var bugReportLogPackager: BugReportLogPackager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        appPreferencesRepository = mockk(relaxed = true)
        themeManager = mockk(relaxed = true)
        launcherIconManager = mockk(relaxed = true)
        exportLibrary = mockk()
        importLibrary = mockk()
        bugReportLogPackager = mockk()
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
        bugReportLogPackager = bugReportLogPackager,
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
        coEvery { exportLibrary(any()) } returns ExportLibraryResult.Success(ContentUri("content://test"))

        val viewModel = createViewModel()
        viewModel.exportLibrary()

        val state = viewModel.uiState.value
        assertFalse(state.isExporting)
        assertTrue(state.exportSuccess)
        assertNull(state.exportError)
    }

    @Test
    fun `exportLibrary sets exportError on failure`() = runTest {
        coEvery { exportLibrary(any()) } returns ExportLibraryResult.Failure(ExportLibraryError.Unexpected(Exception()))

        val viewModel = createViewModel()
        viewModel.exportLibrary()

        val state = viewModel.uiState.value
        assertFalse(state.isExporting)
        assertFalse(state.exportSuccess)
        assertEquals(R.string.settings_error_unexpected, state.exportError?.resId)
    }

    @Test
    fun `exportLibrary maps backup json missing to invalid backup message`() = runTest {
        coEvery { exportLibrary(any()) } returns ExportLibraryResult.Failure(
            ExportLibraryError.BackupCreationFailed(BackupManagerError.BackupJsonMissing)
        )

        val viewModel = createViewModel()
        viewModel.exportLibrary()

        val state = viewModel.uiState.value
        assertEquals(R.string.settings_error_backup_invalid, state.exportError?.resId)
    }

    @Test
    fun `importLibrary sets importSuccess and importResult on success`() = runTest {
        val importResult = ImportResult(importedCount = 5, failedCount = 0, failedProjectNames = emptyList())
        coEvery { importLibrary(any(), any()) } returns ImportLibraryResult.Success(importResult)

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
        coEvery { importLibrary(any(), any()) } returns ImportLibraryResult.Failure(
            ImportLibraryError.UnsupportedBackupVersion(99)
        )

        val viewModel = createViewModel()
        viewModel.importLibrary(mockk(relaxed = true))

        val state = viewModel.uiState.value
        assertFalse(state.isImporting)
        assertFalse(state.importSuccess)
        assertEquals(R.string.settings_error_backup_unsupported_version, state.importError?.resId)
    }

    @Test
    fun `importLibrary maps unsafe zip entry to unsafe backup message`() = runTest {
        coEvery { importLibrary(any(), any()) } returns ImportLibraryResult.Failure(
            ImportLibraryError.BackupExtractionFailed(BackupManagerError.UnsafeZipEntry("../bad"))
        )

        val viewModel = createViewModel()
        viewModel.importLibrary(mockk(relaxed = true))

        val state = viewModel.uiState.value
        assertEquals(R.string.settings_error_backup_unsafe, state.importError?.resId)
    }

    @Test
    fun `clearExportStatus resets export flags`() = runTest {
        coEvery { exportLibrary(any()) } returns ExportLibraryResult.Success(ContentUri("content://test"))

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
        coEvery { importLibrary(any(), any()) } returns ImportLibraryResult.Success(importResult)

        val viewModel = createViewModel()
        viewModel.importLibrary(mockk(relaxed = true))
        viewModel.clearImportStatus()

        val state = viewModel.uiState.value
        assertFalse(state.importSuccess)
        assertNull(state.importError)
        assertNull(state.importResult)
    }

    @Test
    fun `onReportBug sends OpenBugReportShare with attachment when packaging succeeds`() = runTest {
        coEvery { bugReportLogPackager.packageLogsAsHtmlZip() } returns
            BugReportLogPackagerResult.Success(java.io.File("/tmp/stitch_diagnostics.zip"))

        val viewModel = createViewModel()
        viewModel.effect.test {
            viewModel.onReportBug()
            val effect = awaitItem()
            assertTrue(effect is SettingsEffect.OpenBugReportShare)
            effect as SettingsEffect.OpenBugReportShare
            assertEquals(Constants.BUG_REPORT_SUBJECT, effect.subject)
            assertEquals("/tmp/stitch_diagnostics.zip", effect.attachmentFilePath)
        }
    }

    @Test
    fun `onReportBug sends OpenBugReportShare without attachment when diagnostics disabled`() = runTest {
        val viewModel = createViewModel()
        viewModel.effect.test {
            viewModel.onReportBug(includeDiagnostics = false)
            val effect = awaitItem()
            assertTrue(effect is SettingsEffect.OpenBugReportShare)
            effect as SettingsEffect.OpenBugReportShare
            assertEquals(Constants.BUG_REPORT_SUBJECT, effect.subject)
            assertNull(effect.attachmentFilePath)
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
