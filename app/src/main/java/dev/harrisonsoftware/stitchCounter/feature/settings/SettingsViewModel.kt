package dev.harrisonsoftware.stitchCounter.feature.settings

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.data.backup.BackupManagerError
import dev.harrisonsoftware.stitchCounter.data.repo.AppPreferencesRepository
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme
import dev.harrisonsoftware.stitchCounter.domain.model.ContentUri
import dev.harrisonsoftware.stitchCounter.domain.usecase.ExportLibraryError
import dev.harrisonsoftware.stitchCounter.domain.usecase.ExportLibraryResult
import dev.harrisonsoftware.stitchCounter.domain.usecase.ExportLibrary
import dev.harrisonsoftware.stitchCounter.domain.usecase.ImportLibraryError
import dev.harrisonsoftware.stitchCounter.domain.usecase.ImportLibraryResult
import dev.harrisonsoftware.stitchCounter.domain.usecase.ImportLibrary
import dev.harrisonsoftware.stitchCounter.feature.theme.LauncherIconManager
import dev.harrisonsoftware.stitchCounter.feature.theme.ThemeColor
import dev.harrisonsoftware.stitchCounter.feature.theme.ThemeManager
import dev.harrisonsoftware.stitchCounter.logging.BugReportLogPackager
import dev.harrisonsoftware.stitchCounter.logging.BugReportLogPackagerResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dev.harrisonsoftware.stitchCounter.Constants
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
    private val themeManager: ThemeManager,
    private val launcherIconManager: LauncherIconManager,
    private val exportLibraryUseCase: ExportLibrary,
    private val importLibraryUseCase: ImportLibrary,
    private val bugReportLogPackager: BugReportLogPackager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SettingsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeTheme()
    }

    private fun observeTheme() {
        viewModelScope.launch {
            appPreferencesRepository.selectedTheme.collect { theme ->
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedTheme = theme,
                        themeColors = themeManager.getThemeColors(theme)
                    )
                }
            }
        }
    }

    fun onThemeSelected(theme: AppTheme) {
        viewModelScope.launch {
            appPreferencesRepository.setTheme(theme)
            launcherIconManager.pendingTheme = theme
        }
    }

    fun onLaunchingExternalActivity() {
        launcherIconManager.skipNextPendingIconApply()
    }

    fun exportLibrary(outputUri: Uri? = null) {
        viewModelScope.launch {
            Timber.tag(Constants.LOG_TAG_SETTINGS_VIEW_MODEL).i("event=export_requested hasOutputUri=${outputUri != null}")
            _uiState.update { it.copy(isExporting = true, exportError = null) }
            val outputContentUri = outputUri?.let { ContentUri(it.toString()) }
            when (val exportResult = exportLibraryUseCase(outputContentUri)) {
                is ExportLibraryResult.Success -> {
                    Timber.tag(Constants.LOG_TAG_SETTINGS_VIEW_MODEL).i("event=export_result status=success")
                    _uiState.update { it.copy(isExporting = false, exportSuccess = true) }
                }
                is ExportLibraryResult.Failure -> {
                    Timber.tag(Constants.LOG_TAG_SETTINGS_VIEW_MODEL).w("event=export_result status=failure error=${exportResult.error}")
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportError = exportResult.error.toUserMessage()
                        )
                    }
                }
            }
        }
    }
    
    fun importLibrary(inputUri: Uri, replaceExisting: Boolean = false) {
        viewModelScope.launch {
            Timber.tag(Constants.LOG_TAG_SETTINGS_VIEW_MODEL).i("event=import_requested replaceExisting=$replaceExisting")
            _uiState.update { it.copy(isImporting = true, importError = null) }
            when (val importResult = importLibraryUseCase(ContentUri(inputUri.toString()), replaceExisting)) {
                is ImportLibraryResult.Success -> {
                    Timber.tag(Constants.LOG_TAG_SETTINGS_VIEW_MODEL)
                        .i("event=import_result status=success imported=${importResult.result.importedCount} failed=${importResult.result.failedCount}")
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importSuccess = true,
                            importResult = importResult.result
                        )
                    }
                }
                is ImportLibraryResult.Failure -> {
                    Timber.tag(Constants.LOG_TAG_SETTINGS_VIEW_MODEL).w("event=import_result status=failure error=${importResult.error}")
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importError = importResult.error.toUserMessage()
                        )
                    }
                }
            }
        }
    }
    
    fun clearExportStatus() {
        _uiState.update { it.copy(exportSuccess = false, exportError = null) }
    }
    
    fun clearImportStatus() {
        _uiState.update { it.copy(importSuccess = false, importError = null, importResult = null) }
    }

    fun onReportBug(includeDiagnostics: Boolean = true) {
        viewModelScope.launch {
            Timber.tag(Constants.LOG_TAG_SETTINGS_VIEW_MODEL).i("event=bug_report_requested includeDiagnostics=$includeDiagnostics")
            if (!includeDiagnostics) {
                Timber.tag(Constants.LOG_TAG_SETTINGS_VIEW_MODEL).i("event=bug_report_result status=no_diagnostics_requested")
                _effect.send(SettingsEffect.OpenBugReportShare(Constants.BUG_REPORT_SUBJECT, null))
                return@launch
            }

            when (val packageResult = bugReportLogPackager.packageLogsAsHtmlZip()) {
                is BugReportLogPackagerResult.Success -> {
                    Timber.tag(Constants.LOG_TAG_SETTINGS_VIEW_MODEL)
                        .i("event=bug_report_result status=diagnostics_packaged attachmentPath=${packageResult.zipFile.absolutePath}")
                    _effect.send(
                        SettingsEffect.OpenBugReportShare(
                            subject = Constants.BUG_REPORT_SUBJECT,
                            attachmentFilePath = packageResult.zipFile.absolutePath
                        )
                    )
                }
                is BugReportLogPackagerResult.NoLogsAvailable -> {
                    Timber.tag(Constants.LOG_TAG_SETTINGS_VIEW_MODEL).i("event=bug_report_result status=no_logs_available")
                    _effect.send(SettingsEffect.OpenBugReportShare(Constants.BUG_REPORT_SUBJECT, null))
                }
                is BugReportLogPackagerResult.Failure -> {
                    Timber.tag(Constants.LOG_TAG_SETTINGS_VIEW_MODEL)
                        .w(packageResult.throwable, "Failed to package diagnostics for bug report")
                    _effect.send(SettingsEffect.OpenBugReportShare(Constants.BUG_REPORT_SUBJECT, null))
                }
            }
        }
    }

    fun onGiveFeedback() {
        viewModelScope.launch {
            _effect.send(SettingsEffect.OpenEmailClient(Constants.FEEDBACK_SUBJECT))
        }
    }

    fun onRequestFeature() {
        viewModelScope.launch {
            _effect.send(SettingsEffect.OpenEmailClient(Constants.FEATURE_REQUEST_SUBJECT))
        }
    }

    fun onOpenPrivacyPolicy() {
        viewModelScope.launch {
            _effect.send(SettingsEffect.OpenPrivacyPolicy)
        }
    }

    fun onOpenEULA() {
        viewModelScope.launch {
            _effect.send(SettingsEffect.OpenEULA)
        }
    }
}

data class SettingsUiText(
    @StringRes val resId: Int,
    val formatArgs: List<Any> = emptyList()
)

private fun ExportLibraryError.toUserMessage(): SettingsUiText = when (this) {
    is ExportLibraryError.BackupCreationFailed -> when (error) {
        BackupManagerError.InputStreamUnavailable,
        BackupManagerError.OutputStreamUnavailable -> SettingsUiText(R.string.settings_error_file_access_unavailable)
        BackupManagerError.ExternalFilesDirectoryUnavailable -> SettingsUiText(R.string.settings_error_storage_unavailable)
        BackupManagerError.BackupJsonMissing -> SettingsUiText(R.string.settings_error_backup_invalid)
        is BackupManagerError.UnsafeZipEntry -> SettingsUiText(R.string.settings_error_backup_unsafe)
        is BackupManagerError.Unexpected -> SettingsUiText(R.string.settings_error_unexpected)
    }
    is ExportLibraryError.Unexpected -> SettingsUiText(R.string.settings_error_unexpected)
}

private fun ImportLibraryError.toUserMessage(): SettingsUiText = when (this) {
    is ImportLibraryError.BackupExtractionFailed -> when (error) {
        BackupManagerError.InputStreamUnavailable,
        BackupManagerError.OutputStreamUnavailable -> SettingsUiText(R.string.settings_error_file_access_unavailable)
        BackupManagerError.ExternalFilesDirectoryUnavailable -> SettingsUiText(R.string.settings_error_storage_unavailable)
        BackupManagerError.BackupJsonMissing -> SettingsUiText(R.string.settings_error_backup_invalid)
        is BackupManagerError.UnsafeZipEntry -> SettingsUiText(R.string.settings_error_backup_unsafe)
        is BackupManagerError.Unexpected -> SettingsUiText(R.string.settings_error_unexpected)
    }
    is ImportLibraryError.UnsupportedBackupVersion -> SettingsUiText(R.string.settings_error_backup_unsupported_version)
    is ImportLibraryError.Unexpected -> SettingsUiText(R.string.settings_error_unexpected)
}

data class SettingsUiState(
    val selectedTheme: AppTheme = AppTheme.FOREST_FIBER,
    val themeColors: List<ThemeColor> = emptyList(),
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val exportError: SettingsUiText? = null,
    val isImporting: Boolean = false,
    val importSuccess: Boolean = false,
    val importError: SettingsUiText? = null,
    val importResult: dev.harrisonsoftware.stitchCounter.domain.usecase.ImportResult? = null
)

sealed interface SettingsEffect {
    data class OpenEmailClient(val subject: String) : SettingsEffect
    data class OpenBugReportShare(val subject: String, val attachmentFilePath: String?) : SettingsEffect
    object OpenPrivacyPolicy : SettingsEffect
    object OpenEULA : SettingsEffect
}
