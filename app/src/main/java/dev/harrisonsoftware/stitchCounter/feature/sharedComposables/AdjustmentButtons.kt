package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.Alignment
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.domain.model.AdjustmentAmount

@Composable
fun AdjustmentButtons(
    selectedAdjustmentAmount: AdjustmentAmount,
    customAdjustmentAmount: Int,
    onAdjustmentClick: (AdjustmentAmount) -> Unit,
    onEditCustomAdjustmentClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(AdjustmentAmount.ONE, AdjustmentAmount.FIVE).forEach { amount ->
            val isSelected = amount == selectedAdjustmentAmount
            val amountLabel = if (amount == AdjustmentAmount.ONE) "+1" else "+5"
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
            val adjustmentDescription = if (isSelected) {
                stringResource(R.string.cd_adjustment_amount_selected, amountLabel)
            } else {
                stringResource(R.string.cd_adjustment_amount, amountLabel)
            }
            Button(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .semantics {
                        selected = isSelected
                        role = Role.RadioButton
                        contentDescription = adjustmentDescription
                    },
                colors = buttonColors,
                onClick = { onAdjustmentClick(amount) }
            ) {
                Text(
                    text = amountLabel,
                    maxLines = 1
                )
            }
        }

        val customAmountLabel = "+$customAdjustmentAmount"
        val isCustomSelected = selectedAdjustmentAmount == AdjustmentAmount.CUSTOM
        val customButtonColors = ButtonDefaults.buttonColors(
            containerColor = if (isCustomSelected) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.tertiary
            },
            contentColor = if (isCustomSelected) {
                MaterialTheme.colorScheme.onSecondary
            } else {
                MaterialTheme.colorScheme.onTertiary
            }
        )
        val customAdjustmentDescription = if (isCustomSelected) {
            stringResource(R.string.cd_adjustment_amount_selected, customAmountLabel)
        } else {
            stringResource(R.string.cd_adjustment_amount, customAmountLabel)
        }
        val editCustomAdjustment = stringResource(R.string.cd_edit_custom_adjustment)

        Button(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                .semantics {
                    selected = isCustomSelected
                    role = Role.RadioButton
                    contentDescription = customAdjustmentDescription
                },
            colors = customButtonColors,
            onClick = { onAdjustmentClick(AdjustmentAmount.CUSTOM) }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = customAmountLabel,
                    maxLines = 1
                )
                Icon(
                    modifier = Modifier.clickable {
                        onEditCustomAdjustmentClick.invoke()
                    }.semantics {
                        contentDescription = editCustomAdjustment
                        role = Role.Button
                    },
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )
            }
        }
    }
}
