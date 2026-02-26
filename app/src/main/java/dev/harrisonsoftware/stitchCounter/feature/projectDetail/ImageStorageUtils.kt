package dev.harrisonsoftware.stitchCounter.feature.projectDetail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

private const val MAX_IMAGE_DIMENSION = 1024
private const val JPEG_COMPRESSION_QUALITY = 80
private const val PROJECT_IMAGES_DIRECTORY_NAME = "project_images"
private const val JPEG_FILE_EXTENSION = "jpg"
private const val LOG_TAG = "ImageSave"

/** Decodes, scales down to [MAX_IMAGE_DIMENSION], and saves as compressed JPEG. Returns a relative path or null on failure. */
fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val imagesDir = File(context.filesDir, PROJECT_IMAGES_DIRECTORY_NAME)
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        val fileName = "project_${UUID.randomUUID()}.$JPEG_FILE_EXTENSION"
        val file = File(imagesDir, fileName)

        val sampleSize = calculateInSampleSize(context, uri, MAX_IMAGE_DIMENSION)

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }

        val sampledBitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, decodeOptions)
        } ?: return null

        val scaledBitmap = scaleDownBitmap(sampledBitmap, MAX_IMAGE_DIMENSION)
        FileOutputStream(file).use { outputStream ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_COMPRESSION_QUALITY, outputStream)
        }
        if (scaledBitmap !== sampledBitmap) {
            scaledBitmap.recycle()
        }
        sampledBitmap.recycle()

        "$PROJECT_IMAGES_DIRECTORY_NAME/$fileName"
    } catch (e: Exception) {
        Log.e(LOG_TAG, "Error saving image", e)
        null
    }
}

fun resolveImagePathToAbsolutePath(context: Context, relativeImagePath: String): String {
    return try {
        val filesDirectory = context.filesDir
        val candidateFile = File(filesDirectory, relativeImagePath)
        val canonicalFilesDirectoryPath = filesDirectory.canonicalPath + File.separator
        val canonicalCandidatePath = candidateFile.canonicalPath
        if (canonicalCandidatePath.startsWith(canonicalFilesDirectoryPath)) {
            canonicalCandidatePath
        } else {
            Log.w(LOG_TAG, "Skipping image path outside files directory: $relativeImagePath")
            ""
        }
    } catch (e: Exception) {
        Log.e(LOG_TAG, "Error resolving image path", e)
        ""
    }
}

private fun calculateInSampleSize(context: Context, uri: Uri, maxDimension: Int): Int {
    val boundsOptions = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BitmapFactory.decodeStream(inputStream, null, boundsOptions)
    }

    val imageWidth = boundsOptions.outWidth
    val imageHeight = boundsOptions.outHeight
    var inSampleSize = 1

    if (imageWidth > maxDimension || imageHeight > maxDimension) {
        val largerDimension = max(imageWidth, imageHeight)
        while (largerDimension / (inSampleSize * 2) >= maxDimension) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

private fun scaleDownBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val largerDimension = max(bitmap.width, bitmap.height)
    if (largerDimension <= maxDimension) return bitmap

    val scaleFactor = maxDimension.toFloat() / largerDimension
    val scaledWidth = (bitmap.width * scaleFactor).roundToInt()
    val scaledHeight = (bitmap.height * scaleFactor).roundToInt()
    return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
}
