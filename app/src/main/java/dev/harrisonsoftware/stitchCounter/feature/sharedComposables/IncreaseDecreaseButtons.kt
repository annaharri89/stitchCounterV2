package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.ui.theme.LocalAppThemeStyle
import dev.harrisonsoftware.stitchCounter.ui.theme.quaternary

@Composable
fun IncreaseDecreaseButtons(
    modifier: Modifier = Modifier,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    counterLabel: String? = null,
    buttonSpacing: Int = 24,
    incrementFontSize: Int = 50,
    decrementFontSize: Int = 60,
) {
    val themeStyle = LocalAppThemeStyle.current
    val performHaptic = rememberThemedHaptic()

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

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppButton(
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = decreaseDescription },
                containerColor = MaterialTheme.quaternary,
                contentColor = Color.White,
                contentPadding = PaddingValues(0.dp),
                onClick = {
                    performHaptic()
                    onDecrement()
                },
            ) {
                androidx.compose.material3.Text(
                    text = themeStyle.decrementSymbol,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = decrementFontSize.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            AppButton(
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = increaseDescription },
                contentPadding = PaddingValues(0.dp),
                onClick = {
                    performHaptic()
                    onIncrement()
                },
            ) {
                androidx.compose.material3.Text(
                    text = themeStyle.incrementSymbol,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = incrementFontSize.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
