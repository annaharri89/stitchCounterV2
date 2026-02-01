package com.example.stitchcounterv3.feature.singleCounter

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    var showResetDialog by remember { mutableStateOf(false) }

    val actions = object : SingleCounterActions {
        override fun increment() = viewModel.increment()
        override fun decrement() = viewModel.decrement()
        override fun resetCount() {
            showResetDialog = true
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
                            FloatingActionButton(
                                onClick = {
                                    onNavigateToDetail(state.id)
                                },
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .size(40.dp),
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Project details",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
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
                            FloatingActionButton(
                                onClick = {
                                    onNavigateToDetail(state.id)
                                },
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .size(40.dp),
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Project details",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else null
                )
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Counter?") },
            text = { Text("Are you sure you want to reset the counter to 0?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetCount()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}