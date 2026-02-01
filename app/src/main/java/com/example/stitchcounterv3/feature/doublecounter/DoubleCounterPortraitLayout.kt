package com.example.stitchcounterv3.feature.doublecounter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.stitchcounterv3.domain.model.AdjustmentAmount
import com.example.stitchcounterv3.domain.model.CounterState
import com.example.stitchcounterv3.feature.sharedComposables.BottomActionButtons
import com.example.stitchcounterv3.feature.sharedComposables.CounterView
import com.example.stitchcounterv3.feature.sharedComposables.RowProgressIndicator
import com.example.stitchcounterv3.ui.theme.StitchCounterV3Theme

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
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.title.isNotEmpty() || topBarContent != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.title.isNotEmpty()) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                topBarContent?.invoke()
            }
        }
        RowProgressIndicator(
            progress = state.rowProgress
        )

        CounterView(
            modifier = Modifier.weight(1f),
            label = "Stitches",
            count = state.stitchCounterState.count,
            selectedAdjustmentAmount = state.stitchCounterState.adjustment,
            onIncrement = { actions.increment(CounterType.STITCH) },
            onDecrement = { actions.decrement(CounterType.STITCH) },
            onReset = { actions.reset(CounterType.STITCH) },
            onAdjustmentClick = { actions.changeAdjustment(CounterType.STITCH, it) }
        )

        CounterView(
            modifier = Modifier.weight(1f),
            label = "Rows/Rounds",
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
            labelText = "Reset All"
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