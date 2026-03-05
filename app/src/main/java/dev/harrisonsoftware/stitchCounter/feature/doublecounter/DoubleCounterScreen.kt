package dev.harrisonsoftware.stitchCounter.feature.doublecounter

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
fun DoubleCounterScreen(
    projectId: Int? = null,
    viewModel: DoubleCounterViewModel = hiltViewModel(),
    isWideLayout: Boolean,
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

    val resetDialogType = remember { mutableStateOf<CounterType?>(null) }
    val showResetAllDialog = remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val actions = remember(viewModel) {
        object : DoubleCounterActions {
            override fun increment(type: CounterType) = viewModel.increment(type)
            override fun decrement(type: CounterType) = viewModel.decrement(type)
            override fun reset(type: CounterType) {
                resetDialogType.value = type
            }
            override fun changeAdjustment(type: CounterType, value: dev.harrisonsoftware.stitchCounter.domain.model.AdjustmentAmount) =
                viewModel.changeAdjustment(type, value)
            override fun setCustomAdjustmentAmount(type: CounterType, value: Int) =
                viewModel.setCustomAdjustmentAmount(type, value)
            override fun resetAll() {
                showResetAllDialog.value = true
            }
            override fun showCustomAdjustmentDialog(type: CounterType) = viewModel.showCustomAdjustmentDialog(type)
            override fun dismissCustomAdjustmentDialog() = viewModel.dismissCustomAdjustmentDialog()
            override fun updateCustomAdjustmentDialogInput(input: String) = viewModel.updateCustomAdjustmentDialogInput(input)
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
                isWideLayout = isWideLayout,
                portraitContent = {
                    DoubleCounterPortraitLayout(
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
                    DoubleCounterLandscapeLayout(
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

    resetDialogType.value?.let { type ->
        val counterName = when (type) {
            CounterType.STITCH -> stringResource(R.string.counter_type_stitches)
            CounterType.ROW -> stringResource(R.string.counter_type_rows_rounds)
        }
        ResetConfirmationDialog(
            title = stringResource(R.string.reset_named_counter_title, counterName),
            message = stringResource(R.string.reset_named_counter_message, counterName),
            onConfirm = {
                viewModel.reset(type)
                resetDialogType.value = null
            },
            onDismiss = { resetDialogType.value = null }
        )
    }

    if (showResetAllDialog.value) {
        ResetConfirmationDialog(
            title = stringResource(R.string.reset_all_counters_title),
            message = stringResource(R.string.reset_all_counters_message),
            onConfirm = {
                viewModel.resetAll()
                showResetAllDialog.value = false
            },
            onDismiss = { showResetAllDialog.value = false }
        )
    }
}
