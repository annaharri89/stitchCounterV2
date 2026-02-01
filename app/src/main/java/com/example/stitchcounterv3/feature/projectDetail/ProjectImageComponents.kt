package com.example.stitchcounterv3.feature.projectDetail

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
    imagePath: String?,
    onImageClick: () -> Unit,
    onRemoveImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (imagePath != null) {
            ProjectImageDisplay(
                imagePath = imagePath,
                onImageClick = onImageClick,
                onRemoveImage = onRemoveImage
            )
        } else {
            ProjectImagePlaceholder(onImageClick = onImageClick)
        }
    }
}

@Composable
private fun ProjectImageDisplay(
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
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onImageClick() },
            contentScale = ContentScale.Crop
        )
        ProjectImageDeleteButton(
            onRemoveImage = onRemoveImage,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
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
    onImageClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onImageClick() }
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = .2f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AddPhotoAlternate,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
        Text(
            text = "Add Project Image",
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "(You can add it later)",
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
    }
}

fun saveImageToInternalStorage(context: Context, uri: Uri, projectId: Int): String? {
    return try {
        val imagesDir = File(context.filesDir, "project_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        
        val fileName = "project_${projectId}_${System.currentTimeMillis()}.jpg"
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
