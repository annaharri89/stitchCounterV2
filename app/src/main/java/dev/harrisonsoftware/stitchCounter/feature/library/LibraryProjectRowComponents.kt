package dev.harrisonsoftware.stitchCounter.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.painter.ColorPainter
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.feature.projectDetail.resolveImagePathToAbsolutePath
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.RowProgressIndicator

@Composable
internal fun ProjectImageOrCheckbox(
    project: Project,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onSelect: () -> Unit
) {
    val context = LocalContext.current
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
    val errorColor = MaterialTheme.colorScheme.errorContainer

    if (isMultiSelectMode) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onSelect() },
            modifier = Modifier.size(24.dp)
        )
    } else {
        project.imagePaths.firstOrNull()?.let { imagePath ->
            val thumbnailSizePx = with(LocalDensity.current) { 100.dp.roundToPx() }
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resolveImagePathToAbsolutePath(context, imagePath))
                    .crossfade(true)
                    .size(thumbnailSizePx)
                    .build(),
                contentDescription = stringResource(R.string.cd_project_image),
                placeholder = ColorPainter(placeholderColor),
                error = ColorPainter(errorColor),
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
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ProjectTitle(project.title)
        if (project.completedAt != null) {
            ProjectFinishedBadge()
        }
    }
}

@Composable
private fun ProjectFinishedBadge() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.label_project_completed),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ProjectTitle(
    title: String
) {
    Text(
        text = title.ifBlank { stringResource(R.string.library_untitled_project) },
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun ProjectStatsContent(
    project: Project,
    modifier: Modifier = Modifier
) {
    when (project.type) {
        ProjectType.SINGLE -> {
            StatBadge(
                label = stringResource(R.string.label_count),
                value = project.stitchCounterNumber.toString(),
                modifier = modifier.fillMaxWidth()
            )
        }
        ProjectType.DOUBLE -> {
            DoubleProjectStats(
                project = project,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun DoubleProjectStats(
    project: Project,
    modifier: Modifier = Modifier
) {
    val rowProgress: Float? = if (project.totalRows > 0) {
        (project.rowCounterNumber.toFloat() / project.totalRows.toFloat()).coerceIn(0f, 1f)
    } else {
        null
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBadge(
                label = stringResource(R.string.label_stitches),
                value = project.stitchCounterNumber.toString(),
                modifier = Modifier.weight(1f)
            )
            StatBadge(
                label = stringResource(R.string.label_rows),
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
                contentDescription = stringResource(R.string.cd_project_details),
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
                contentDescription = stringResource(R.string.cd_delete_project),
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
    val badgeDescription = stringResource(R.string.cd_stat_badge, label, value)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = badgeDescription
            },
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
