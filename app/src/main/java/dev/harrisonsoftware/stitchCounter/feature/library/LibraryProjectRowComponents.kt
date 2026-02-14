package dev.harrisonsoftware.stitchCounter.feature.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.RowProgressIndicator

@Composable
internal fun ProjectImage(
    project: Project
) {
    val context = LocalContext.current

    project.imagePaths.firstOrNull()?.let { imagePath ->
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(context)
                    .data(imagePath)
                    .build()
            ),
            contentDescription = stringResource(R.string.cd_project_image),
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    }

}

@Composable
internal fun ProjectInfoSection(
    project: Project,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProjectTitle(project.title)

        if (project.completedAt != null) {
            CompletedBadge()
        }
    }
}

@Composable
private fun CompletedBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .height(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = stringResource(R.string.label_project_completed),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
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
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun ProjectStatsContent(project: Project) {
    when (project.type) {
        ProjectType.SINGLE -> {
            StatBadge(
                label = stringResource(R.string.label_count),
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
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
