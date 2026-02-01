package com.example.stitchcounterv3.feature.doublecounter

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

@RootNavGraph
@Destination
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun DoubleCounterScreen(
    projectId: Int? = null,
    viewModel: DoubleCounterViewModel = hiltViewModel(),
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

    var resetDialogType by remember { mutableStateOf<CounterType?>(null) }
    var showResetAllDialog by remember { mutableStateOf(false) }

    val actions = object : DoubleCounterActions {
        override fun increment(type: CounterType) = viewModel.increment(type)
        override fun decrement(type: CounterType) = viewModel.decrement(type)
        override fun reset(type: CounterType) {
            resetDialogType = type
        }
        override fun changeAdjustment(type: CounterType, value: com.example.stitchcounterv3.domain.model.AdjustmentAmount) = 
            viewModel.changeAdjustment(type, value)
        override fun resetAll() {
            showResetAllDialog = true
        }
    }

    Surface(
        modifier = Modifier.height(screenHeight * 0.99f)
    ) {
        AdaptiveLayout(
            windowSizeClass = windowSizeClass,
            portraitContent = {
                DoubleCounterPortraitLayout(
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
                DoubleCounterLandscapeLayout(
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

    resetDialogType?.let { type ->
        val counterName = when (type) {
            CounterType.STITCH -> "Stitches"
            CounterType.ROW -> "Rows/Rounds"
        }
        AlertDialog(
            onDismissRequest = { resetDialogType = null },
            title = { Text("Reset $counterName Counter?") },
            text = { Text("Are you sure you want to reset the $counterName counter to 0?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.reset(type)
                        resetDialogType = null
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { resetDialogType = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetAllDialog) {
        AlertDialog(
            onDismissRequest = { showResetAllDialog = false },
            title = { Text("Reset All Counters?") },
            text = { Text("Are you sure you want to reset both Stitches and Rows/Rounds counters to 0?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAll()
                        showResetAllDialog = false
                    }
                ) {
                    Text("Reset All")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetAllDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}


