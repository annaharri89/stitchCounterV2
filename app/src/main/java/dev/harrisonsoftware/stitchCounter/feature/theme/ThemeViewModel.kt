package dev.harrisonsoftware.stitchCounter.feature.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.harrisonsoftware.stitchCounter.data.repo.AppPreferencesRepository
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
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()

    init {
        observeTheme()
        observeForceDarkMode()
        observeForceLightMode()
    }

    private fun observeTheme() {
        viewModelScope.launch {
            appPreferencesRepository.selectedTheme.collect { theme ->
                _uiState.update { currentState -> currentState.copy(selectedTheme = theme) }
            }
        }
    }

    private fun observeForceDarkMode() {
        viewModelScope.launch {
            appPreferencesRepository.forceDarkMode.collect { forceDarkMode ->
                _uiState.update { currentState -> currentState.copy(forceDarkMode = forceDarkMode) }
            }
        }
    }

    private fun observeForceLightMode() {
        viewModelScope.launch {
            appPreferencesRepository.forceLightMode.collect { forceLightMode ->
                _uiState.update { currentState -> currentState.copy(forceLightMode = forceLightMode) }
            }
        }
    }
}

data class ThemeUiState(
    val selectedTheme: AppTheme = AppTheme.FOREST_FIBER,
    val forceDarkMode: Boolean = false,
    val forceLightMode: Boolean = false,
) {
    fun resolveDarkTheme(systemInDarkTheme: Boolean): Boolean = when {
        forceDarkMode -> true
        forceLightMode -> false
        else -> systemInDarkTheme
    }
}
