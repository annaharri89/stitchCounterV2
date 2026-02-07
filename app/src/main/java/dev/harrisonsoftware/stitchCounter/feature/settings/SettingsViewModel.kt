package dev.harrisonsoftware.stitchCounter.feature.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.harrisonsoftware.stitchCounter.data.backup.BackupManager
import dev.harrisonsoftware.stitchCounter.data.repo.ThemePreferencesRepository
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme
import dev.harrisonsoftware.stitchCounter.domain.usecase.ExportLibrary
import dev.harrisonsoftware.stitchCounter.domain.usecase.ImportLibrary
import dev.harrisonsoftware.stitchCounter.feature.theme.LauncherIconManager
import dev.harrisonsoftware.stitchCounter.feature.theme.ThemeColor
import dev.harrisonsoftware.stitchCounter.feature.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferencesRepository: ThemePreferencesRepository,
    private val themeManager: ThemeManager,
    private val launcherIconManager: LauncherIconManager,
    private val exportLibraryUseCase: ExportLibrary,
    private val importLibraryUseCase: ImportLibrary
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeTheme()
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themePreferencesRepository.selectedTheme.collect { theme ->
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
            themePreferencesRepository.setTheme(theme)
            launcherIconManager.updateLauncherIcon(theme)
            themePreferencesRepository.setShouldNavigateToSettings(true)
        }
    }

    fun exportLibrary(outputUri: Uri? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportError = null) }
            exportLibraryUseCase(outputUri).fold(
                onSuccess = { uri ->
                    _uiState.update { it.copy(isExporting = false, exportSuccess = true) }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isExporting = false, 
                            exportError = error.message ?: "Export failed"
                        ) 
                    }
                }
            )
        }
    }
    
    fun importLibrary(inputUri: Uri, replaceExisting: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importError = null) }
            importLibraryUseCase(inputUri, replaceExisting).fold(
                onSuccess = { importResult ->
                    _uiState.update { 
                        it.copy(
                            isImporting = false, 
                            importSuccess = true,
                            importResult = importResult
                        ) 
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isImporting = false, 
                            importError = error.message ?: "Import failed"
                        ) 
                    }
                }
            )
        }
    }
    
    fun clearExportStatus() {
        _uiState.update { it.copy(exportSuccess = false, exportError = null) }
    }
    
    fun clearImportStatus() {
        _uiState.update { it.copy(importSuccess = false, importError = null, importResult = null) }
    }
}

data class SettingsUiState(
    val selectedTheme: AppTheme = AppTheme.SEA_COTTAGE,
    val themeColors: List<ThemeColor> = emptyList(),
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val exportError: String? = null,
    val isImporting: Boolean = false,
    val importSuccess: Boolean = false,
    val importError: String? = null,
    val importResult: dev.harrisonsoftware.stitchCounter.domain.usecase.ImportResult? = null
)