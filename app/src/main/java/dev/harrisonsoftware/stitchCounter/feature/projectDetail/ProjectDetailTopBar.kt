package dev.harrisonsoftware.stitchCounter.feature.projectDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.harrisonsoftware.stitchCounter.R

@Composable
fun ProjectDetailTopBar(
    isNewProject: Boolean,
    onCloseClick: (() -> Unit)?,
    onBackClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back)
                )
            }
        } else {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(48.dp))
        }
        
        Text(
            text = stringResource(R.string.project_detail_title),
            style = MaterialTheme.typography.headlineMedium
        )
        
        if (onCloseClick != null) {
            IconButton(onClick = onCloseClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cd_close)
                )
            }
        } else {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(48.dp))
        }
    }
}
