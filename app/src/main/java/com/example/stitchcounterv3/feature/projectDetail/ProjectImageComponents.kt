package com.example.stitchcounterv3.feature.projectDetail

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun ProjectImageSelector(
    imagePaths: List<String>,
    onImageClick: () -> Unit,
    onRemoveImage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (imagePaths.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(imagePaths) { imagePath ->
                    ProjectImageThumbnail(
                        imagePath = imagePath,
                        onImageClick = onImageClick,
                        onRemoveImage = { onRemoveImage(imagePath) }
                    )
                }
            }
        }
        
        if (imagePaths.size < 10) {
            ProjectImagePlaceholder(
                onImageClick = onImageClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ProjectImageThumbnail(
    imagePath: String,
    onImageClick: () -> Unit,
    onRemoveImage: () -> Unit
) {
    val context = LocalContext.current
    
    Box {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(context)
                    .data(imagePath)
                    .build()
            ),
            contentDescription = "Project image",
            modifier = Modifier
                .width(120.dp)
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onImageClick() },
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
            contentDescription = "Remove image",
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun ProjectImagePlaceholder(
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onImageClick() }
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = .2f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AddPhotoAlternate,
            contentDescription = "Add photo",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
        Text(
            text = "Add Photo",
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
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
