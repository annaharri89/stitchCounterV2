package com.example.stitchcounterv3.feature.doublecounter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.stitchcounterv3.domain.model.AdjustmentAmount
import com.example.stitchcounterv3.domain.model.CounterState
import com.example.stitchcounterv3.feature.sharedComposables.BottomActionButtons
import com.example.stitchcounterv3.feature.sharedComposables.CounterTopBar
import com.example.stitchcounterv3.feature.sharedComposables.CounterView
import com.example.stitchcounterv3.feature.sharedComposables.RowProgressIndicator
import com.example.stitchcounterv3.ui.theme.StitchCounterV3Theme

@Composable
fun DoubleCounterLandscapeLayout(
    state: DoubleCounterUiState,
    actions: DoubleCounterActions,
    topBarContent: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CounterTopBar(
            title = state.title,
            topBarContent = topBarContent
        )
        RowProgressIndicator(
            progress = state.rowProgress
        )

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CounterView(
                modifier = Modifier.weight(1f),
                label = "Stitches",
                count = state.stitchCounterState.count,
                selectedAdjustmentAmount = state.stitchCounterState.adjustment,
                onIncrement = { actions.increment(CounterType.STITCH) },
                onDecrement = { actions.decrement(CounterType.STITCH) },
                onAdjustmentClick = { actions.changeAdjustment(CounterType.STITCH, it) },
                onReset = { actions.reset(CounterType.STITCH) }
            )

            CounterView(
                modifier = Modifier.weight(1f),
                label = "Rows/Rounds",
                count = state.rowCounterState.count,
                selectedAdjustmentAmount = state.rowCounterState.adjustment,
                onIncrement = { actions.increment(CounterType.ROW) },
                onDecrement = { actions.decrement(CounterType.ROW) },
                onAdjustmentClick = { actions.changeAdjustment(CounterType.ROW, it) },
                onReset = { actions.reset(CounterType.ROW) }
            )
        }

        BottomActionButtons(
            onResetAll = actions::resetAll,
            labelText = "Reset All"
        )
    }
}

@Preview
@Composable
private fun DoubleCounterLandscapePreview() {
    StitchCounterV3Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val fakeActions = object : DoubleCounterActions {
                override fun increment(type: CounterType) {}
                override fun decrement(type: CounterType) {}
                override fun reset(type: CounterType) {}
                override fun changeAdjustment(type: CounterType, value: AdjustmentAmount) {}
                override fun resetAll() {}
            }
            
            DoubleCounterLandscapeLayout(
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