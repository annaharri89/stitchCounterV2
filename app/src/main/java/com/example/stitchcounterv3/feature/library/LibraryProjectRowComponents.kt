package com.example.stitchcounterv3.feature.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.stitchcounterv3.domain.model.Project
import com.example.stitchcounterv3.domain.model.ProjectType
import com.example.stitchcounterv3.feature.sharedComposables.RowProgressIndicator

@Composable
internal fun ProjectImageOrCheckbox(
    project: Project,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onSelect: () -> Unit
) {
    val context = LocalContext.current
    
    if (isMultiSelectMode) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onSelect() },
            modifier = Modifier.size(24.dp)
        )
    } else {
        if (project.imagePath != null) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(project.imagePath)
                        .build()
                ),
                contentDescription = "Project image",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } 
    }
}

@Composable
internal fun ProjectInfoSection(
    project: Project,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ProjectTitle(project.title)
        ProjectStatsContent(project = project)
    }
}

@Composable
private fun ProjectTitle(
    title: String
) {
    Text(
        text = title.ifBlank { "Untitled Project" },
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun ProjectStatsContent(
    project: Project
) {
    when (project.type) {
        ProjectType.SINGLE -> {
            StatBadge(
                label = "Count",
                value = project.stitchCounterNumber.toString(),
                modifier = Modifier.fillMaxWidth()
            )
        }
        ProjectType.DOUBLE -> {
            DoubleProjectStats(
                project = project
            )
        }
    }
}

@Composable
private fun DoubleProjectStats(
    project: Project
) {
    val rowProgress: Float? = if (project.totalRows > 0) {
        (project.rowCounterNumber.toFloat() / project.totalRows.toFloat()).coerceIn(0f, 1f)
    } else {
        null
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBadge(
                label = "Stitches",
                value = project.stitchCounterNumber.toString(),
                modifier = Modifier.weight(1f)
            )
            StatBadge(
                label = "Rows",
                value = "${project.rowCounterNumber}${if (project.totalRows > 0) "/${project.totalRows}" else ""}",
                modifier = Modifier.weight(1f)
            )
        }
        
        if (rowProgress != null) {
            RowProgressIndicator(
                progress = rowProgress,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
internal fun ProjectActionButtons(
    onInfoClick: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onInfoClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Project details",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete project",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun StatBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
