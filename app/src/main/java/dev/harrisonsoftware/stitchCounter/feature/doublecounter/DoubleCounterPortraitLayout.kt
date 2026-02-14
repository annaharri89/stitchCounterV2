package dev.harrisonsoftware.stitchCounter.feature.doublecounter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import dev.harrisonsoftware.stitchCounter.domain.model.AdjustmentAmount
import dev.harrisonsoftware.stitchCounter.domain.model.CounterState
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.BottomActionButtons
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.CounterTopBar
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.CounterView
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.RowProgressWithLabel
import dev.harrisonsoftware.stitchCounter.ui.theme.StitchCounterV3Theme

interface DoubleCounterActions {
    fun increment(type: CounterType)
    fun decrement(type: CounterType)
    fun reset(type: CounterType)
    fun changeAdjustment(type: CounterType, value: AdjustmentAmount)
    fun resetAll()
}

@Composable
fun DoubleCounterPortraitLayout(
    state: DoubleCounterUiState,
    actions: DoubleCounterActions,
    topBarContent: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CounterTopBar(
            title = state.title,
            topBarContent = topBarContent
        )
        RowProgressWithLabel(
            currentRowCount = state.rowCounterState.count,
            totalRows = state.totalRows,
            modifier = Modifier.fillMaxWidth()
        )

        CounterView(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.counter_type_stitches),
            count = state.stitchCounterState.count,
            selectedAdjustmentAmount = state.stitchCounterState.adjustment,
            onIncrement = { actions.increment(CounterType.STITCH) },
            onDecrement = { actions.decrement(CounterType.STITCH) },
            onReset = { actions.reset(CounterType.STITCH) },
            onAdjustmentClick = { actions.changeAdjustment(CounterType.STITCH, it) }
        )

        CounterView(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.counter_type_rows_rounds),
            count = state.rowCounterState.count,
            selectedAdjustmentAmount = state.rowCounterState.adjustment,
            onIncrement = { actions.increment(CounterType.ROW) },
            onDecrement = { actions.decrement(CounterType.ROW) },
            onReset = { actions.reset(CounterType.ROW) },
            onAdjustmentClick = { actions.changeAdjustment(CounterType.ROW, it) }
        )
        
        Spacer(modifier = Modifier.weight(1f))

        BottomActionButtons(
            onResetAll = actions::resetAll,
            labelText = stringResource(R.string.action_reset_all)
        )
    }
}

@Preview
@Composable
private fun DoubleCounterPortraitPreview() {
    StitchCounterV3Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val fakeActions = object : DoubleCounterActions {
                override fun increment(type: CounterType) {}
                override fun decrement(type: CounterType) {}
                override fun reset(type: CounterType) {}
                override fun changeAdjustment(type: CounterType, value: AdjustmentAmount) {}
                override fun resetAll() {}
            }
            
            DoubleCounterPortraitLayout(
                state = DoubleCounterUiState(
                    stitchCounterState = CounterState(
                        count = 42,
                        adjustment = AdjustmentAmount.FIVE
                    ),
                    rowCounterState = CounterState(
                        count = 10,
                        adjustment = AdjustmentAmount.ONE
                    ),
                    totalRows = 20
                ),
                actions = fakeActions
            )
        }
    }
}
