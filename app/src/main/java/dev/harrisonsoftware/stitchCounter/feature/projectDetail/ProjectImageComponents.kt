package dev.harrisonsoftware.stitchCounter.feature.projectDetail

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import dev.harrisonsoftware.stitchCounter.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

private const val MAX_PHOTOS = 10

@Composable
fun ProjectImageSelector(
    imagePaths: List<String>,
    onImageClick: () -> Unit,
    onRemoveImage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val photoCount = imagePaths.size
    val isAtMax = photoCount >= MAX_PHOTOS
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProjectPhotosSectionHeader(
            currentCount = photoCount,
            maxCount = MAX_PHOTOS,
            modifier = Modifier.fillMaxWidth()
        )
        
        if (photoCount == 0) {
            ProjectImagePlaceholder(
                hasExistingPhotos = false,
                isAtMax = false,
                onImageClick = onImageClick,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            val allItems = imagePaths.toMutableList()
            if (!isAtMax) {
                allItems.add("ADD_BUTTON_PLACEHOLDER")
            }
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allItems.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(
                                modifier = Modifier.weight(1f)
                            ) {
                                if (item == "ADD_BUTTON_PLACEHOLDER") {
                                    AddPhotoButton(
                                        onImageClick = onImageClick,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    ProjectImageThumbnail(
                                        imagePath = item,
                                        onRemoveImage = { onRemoveImage(item) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectImageThumbnail(
    imagePath: String,
    onRemoveImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Box(modifier = modifier) {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(context)
                    .data(imagePath)
                    .build()
            ),
            contentDescription = stringResource(R.string.cd_project_image),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        ProjectImageDeleteButton(
            onRemoveImage = onRemoveImage,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        )
    }
}

@Composable
private fun ProjectImageDeleteButton(
    onRemoveImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onRemoveImage,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = stringResource(R.string.cd_remove_image),
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun ProjectImagePlaceholder(
    hasExistingPhotos: Boolean,
    isAtMax: Boolean,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val placeholderText = when {
        isAtMax -> stringResource(R.string.max_photos_reached, MAX_PHOTOS)
        hasExistingPhotos -> stringResource(R.string.add_another_photo)
        else -> stringResource(R.string.add_photo)
    }
    
    Column(
        modifier = modifier
            .then(
                if (!isAtMax) {
                    Modifier.clickable { onImageClick() }
                } else {
                    Modifier
                }
            )
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isAtMax) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = .2f)
                }
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AddPhotoAlternate,
            contentDescription = stringResource(R.string.cd_add_photo),
            modifier = Modifier.size(32.dp),
            tint = if (isAtMax) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.tertiary
            }
        )
        Text(
            text = placeholderText,
            color = if (isAtMax) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.tertiary
            },
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AddPhotoButton(
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            .clickable { onImageClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.cd_add_another_photo),
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ProjectPhotosSectionHeader(
    currentCount: Int,
    maxCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.project_photos_header),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.photo_count_format, currentCount, maxCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun saveImageToInternalStorage(context: Context, uri: Uri, projectId: Int, imageIndex: Int = 0): String? {
    return try {
        val imagesDir = File(context.filesDir, "project_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        
        val fileName = "project_${projectId}_${System.currentTimeMillis()}_${imageIndex}.jpg"
        val file = File(imagesDir, fileName)
        
        context.contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        
        file.absolutePath
    } catch (e: Exception) {
        android.util.Log.e("ProjectDetailScreen", "Error saving image", e)
        null
    }
}
