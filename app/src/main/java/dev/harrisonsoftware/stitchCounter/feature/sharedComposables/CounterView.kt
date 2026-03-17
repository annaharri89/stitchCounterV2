package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
    customAdjustmentAmount: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
    onAdjustmentClick: (AdjustmentAmount) -> Unit,
    onCustomAdjustmentAmountChange: (Int) -> Unit,
    customAdjustmentDialogState: CustomAdjustmentDialogState = CustomAdjustmentDialogState(),
    onShowCustomAdjustmentDialog: () -> Unit,
    onDismissCustomAdjustmentDialog: () -> Unit,
    onCustomAdjustmentDialogInputChange: (String) -> Unit,
    textPaddingEnd: Float = 24f,
    buttonSpacing: Int = 24,
    buttonShape: RoundedCornerShape = RoundedCornerShape(12.dp),
    showResetButton: Boolean = true,
    counterNumberIsVertical: Boolean = false,
    increaseDecreaseButtonsHeightFillFraction: Float = 1f
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val countDescription = if (label != null) {
        stringResource(R.string.cd_named_current_count, label, count)
    } else {
        stringResource(R.string.cd_current_count, count)
    }
    val customAmount = customAdjustmentAmount.coerceAtLeast(1)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
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
                    minFontSize = 48f
                )
            }

            IncreaseDecreaseButtons(
                modifier = Modifier.weight(2f),
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                counterLabel = label,
                buttonSpacing = buttonSpacing,
                buttonShape = buttonShape,
                maxHeightFillFraction = increaseDecreaseButtonsHeightFillFraction
            )
        }

        Spacer(Modifier.weight(.2f))

        Row(
            modifier = Modifier.fillMaxWidth().weight(.5f),
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
                customAdjustmentAmount = customAmount,
                onEditCustomAdjustmentClick = onShowCustomAdjustmentDialog,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (customAdjustmentDialogState.isVisible) {
        AlertDialog(
            onDismissRequest = onDismissCustomAdjustmentDialog,
            title = { Text(text = stringResource(R.string.title_set_custom_adjustment)) },
            text = {
                OutlinedTextField(
                    value = customAdjustmentDialogState.input,
                    onValueChange = { input ->
                        onCustomAdjustmentDialogInputChange(input.filter { it.isDigit() }.take(4))
                    },
                    singleLine = true,
                    label = { Text(text = stringResource(R.string.label_custom_adjustment)) },
                    placeholder = { Text(text = stringResource(R.string.placeholder_custom_adjustment)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val parsedAmount = customAdjustmentDialogState.input.toIntOrNull()
                            if (parsedAmount != null && parsedAmount > 0) {
                                onCustomAdjustmentAmountChange(parsedAmount)
                                keyboardController?.hide()
                                onDismissCustomAdjustmentDialog()
                            }
                        }
                    ),
                    isError = customAdjustmentDialogState.input.toIntOrNull()?.let { it <= 0 } ?: customAdjustmentDialogState.input.isNotBlank(),
                    supportingText = {
                        if (customAdjustmentDialogState.input.toIntOrNull()?.let { it <= 0 } == true) {
                            Text(text = stringResource(R.string.error_custom_adjustment_invalid))
                        }
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsedAmount = customAdjustmentDialogState.input.toIntOrNull()
                        if (parsedAmount != null && parsedAmount > 0) {
                            onCustomAdjustmentAmountChange(parsedAmount)
                            onDismissCustomAdjustmentDialog()
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissCustomAdjustmentDialog) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
