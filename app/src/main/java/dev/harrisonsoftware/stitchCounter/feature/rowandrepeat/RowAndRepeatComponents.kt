package dev.harrisonsoftware.stitchCounter.feature.rowandrepeat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.IncreaseDecreaseButtons
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.ResizableText
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.rememberCounterButtonClickHandler
import dev.harrisonsoftware.stitchCounter.ui.theme.onQuaternary
import dev.harrisonsoftware.stitchCounter.ui.theme.quaternary

@Composable
internal fun RepeatSummaryCard(
    repeatCount: Int,
    repeatGoal: Int,
    rowsPerRepeat: Int,
    repeatProgress: Float? = null,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val progressDescription = stringResource(
        R.string.cd_row_and_repeat_progress,
        repeatCount,
        repeatGoal
    )
    val cardPadding = if (compact) 10.dp else 16.dp
    val summaryLabelStyle = MaterialTheme.typography.labelMedium.copy(
        fontSize = if (compact) 12.sp else 14.sp
    )
    val countStyle = if (compact) {
        MaterialTheme.typography.headlineLarge
    } else {
        MaterialTheme.typography.displaySmall
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = progressDescription
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding),
            verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = if (compact) Modifier.weight(1f) else Modifier,
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.row_and_repeat_repeat_label),
                            style = summaryLabelStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        RepeatAutoBadge(
                            rowsPerRepeat = rowsPerRepeat,
                            compact = compact
                        )
                    }
                    Text(
                        text = repeatCount.toString(),
                        style = countStyle,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = stringResource(R.string.row_and_repeat_goal_label),
                        style = summaryLabelStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.row_and_repeat_repeat_goal_value, repeatGoal),
                        style = countStyle,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                }
            }
            if (repeatProgress != null) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    progress = { repeatProgress },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RepeatAutoBadge(
    rowsPerRepeat: Int,
    compact: Boolean = false,
) {
    Text(
        text = stringResource(R.string.row_and_repeat_repeat_auto_badge, rowsPerRepeat),
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = if (compact) 10.sp else 13.sp
        ),
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(
                horizontal = if (compact) 6.dp else 8.dp,
                vertical = 2.dp
            )
    )
}

@Composable
internal fun RowCounter(
    label: String,
    rowCount: Int,
    rowsPerRepeat: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onResetRow: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val countDescription = stringResource(R.string.cd_named_current_count, label, rowCount)
    val rowCycleProgress = if (rowsPerRepeat > 0) {
        (rowCount.toFloat() / rowsPerRepeat.toFloat()).coerceIn(0f, 1f)
    } else {
        null
    }
    val rowCycleDescription = stringResource(R.string.cd_row_progress, rowCount, rowsPerRepeat)
    val resetRowLabel = stringResource(R.string.row_and_repeat_reset_row)
    val resetRowDescription = stringResource(R.string.cd_reset_row_count)
    val contentSpacing = if (compact) 4.dp else 8.dp
    val framePadding = if (compact) 12.dp else 20.dp
    val progressHeight = if (compact) 6.dp else 12.dp

    PrimaryTapCounterFrame(
        modifier = modifier,
        innerPadding = framePadding
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(contentSpacing)
        ) {
            Text(
                text = label,
                style = if (compact) {
                    MaterialTheme.typography.titleSmall
                } else {
                    MaterialTheme.typography.titleMedium
                }
            )
            if (rowCycleProgress != null) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(progressHeight)
                        .clip(RoundedCornerShape(6.dp))
                        .semantics { contentDescription = rowCycleDescription },
                    progress = { rowCycleProgress },
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .fillMaxHeight(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        ResizableText(
                            modifier = Modifier
                                .fillMaxSize()
                                .semantics {
                                    contentDescription = countDescription
                                    liveRegion = LiveRegionMode.Polite
                                },
                            text = rowCount.toString(),
                            sizingReferenceText = rowsPerRepeat.toString(),
                            heightRatio = 0.6f,
                            widthRatio = 0.3f,
                            minFontSize = 48f,
                        )
                    }
                    RowCounterResetButton(
                        label = resetRowLabel,
                        contentDescription = resetRowDescription,
                        onClick = onResetRow,
                        compact = compact
                    )
                }
                IncreaseDecreaseButtons(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight(),
                    onIncrement = onIncrement,
                    onDecrement = onDecrement,
                    counterLabel = label,
                    fillAvailableHeight = true,
                )
            }
        }
    }
}

@Composable
private fun RowCounterResetButton(
    label: String,
    contentDescription: String,
    onClick: () -> Unit,
    compact: Boolean = false,
) {
    val onResetClick = rememberCounterButtonClickHandler(onClick)
    val pillShape = RoundedCornerShape(50)
    Button(
        onClick = onResetClick,
        modifier = Modifier
            .sizeIn(minWidth = 48.dp, minHeight = if (compact) 40.dp else 48.dp)
            .semantics { this.contentDescription = contentDescription },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.quaternary,
            contentColor = MaterialTheme.onQuaternary
        ),
        contentPadding = PaddingValues(
            horizontal = if (compact) 12.dp else 16.dp,
            vertical = if (compact) 4.dp else 8.dp
        ),
        shape = pillShape
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = if (compact) 11.sp else 12.sp
            )
        )
    }
}

@Composable
private fun PrimaryTapCounterFrame(
    modifier: Modifier = Modifier,
    innerPadding: androidx.compose.ui.unit.Dp = 20.dp,
    content: @Composable () -> Unit,
) {
    val outerShape = RoundedCornerShape(16.dp)
    val innerShape = RoundedCornerShape(14.dp)
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = primaryColor.copy(alpha = 0.35f), shape = outerShape)
            .padding(5.dp)
            .border(width = 4.dp, color = primaryColor, shape = innerShape)
            .background(color = MaterialTheme.colorScheme.surface, shape = innerShape)
            .padding(innerPadding)
    ) {
        content()
    }
}

@Composable
internal fun RepeatCorrectionControl(
    onIncrementRepeat: () -> Unit,
    onDecrementRepeat: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 16.dp)
    ) {
        DashedCorrectionButton(
            isExpanded = isExpanded,
            onToggle = { isExpanded = !isExpanded },
            compact = compact
        )
        AnimatedVisibility(visible = isExpanded) {
            RepeatCorrectionButtons(
                onIncrementRepeat = onIncrementRepeat,
                onDecrementRepeat = onDecrementRepeat
            )
        }
    }
}

@Composable
private fun DashedCorrectionButton(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    compact: Boolean = false,
) {
    val strokeColor = MaterialTheme.colorScheme.outline
    val shape = RoundedCornerShape(50)
    val label = stringResource(R.string.row_and_repeat_correct_repeat_button)
    val expandedStateDescription =
        stringResource(R.string.cd_row_and_repeat_correct_repeat_expanded)
    val collapsedStateDescription =
        stringResource(R.string.cd_row_and_repeat_correct_repeat_collapsed)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(minHeight = if (compact) 40.dp else 48.dp)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onToggle)
            .drawBehind {
                val strokeWidthPx = 1.5.dp.toPx()
                val dashInterval = 10.dp.toPx()
                val dashGap = 7.dp.toPx()
                drawRoundRect(
                    color = strokeColor,
                    topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f),
                    size = Size(size.width - strokeWidthPx, size.height - strokeWidthPx),
                    cornerRadius = CornerRadius((size.height - strokeWidthPx) / 2f),
                    style = Stroke(
                        width = strokeWidthPx,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(dashInterval, dashGap),
                            0f
                        )
                    )
                )
            }
            .padding(
                horizontal = if (compact) 10.dp else 20.dp,
                vertical = if (compact) 8.dp else 12.dp
            )
            .semantics {
                stateDescription = if (isExpanded) {
                    expandedStateDescription
                } else {
                    collapsedStateDescription
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = if (compact) {
                MaterialTheme.typography.labelMedium
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RepeatCorrectionButtons(
    onIncrementRepeat: () -> Unit,
    onDecrementRepeat: () -> Unit,
) {
    val repeatLabel = stringResource(R.string.row_and_repeat_repeat_label)
    val decreaseDescription = stringResource(R.string.cd_decrease_named_count, repeatLabel)
    val increaseDescription = stringResource(R.string.cd_increase_named_count, repeatLabel)
    val buttonShape = RoundedCornerShape(16.dp)
    val onDecrementRepeatClick = rememberCounterButtonClickHandler(onDecrementRepeat)
    val onIncrementRepeatClick = rememberCounterButtonClickHandler(onIncrementRepeat)

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            modifier = Modifier
                .width(80.dp)
                .sizeIn(minHeight = 56.dp)
                .semantics { contentDescription = decreaseDescription },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.quaternary,
                contentColor = MaterialTheme.onQuaternary
            ),
            contentPadding = PaddingValues(0.dp),
            shape = buttonShape,
            onClick = onDecrementRepeatClick
        ) {
            Text(text = "\u2212", style = MaterialTheme.typography.headlineSmall)
        }
        Button(
            modifier = Modifier
                .width(80.dp)
                .sizeIn(minHeight = 56.dp)
                .semantics { contentDescription = increaseDescription },
            contentPadding = PaddingValues(0.dp),
            shape = buttonShape,
            onClick = onIncrementRepeatClick
        ) {
            Text(text = "+", style = MaterialTheme.typography.headlineSmall)
        }
    }
}
