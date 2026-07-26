package dev.harrisonsoftware.stitchCounter.feature.rowandrepeat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.AdaptiveLayout
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.CounterHapticFeedbackProvider
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.KeepScreenOnEffect
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.LoadFailureContent
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.ProjectDetailsFAB
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.ResetConfirmationDialog

@Composable
fun RowAndRepeat(
    projectId: Int? = null,
    viewModel: RowAndRepeatViewModel = hiltViewModel(),
    isWideLayout: Boolean,
    onNavigateToDetail: ((Int) -> Unit)? = null
) {
    LaunchedEffect(projectId) {
        projectId?.let { viewModel.loadProject(it) } ?: viewModel.resetState()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, projectId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                projectId?.let { viewModel.loadProject(it) }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val loadError = state.loadError
    KeepScreenOnEffect(enabled = state.forceCounterScreensOn)

    val showResetRowDialog = remember { mutableStateOf(false) }
    val showResetAllDialog = remember { mutableStateOf(false) }

    val actions = remember(viewModel) {
        object : RowAndRepeatActions {
            override fun incrementRow() = viewModel.incrementRow()
            override fun decrementRow() = viewModel.decrementRow()
            override fun resetRow() { showResetRowDialog.value = true }
            override fun incrementRepeat() = viewModel.incrementRepeat()
            override fun decrementRepeat() = viewModel.decrementRepeat()
            override fun resetAll() { showResetAllDialog.value = true }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (loadError != null) {
            LoadFailureContent(
                messageResId = loadError,
                onClose = { viewModel.attemptDismissal() },
            )
        } else {
        CounterHapticFeedbackProvider(enabled = state.counterHapticFeedbackEnabled) {
        Box(modifier = Modifier.fillMaxSize()) {
            val topBarContent: (@Composable () -> Unit)? =
                if (state.id > 0 && onNavigateToDetail != null) {
                    {
                        ProjectDetailsFAB(
                            onClick = { onNavigateToDetail(state.id) },
                            compact = isWideLayout
                        )
                    }
                } else {
                    null
                }

            AdaptiveLayout(
                isWideLayout = isWideLayout,
                portraitContent = {
                    RowAndRepeatPortraitLayout(
                        state = state,
                        actions = actions,
                        topBarContent = topBarContent,
                    )
                },
                landscapeContent = {
                    RowAndRepeatLandscapeLayout(
                        state = state,
                        actions = actions,
                        topBarContent = topBarContent,
                    )
                }
            )
        }
        }
        }
    }

    if (showResetRowDialog.value) {
        ResetConfirmationDialog(
            title = stringResource(R.string.row_and_repeat_reset_row_title),
            message = stringResource(R.string.row_and_repeat_reset_row_message),
            onConfirm = {
                viewModel.resetRow()
                showResetRowDialog.value = false
            },
            onDismiss = { showResetRowDialog.value = false }
        )
    }

    if (showResetAllDialog.value) {
        ResetConfirmationDialog(
            title = stringResource(R.string.row_and_repeat_reset_all_title),
            message = stringResource(R.string.row_and_repeat_reset_all_message),
            onConfirm = {
                viewModel.resetAll()
                showResetAllDialog.value = false
            },
            onDismiss = { showResetAllDialog.value = false }
        )
    }
}
