package dev.harrisonsoftware.stitchCounter.feature.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.harrisonsoftware.stitchCounter.data.repo.ThemePreferencesRepository
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePreferencesRepository: ThemePreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()

    init {
        observeTheme()
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themePreferencesRepository.selectedTheme.collect { theme ->
                _uiState.update { currentState -> currentState.copy(selectedTheme = theme) }
            }
        }
    }
}

data class ThemeUiState(
    val selectedTheme: AppTheme = AppTheme.SEA_COTTAGE
)