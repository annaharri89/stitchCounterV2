package dev.harrisonsoftware.stitchCounter.feature.projectDetail

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ImageStorageUtilsRobolectricTest {

    @Test
    fun `resolveImagePathToAbsolutePath returns empty string when path escapes files directory`() {
        val filesDirectory = Files.createTempDirectory("image-utils-files").toFile()
        val context = mockk<Context>()
        every { context.filesDir } returns filesDirectory

        val resolved = resolveImagePathToAbsolutePath(context, "../outside_project_images")

        assertEquals("", resolved)
        filesDirectory.deleteRecursively()
    }

    @Test
    fun `matrixForExifOrientation returns identity for normal orientation`() {
        val matrix = matrixForExifOrientation(ExifInterface.ORIENTATION_NORMAL)

        assertTrue(matrix.isIdentity)
    }

    @Test
    fun `matrixForExifOrientation returns non identity for rotate 90`() {
        val matrix = matrixForExifOrientation(ExifInterface.ORIENTATION_ROTATE_90)

        assertFalse(matrix.isIdentity)
    }

    @Test
    fun `matrixForExifOrientation covers flip and transpose branches`() {
        assertFalse(matrixForExifOrientation(ExifInterface.ORIENTATION_FLIP_HORIZONTAL).isIdentity)
        assertFalse(matrixForExifOrientation(ExifInterface.ORIENTATION_ROTATE_180).isIdentity)
        assertFalse(matrixForExifOrientation(ExifInterface.ORIENTATION_FLIP_VERTICAL).isIdentity)
        assertFalse(matrixForExifOrientation(ExifInterface.ORIENTATION_TRANSPOSE).isIdentity)
        assertFalse(matrixForExifOrientation(ExifInterface.ORIENTATION_TRANSVERSE).isIdentity)
        assertFalse(matrixForExifOrientation(ExifInterface.ORIENTATION_ROTATE_270).isIdentity)
    }

    @Test
    fun `applyExifOrientation returns same bitmap when matrix is identity`() {
        val bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)

        val result = applyExifOrientation(bitmap, Matrix())

        assertSame(bitmap, result)
    }

    @Test
    fun `applyExifOrientation transforms when matrix rotates`() {
        val bitmap = Bitmap.createBitmap(6, 4, Bitmap.Config.ARGB_8888)
        val matrix = matrixForExifOrientation(ExifInterface.ORIENTATION_ROTATE_90)

        val result = applyExifOrientation(bitmap, matrix)

        assertFalse(bitmap === result)
        assertEquals(4, result.width)
        assertEquals(6, result.height)
        result.recycle()
        bitmap.recycle()
    }

    @Test
    fun `saveImageToInternalStorage writes jpeg under project_images and returns relative path`() {
        val root = Files.createTempDirectory("save-image-robo").toFile()
        val filesDir = File(root, "files").apply { mkdirs() }
        val sourceJpeg = File(root, "in.jpg")
        val largeBitmap = Bitmap.createBitmap(2100, 900, Bitmap.Config.ARGB_8888)
        FileOutputStream(sourceJpeg).use { outputStream ->
            largeBitmap.compress(Bitmap.CompressFormat.JPEG, 92, outputStream)
        }
        largeBitmap.recycle()

        val uri = mockk<Uri>()
        val resolver = mockk<ContentResolver>()
        val context = mockk<Context>()
        every { context.filesDir } returns filesDir
        every { context.contentResolver } returns resolver
        every { resolver.openInputStream(uri) } answers { FileInputStream(sourceJpeg) }

        val relativePath = saveImageToInternalStorage(context, uri)

        assertNotNull(relativePath)
        assertTrue(relativePath!!.startsWith("project_images/project_"))
        val outputFile = File(filesDir, relativePath)
        assertTrue(outputFile.exists())
        assertTrue(outputFile.length() > 0L)

        root.deleteRecursively()
    }
}
