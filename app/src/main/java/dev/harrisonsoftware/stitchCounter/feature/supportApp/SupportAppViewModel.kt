package dev.harrisonsoftware.stitchCounter.feature.supportApp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.harrisonsoftware.stitchCounter.data.repo.SupportAppPreferencesRepository
import dev.harrisonsoftware.stitchCounter.data.repo.ThemePreferencesRepository
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupportAppViewModel @Inject constructor(
    private val supportAppPreferencesRepository: SupportAppPreferencesRepository,
    private val themePreferencesRepository: ThemePreferencesRepository
) : ViewModel() {

    private val _hasSupported = MutableStateFlow(false)
    val hasSupported: StateFlow<Boolean> = _hasSupported.asStateFlow()

    private val _selectedTheme = MutableStateFlow(AppTheme.SEA_COTTAGE)
    val selectedTheme: StateFlow<AppTheme> = _selectedTheme.asStateFlow()

    init {
        observeSupportState()
        observeTheme()
    }

    private fun observeSupportState() {
        viewModelScope.launch {
            supportAppPreferencesRepository.hasSupported.collect { supported ->
                _hasSupported.value = supported
            }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themePreferencesRepository.selectedTheme.collect { theme ->
                _selectedTheme.value = theme
            }
        }
    }

    fun toggleSupported() {
        viewModelScope.launch {
            val newValue = !_hasSupported.value
            supportAppPreferencesRepository.setHasSupported(newValue)
        }
    }
}
