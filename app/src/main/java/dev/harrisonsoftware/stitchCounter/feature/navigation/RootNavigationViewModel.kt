package dev.harrisonsoftware.stitchCounter.feature.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RootNavigationViewModel @Inject constructor() : ViewModel() {

    private val _currentSheet = MutableStateFlow<SheetScreen?>(null)
    val currentSheet: StateFlow<SheetScreen?> = _currentSheet

    fun showBottomSheet(sheet: SheetScreen?) {
        _currentSheet.value = sheet
    }

    private val _selectedTab = MutableStateFlow(BottomNavTab.LIBRARY)
    val selectedTab: StateFlow<BottomNavTab> = _selectedTab.asStateFlow()

    fun selectTab(tab: BottomNavTab) {
        _selectedTab.value = tab
    }
}