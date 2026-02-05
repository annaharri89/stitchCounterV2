package com.example.stitchcounterv3.feature.singleCounter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.stitchcounterv3.ui.theme.StitchCounterV3Theme

interface SingleCounterActions {
    fun increment()
    fun decrement()
    fun resetCount()
    fun changeAdjustment(value: AdjustmentAmount)
}

@Composable
fun SingleCounterPortraitLayout(
    state: SingleCounterUiState,
    actions: SingleCounterActions,
    topBarContent: (@Composable () -> Unit)? = null
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
        CounterView(
            modifier = Modifier.weight(1f),
            count = state.counterState.count,
            selectedAdjustmentAmount = state.counterState.adjustment,
            onIncrement = actions::increment,
            onDecrement = actions::decrement,
            onAdjustmentClick = actions::changeAdjustment,
            onReset = actions::resetCount,
            showResetButton = false,
            counterNumberIsVertical = true
        )
        
        Spacer(modifier = Modifier.weight(.5f))

        BottomActionButtons(
            onResetAll = actions::resetCount
        )
    }
}

@Preview
@Composable
private fun SingleCounterPortraitPreview() {
    StitchCounterV3Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val fakeActions = object : SingleCounterActions {
                override fun increment() {}
                override fun decrement() {}
                override fun resetCount() {}
                override fun changeAdjustment(value: AdjustmentAmount) {}
            }
            
            SingleCounterPortraitLayout(
                state = SingleCounterUiState(
                    counterState = CounterState(
                        count = 42,
                        adjustment = AdjustmentAmount.FIVE
                    )
                ),
                actions = fakeActions
            )
        }
    }
}