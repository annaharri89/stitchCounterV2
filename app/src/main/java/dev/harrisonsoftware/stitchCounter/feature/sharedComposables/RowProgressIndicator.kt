package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RowProgressIndicator(
    progress: Float?,
    modifier: Modifier = Modifier
) {
    progress?.let {
        LinearProgressIndicator(
            modifier = modifier.fillMaxWidth(),
            progress = { it }
        )
    }
}

@Composable
fun RowProgressWithLabel(
    currentRowCount: Int,
    totalRows: Int,
    modifier: Modifier = Modifier
) {
    val progress: Float? = if (totalRows > 0) {
        (currentRowCount.toFloat() / totalRows.toFloat()).coerceIn(0f, 1f)
    } else {
        null
    }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowProgressIndicator(
            modifier = Modifier.weight(1f),
            progress = progress
        )
        if (totalRows > 0) {
            Text(
                text = "$currentRowCount/$totalRows",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
