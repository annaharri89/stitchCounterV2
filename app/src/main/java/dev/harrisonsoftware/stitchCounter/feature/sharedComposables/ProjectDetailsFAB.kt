package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.harrisonsoftware.stitchCounter.R

@Composable
fun ProjectDetailsFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val fabSize = if (compact) 32.dp else 40.dp
    val iconSize = if (compact) 16.dp else 20.dp
    val startPadding = if (compact) 8.dp else 16.dp
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .padding(start = startPadding)
            .size(fabSize),
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = stringResource(R.string.cd_project_details),
            modifier = Modifier.size(iconSize)
        )
    }
}
