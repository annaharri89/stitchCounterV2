package dev.harrisonsoftware.stitchCounter.feature.singleCounter

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.unit.dp
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.activity.ComponentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.feature.navigation.RootNavGraph
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.AdaptiveLayout
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.ProjectDetailsFAB
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.ResetConfirmationDialog
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

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, projectId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                projectId?.let {
                    viewModel.loadProject(it)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    val activity = LocalContext.current as ComponentActivity
    val windowSizeClass = calculateWindowSizeClass(activity)

    val showResetDialog = remember { mutableStateOf(false) }

    val actions = object : SingleCounterActions {
        override fun increment() = viewModel.increment()
        override fun decrement() = viewModel.decrement()
        override fun resetCount() {
            showResetDialog.value = true
        }
        override fun changeAdjustment(value: dev.harrisonsoftware.stitchCounter.domain.model.AdjustmentAmount) = 
            viewModel.changeAdjustment(value)
    }

    Surface(
        modifier = Modifier.fillMaxSize()
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
            title = stringResource(R.string.reset_counter_title),
            message = stringResource(R.string.reset_counter_message),
            onConfirm = {
                viewModel.resetCount()
                showResetDialog.value = false
            },
            onDismiss = { showResetDialog.value = false }
        )
    }
}
