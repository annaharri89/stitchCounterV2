package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    customAdjustmentAmount: Int,
    onAdjustmentClick: (AdjustmentAmount) -> Unit,
    onEditCustomAdjustmentClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val editCustomAdjustmentDescription = stringResource(R.string.cd_edit_custom_adjustment)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        AdjustmentAmount.entries.forEach { amount ->
            val label = when (amount) {
                AdjustmentAmount.ONE -> "+1"
                AdjustmentAmount.FIVE -> "+5"
                AdjustmentAmount.CUSTOM -> "+$customAdjustmentAmount"
            }
            AdjustmentButton(
                label = label,
                isSelected = amount == selectedAdjustmentAmount,
                onClick = { onAdjustmentClick(amount) },
                trailingIcon = if (amount == AdjustmentAmount.CUSTOM) {
                    {
                        Icon(
                            modifier = Modifier
                                .clickable { onEditCustomAdjustmentClick() }
                                .semantics {
                                    contentDescription = editCustomAdjustmentDescription
                                    role = Role.Button
                                },
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
private fun AdjustmentButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
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
        stringResource(R.string.cd_adjustment_amount_selected, label)
    } else {
        stringResource(R.string.cd_adjustment_amount, label)
    }

    Button(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .sizeIn(minWidth = 40.dp, minHeight = 40.dp)
            .semantics {
                selected = isSelected
                role = Role.RadioButton
                contentDescription = adjustmentDescription
            },
        colors = buttonColors,
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = label, maxLines = 1)
            trailingIcon?.invoke()
        }
    }
}
