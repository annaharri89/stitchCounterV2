package com.example.stitchcounterv3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.example.stitchcounterv3.data.repo.ThemePreferencesRepository
import com.example.stitchcounterv3.domain.model.AppTheme
import com.example.stitchcounterv3.feature.navigation.RootNavigationScreen
import com.example.stitchcounterv3.feature.navigation.RootNavigationViewModel
import com.example.stitchcounterv3.feature.theme.LauncherIconManager
import com.example.stitchcounterv3.feature.theme.ThemeViewModel
import com.example.stitchcounterv3.ui.theme.StitchCounterV3Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferencesRepository: ThemePreferencesRepository

    @Inject
    lateinit var launcherIconManager: LauncherIconManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize launcher icon on first launch (when savedInstanceState is null)
        if (savedInstanceState == null) {
            initializeLauncherIcon()
        }
        
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val rootNavigationViewModel: RootNavigationViewModel = hiltViewModel()
            val themeUiState by themeViewModel.uiState.collectAsState()
            
            StitchCounterV3Theme(theme = themeUiState.selectedTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RootNavigationScreen(viewModel = rootNavigationViewModel)
                }
            }
        }
    }

    /**
     * Initializes the launcher icon to match the current theme on app startup.
     * Reads the saved theme from DataStore and ensures the correct icon alias is enabled.
     */
    private fun initializeLauncherIcon() {
        lifecycleScope.launch {
            try {
                val currentTheme = themePreferencesRepository.selectedTheme.first()
                launcherIconManager.updateLauncherIcon(currentTheme)
            } catch (e: Exception) {
                // If there's an error reading the theme, default to Sea Cottage
                launcherIconManager.updateLauncherIcon(AppTheme.SEA_COTTAGE)
            }
        }
    }
}