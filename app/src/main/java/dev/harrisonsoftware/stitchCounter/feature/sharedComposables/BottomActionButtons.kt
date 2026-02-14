package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.ui.theme.onQuaternary
import dev.harrisonsoftware.stitchCounter.ui.theme.quaternary

@Composable
fun BottomActionButtons(
    labelText: String = stringResource(R.string.action_reset),
    onResetAll: () -> Unit
) {
    AppButton(
        containerColor = MaterialTheme.quaternary,
        contentColor = MaterialTheme.onQuaternary,
        onClick = { onResetAll.invoke() },
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
    ) {
        Text(labelText)
    }
}
