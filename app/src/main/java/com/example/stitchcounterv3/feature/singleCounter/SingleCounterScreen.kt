package com.example.stitchcounterv3.feature.singleCounter

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.activity.ComponentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stitchcounterv3.feature.navigation.RootNavGraph
import com.example.stitchcounterv3.feature.sharedComposables.AdaptiveLayout
import com.example.stitchcounterv3.feature.sharedComposables.ProjectDetailsFAB
import com.example.stitchcounterv3.feature.sharedComposables.ResetConfirmationDialog
import com.ramcosta.composedestinations.annotation.Destination

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@RootNavGraph
@Destination
@Composable
fun SingleCounterScreen(
    projectId: Int? = null,
    viewModel: SingleCounterViewModel = hiltViewModel(),
    onNavigateToDetail: ((Int) -> Unit)? = null
) {
    LaunchedEffect(projectId) {
        projectId?.let {
            viewModel.loadProject(projectId)
        } ?: run {
            viewModel.resetState()
        }
    }
    
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    val activity = LocalContext.current as ComponentActivity
    val windowSizeClass = calculateWindowSizeClass(activity)
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val showResetDialog = remember { mutableStateOf(false) }

    val actions = object : SingleCounterActions {
        override fun increment() = viewModel.increment()
        override fun decrement() = viewModel.decrement()
        override fun resetCount() {
            showResetDialog.value = true
        }
        override fun changeAdjustment(value: com.example.stitchcounterv3.domain.model.AdjustmentAmount) = 
            viewModel.changeAdjustment(value)
    }

    Surface(
        modifier = Modifier.height(screenHeight * 0.99f)
    ) {
        AdaptiveLayout(
            windowSizeClass = windowSizeClass,
            portraitContent = {
                SingleCounterPortraitLayout(
                    state = state,
                    actions = actions,
                    topBarContent = if (state.id > 0 && onNavigateToDetail != null) {
                        {
                            ProjectDetailsFAB(
                                onClick = { onNavigateToDetail(state.id) }
                            )
                        }
                    } else null
                )
            },
            landscapeContent = {
                SingleCounterLandscapeLayout(
                    state = state,
                    actions = actions,
                    topBarContent = if (state.id > 0 && onNavigateToDetail != null) {
                        {
                            ProjectDetailsFAB(
                                onClick = { onNavigateToDetail(state.id) }
                            )
                        }
                    } else null
                )
            }
        )
    }

    if (showResetDialog.value) {
        ResetConfirmationDialog(
            title = "Reset Counter?",
            message = "Are you sure you want to reset the counter to 0?",
            onConfirm = {
                viewModel.resetCount()
                showResetDialog.value = false
            },
            onDismiss = { showResetDialog.value = false }
        )
    }
}