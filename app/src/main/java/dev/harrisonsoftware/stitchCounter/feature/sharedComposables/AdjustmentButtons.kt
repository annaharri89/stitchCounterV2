package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.harrisonsoftware.stitchCounter.domain.model.AdjustmentAmount

@Composable
fun AdjustmentButtons(
    selectedAdjustmentAmount: AdjustmentAmount,
    onAdjustmentClick: (AdjustmentAmount) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AdjustmentAmount.entries.forEach { amount ->
            val isSelected = amount == selectedAdjustmentAmount
            val buttonColors = ButtonDefaults.buttonColors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.tertiary
                },
                contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onSecondary
                } else {
                    MaterialTheme.colorScheme.onTertiary
                },
            )
            Button(
                modifier = Modifier
                    .padding(horizontal = 4.dp),
                colors = buttonColors,
                onClick = { onAdjustmentClick(amount) }
            ) {
                Text(
                    text = amount.text,
                    maxLines = 1
                )
            }
        }
    }
}