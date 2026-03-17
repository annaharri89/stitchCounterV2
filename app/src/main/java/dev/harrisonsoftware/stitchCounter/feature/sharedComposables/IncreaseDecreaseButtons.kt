package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.ui.theme.onQuaternary
import dev.harrisonsoftware.stitchCounter.ui.theme.quaternary

@Composable
fun IncreaseDecreaseButtons(
    modifier: Modifier = Modifier,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    counterLabel: String? = null,
    buttonSpacing: Int = 24,
    buttonShape: RoundedCornerShape = RoundedCornerShape(12.dp),
    maxHeightFillFraction: Float = 1f
) {
    val decreaseDescription = if (counterLabel != null) {
        stringResource(R.string.cd_decrease_named_count, counterLabel)
    } else {
        stringResource(R.string.cd_decrease_count)
    }
    val increaseDescription = if (counterLabel != null) {
        stringResource(R.string.cd_increase_named_count, counterLabel)
    } else {
        stringResource(R.string.cd_increase_count)
    }

    BoxWithConstraints(modifier = modifier.fillMaxHeight()) {
        val buttonSpacingDp = buttonSpacing.dp
        val availableButtonWidth = ((maxWidth - buttonSpacingDp).coerceAtLeast(0.dp)) / 2
        val maximumAllowedHeight = maxHeight * maxHeightFillFraction.coerceIn(0f, 1f)
        val rowHeight = maximumAllowedHeight.coerceAtMost(availableButtonWidth)

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(rowHeight),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacingDp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .semantics { contentDescription = decreaseDescription },
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = MaterialTheme.quaternary,
                    contentColor = MaterialTheme.onQuaternary
                ),
                contentPadding = PaddingValues(0.dp),
                onClick = onDecrement,
                shape = buttonShape
            ) {
                ResizableText("-")
            }

            Button(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .semantics { contentDescription = increaseDescription },
                contentPadding = PaddingValues(0.dp),
                onClick = onIncrement,
                shape = buttonShape
            ) {
                ResizableText("+")
            }
        }
    }
}
