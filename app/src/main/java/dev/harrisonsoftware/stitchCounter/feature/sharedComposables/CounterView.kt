package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.domain.model.AdjustmentAmount
import dev.harrisonsoftware.stitchCounter.ui.theme.onQuaternary
import dev.harrisonsoftware.stitchCounter.ui.theme.quaternary

@Composable
fun CounterView(
    modifier: Modifier = Modifier,
    label: String? = null,
    count: Int,
    selectedAdjustmentAmount: AdjustmentAmount,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
    onAdjustmentClick: (AdjustmentAmount) -> Unit,
    textPaddingEnd: Float = 24f,
    buttonSpacing: Int = 24,
    buttonShape: RoundedCornerShape = RoundedCornerShape(12.dp),
    incrementFontSize: Int = 50,
    decrementFontSize: Int = 60,
    showResetButton: Boolean = true,
    counterNumberIsVertical: Boolean = false
) {
    val countDescription = if (label != null) {
        stringResource(R.string.cd_named_current_count, label, count)
    } else {
        stringResource(R.string.cd_current_count, count)
    }

    Column(
        modifier = modifier,
    ) {
        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (counterNumberIsVertical) {
            ResizableText(
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = countDescription
                        liveRegion = LiveRegionMode.Polite
                    },
                text = "$count",
                heightRatio = 0.6f,
                widthRatio = 0.3f,
                minFontSize = 48f,
                maxFontSize = 200f
            )
        }

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (!counterNumberIsVertical) {
                ResizableText(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = textPaddingEnd.dp)
                        .semantics {
                            contentDescription = countDescription
                            liveRegion = LiveRegionMode.Polite
                        },
                    text = "$count",
                    heightRatio = 0.6f,
                    widthRatio = 0.3f,
                    minFontSize = 48f,
                    maxFontSize = 200f
                )
            }

            IncreaseDecreaseButtons(
                modifier = Modifier.weight(2f),
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                counterLabel = label,
                buttonSpacing = buttonSpacing,
                buttonShape = buttonShape,
                incrementFontSize = incrementFontSize,
                decrementFontSize = decrementFontSize
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (!showResetButton) Arrangement.SpaceAround else Arrangement.Start
        ) {
            if (showResetButton) {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.quaternary,
                        contentColor = MaterialTheme.onQuaternary
                    ),
                    onClick = { onReset() },
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(stringResource(R.string.action_reset))
                }
            }

            AdjustmentButtons(
                selectedAdjustmentAmount = selectedAdjustmentAmount,
                onAdjustmentClick = onAdjustmentClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
