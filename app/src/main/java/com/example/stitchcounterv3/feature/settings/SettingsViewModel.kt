package com.example.stitchcounterv3.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stitchcounterv3.data.repo.ThemePreferencesRepository
import com.example.stitchcounterv3.domain.model.AppTheme
import com.example.stitchcounterv3.feature.theme.LauncherIconManager
import com.example.stitchcounterv3.feature.theme.ThemeColor
import com.example.stitchcounterv3.feature.theme.ThemeManager
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
    private val launcherIconManager: LauncherIconManager
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
}

data class SettingsUiState(
    val selectedTheme: AppTheme = AppTheme.SEA_COTTAGE,
    val themeColors: List<ThemeColor> = emptyList()
)