package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.harrisonsoftware.stitchCounter.R
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
            val adjustmentDescription = if (isSelected) {
                stringResource(R.string.cd_adjustment_amount_selected, amount.text)
            } else {
                stringResource(R.string.cd_adjustment_amount, amount.text)
            }

            AppButton(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .semantics {
                        selected = isSelected
                        role = Role.RadioButton
                        contentDescription = adjustmentDescription
                    },
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
                onClick = { onAdjustmentClick(amount) },
            ) {
                Text(
                    text = amount.text,
                    maxLines = 1
                )
            }
        }
    }
}
