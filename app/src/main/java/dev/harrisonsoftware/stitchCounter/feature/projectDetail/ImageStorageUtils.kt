package dev.harrisonsoftware.stitchCounter.feature.projectDetail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import dev.harrisonsoftware.stitchCounter.Constants
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

private const val MAX_IMAGE_DIMENSION = 1024
private const val JPEG_COMPRESSION_QUALITY = 80
private const val PROJECT_IMAGES_DIRECTORY_NAME = "project_images"
private const val JPEG_FILE_EXTENSION = "jpg"

internal fun isPathInsideDirectory(directory: File, candidateFile: File): Boolean {
    val canonicalDirectoryPath = directory.canonicalPath + File.separator
    val canonicalCandidatePath = candidateFile.canonicalPath
    return canonicalCandidatePath.startsWith(canonicalDirectoryPath)
}

internal fun calculateInSampleSizeForDimensions(
    imageWidth: Int,
    imageHeight: Int,
    maxDimension: Int
): Int {
    var inSampleSize = 1
    if (imageWidth > maxDimension || imageHeight > maxDimension) {
        val largerDimension = max(imageWidth, imageHeight)
        while (largerDimension / (inSampleSize * 2) >= maxDimension) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

internal fun calculateScaledDimensions(
    width: Int,
    height: Int,
    maxDimension: Int
): Pair<Int, Int>? {
    val largerDimension = max(width, height)
    if (largerDimension <= maxDimension) return null

    val scaleFactor = maxDimension.toFloat() / largerDimension
    val scaledWidth = (width * scaleFactor).roundToInt()
    val scaledHeight = (height * scaleFactor).roundToInt()
    return scaledWidth to scaledHeight
}

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

        val exifOrientationMatrix = readExifOrientationMatrix(context, uri)
        val orientedBitmap = applyExifOrientation(sampledBitmap, exifOrientationMatrix)
        val scaledBitmap = scaleDownBitmap(orientedBitmap, MAX_IMAGE_DIMENSION)
        FileOutputStream(file).use { outputStream ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_COMPRESSION_QUALITY, outputStream)
        }
        if (scaledBitmap !== orientedBitmap) {
            scaledBitmap.recycle()
        }
        if (orientedBitmap !== sampledBitmap) {
            orientedBitmap.recycle()
        }
        sampledBitmap.recycle()

        "$PROJECT_IMAGES_DIRECTORY_NAME/$fileName"
    } catch (e: Exception) {
        Log.e(Constants.LOG_TAG_IMAGE_SAVE, "Error saving image", e)
        null
    }
}

fun resolveImagePathToAbsolutePath(context: Context, relativeImagePath: String): String {
    return try {
        val filesDirectory = context.filesDir
        val candidateFile = File(filesDirectory, relativeImagePath)
        if (isPathInsideDirectory(filesDirectory, candidateFile)) {
            candidateFile.canonicalPath
        } else {
            Log.w(Constants.LOG_TAG_IMAGE_SAVE, "Skipping image path outside files directory: $relativeImagePath")
            ""
        }
    } catch (e: Exception) {
        Log.e(Constants.LOG_TAG_IMAGE_SAVE, "Error resolving image path", e)
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
    return calculateInSampleSizeForDimensions(imageWidth, imageHeight, maxDimension)
}

private fun scaleDownBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val scaledDimensions = calculateScaledDimensions(bitmap.width, bitmap.height, maxDimension)
    if (scaledDimensions == null) return bitmap
    return Bitmap.createScaledBitmap(
        bitmap,
        scaledDimensions.first,
        scaledDimensions.second,
        true
    )
}

internal fun matrixForExifOrientation(orientation: Int): Matrix {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.setRotate(90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.setRotate(-90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
    }
    return matrix
}

private fun readExifOrientationMatrix(context: Context, uri: Uri): Matrix {
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            return matrixForExifOrientation(orientation)
        }
    } catch (e: Exception) {
        Log.w(Constants.LOG_TAG_IMAGE_SAVE, "Failed to read EXIF orientation", e)
    }
    return Matrix()
}

internal fun applyExifOrientation(bitmap: Bitmap, exifMatrix: Matrix): Bitmap {
    if (exifMatrix.isIdentity) return bitmap
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, exifMatrix, true)
}
