package dev.harrisonsoftware.stitchCounter.feature.singleCounter

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.feature.navigation.RootNavGraph
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.AdaptiveLayout
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.ProjectDetailsFAB
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.ResetConfirmationDialog
import com.ramcosta.composedestinations.annotation.Destination

@RootNavGraph
@Destination
@Composable
fun SingleCounterScreen(
    projectId: Int? = null,
    viewModel: SingleCounterViewModel = hiltViewModel(),
    windowSizeClass: WindowSizeClass,
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
    
    val context = LocalContext.current

    val showResetDialog = remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val actions = remember(viewModel) {
        object : SingleCounterActions {
            override fun increment() = viewModel.increment()
            override fun decrement() = viewModel.decrement()
            override fun resetCount() {
                showResetDialog.value = true
            }
            override fun changeAdjustment(value: dev.harrisonsoftware.stitchCounter.domain.model.AdjustmentAmount) = 
                viewModel.changeAdjustment(value)
            override fun setCustomAdjustmentAmount(value: Int) = viewModel.setCustomAdjustmentAmount(value)
        }
    }

    LaunchedEffect(state.shouldShowCustomAdjustmentTip) {
        if (state.shouldShowCustomAdjustmentTip) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.tip_custom_adjustment)
            )
            viewModel.onCustomAdjustmentTipShown()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
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
