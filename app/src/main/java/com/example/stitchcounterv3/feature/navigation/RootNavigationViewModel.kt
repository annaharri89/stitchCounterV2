package com.example.stitchcounterv3.feature.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stitchcounterv3.data.repo.ThemePreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootNavigationViewModel @Inject constructor(
    private val themePreferencesRepository: ThemePreferencesRepository
) : ViewModel() {
    

    private val _currentSheet = MutableStateFlow<SheetScreen?>(null)
    val currentSheet: StateFlow<SheetScreen?> = _currentSheet

    fun showBottomSheet(sheet: SheetScreen?) {
        _currentSheet.value = sheet
    }

    private val _selectedTab = MutableStateFlow(BottomNavTab.HOME)
    val selectedTab: StateFlow<BottomNavTab> = _selectedTab.asStateFlow()

    init {
        checkShouldNavigateToSettings()
    }

    private fun checkShouldNavigateToSettings() {
        viewModelScope.launch {
            val shouldNavigate = themePreferencesRepository.checkAndClearShouldNavigateToSettings()
            if (shouldNavigate) {
                _selectedTab.value = BottomNavTab.SETTINGS
            }
        }
    }

    fun selectTab(tab: BottomNavTab) {
        _selectedTab.value = tab
    }
}