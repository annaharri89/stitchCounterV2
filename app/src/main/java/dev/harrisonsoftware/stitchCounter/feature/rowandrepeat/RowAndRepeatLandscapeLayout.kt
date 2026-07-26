package dev.harrisonsoftware.stitchCounter.feature.rowandrepeat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.BottomActionButtons
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.CounterTopBar
import dev.harrisonsoftware.stitchCounter.ui.theme.StitchCounterV3Theme

private const val LANDSCAPE_REPEAT_PANEL_WEIGHT = 0.4f
private const val LANDSCAPE_ROW_PANEL_WEIGHT = 0.6f

@Composable
fun RowAndRepeatLandscapeLayout(
    state: RowAndRepeatUiState,
    actions: RowAndRepeatActions,
    topBarContent: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CounterTopBar(
            title = state.title,
            topBarContent = topBarContent,
            compact = true
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(LANDSCAPE_REPEAT_PANEL_WEIGHT)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RepeatSummaryCard(
                    repeatCount = state.repeatCount,
                    repeatGoal = state.repeatGoal,
                    rowsPerRepeat = state.rowsPerRepeat,
                    repeatProgress = state.repeatProgress,
                    compact = true
                )

                RepeatCorrectionControl(
                    onIncrementRepeat = actions::incrementRepeat,
                    onDecrementRepeat = actions::decrementRepeat,
                    compact = true
                )
            }

            RowCounter(
                modifier = Modifier
                    .weight(LANDSCAPE_ROW_PANEL_WEIGHT)
                    .fillMaxHeight(),
                label = stringResource(R.string.row_and_repeat_row_label),
                rowCount = state.rowCount,
                rowsPerRepeat = state.rowsPerRepeat,
                onIncrement = actions::incrementRow,
                onDecrement = actions::decrementRow,
                onResetRow = actions::resetRow,
                compact = true
            )
        }

        BottomActionButtons(
            onResetAll = actions::resetAll,
            labelText = stringResource(R.string.action_reset_all)
        )
    }
}

@Preview(name = "Landscape", widthDp = 800, heightDp = 360, showBackground = true)
@Composable
private fun RowAndRepeatLandscapePreview() {
    StitchCounterV3Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            RowAndRepeatLandscapeLayout(
                state = RowAndRepeatUiState(
                    title = "Cable Repeat Scarf",
                    rowCount = 4,
                    rowsPerRepeat = 8,
                    repeatCount = 3,
                    repeatGoal = 20
                ),
                actions = RowAndRepeatActions.NoOp
            )
        }
    }
}
