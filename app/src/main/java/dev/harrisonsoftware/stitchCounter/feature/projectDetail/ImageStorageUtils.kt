package dev.harrisonsoftware.stitchCounter.feature.projectDetail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.roundToInt

private const val MAX_IMAGE_DIMENSION = 1024
private const val JPEG_COMPRESSION_QUALITY = 80
private const val LOG_TAG = "ImageSave"

/** Decodes, scales down to [MAX_IMAGE_DIMENSION], and saves as compressed JPEG. Returns the absolute path or null on failure. */
fun saveImageToInternalStorage(context: Context, uri: Uri, projectId: Int, imageIndex: Int = 0): String? {
    return try {
        val imagesDir = File(context.filesDir, "project_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        val fileName = "project_${projectId}_${System.currentTimeMillis()}_${imageIndex}.jpg"
        val file = File(imagesDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
                ?: return null

            val scaledBitmap = scaleDownBitmap(originalBitmap, MAX_IMAGE_DIMENSION)
            FileOutputStream(file).use { outputStream ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_COMPRESSION_QUALITY, outputStream)
            }
            if (scaledBitmap !== originalBitmap) {
                scaledBitmap.recycle()
            }
            originalBitmap.recycle()
        }

        file.absolutePath
    } catch (e: Exception) {
        Log.e(LOG_TAG, "Error saving image", e)
        null
    }
}

private fun scaleDownBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val largerDimension = max(bitmap.width, bitmap.height)
    if (largerDimension <= maxDimension) return bitmap

    val scaleFactor = maxDimension.toFloat() / largerDimension
    val scaledWidth = (bitmap.width * scaleFactor).roundToInt()
    val scaledHeight = (bitmap.height * scaleFactor).roundToInt()
    return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
}
