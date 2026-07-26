package dev.harrisonsoftware.stitchCounter.feature.rowandrepeat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.BottomActionButtons
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.CounterTopBar
import dev.harrisonsoftware.stitchCounter.ui.theme.StitchCounterV3Theme

@Composable
fun RowAndRepeatPortraitLayout(
    state: RowAndRepeatUiState,
    actions: RowAndRepeatActions,
    topBarContent: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CounterTopBar(
            title = state.title,
            topBarContent = topBarContent
        )

        RepeatSummaryCard(
            repeatCount = state.repeatCount,
            repeatGoal = state.repeatGoal,
            rowsPerRepeat = state.rowsPerRepeat,
            repeatProgress = state.repeatProgress,
        )

        RowCounter(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = stringResource(R.string.row_and_repeat_row_label),
            rowCount = state.rowCount,
            rowsPerRepeat = state.rowsPerRepeat,
            onIncrement = actions::incrementRow,
            onDecrement = actions::decrementRow,
            onResetRow = actions::resetRow
        )

        RepeatCorrectionControl(
            onIncrementRepeat = actions::incrementRepeat,
            onDecrementRepeat = actions::decrementRepeat
        )

        BottomActionButtons(
            onResetAll = actions::resetAll,
            labelText = stringResource(R.string.action_reset_all)
        )
    }
}

@Preview
@Composable
private fun RowAndRepeatPortraitPreview() {
    StitchCounterV3Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            RowAndRepeatPortraitLayout(
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
