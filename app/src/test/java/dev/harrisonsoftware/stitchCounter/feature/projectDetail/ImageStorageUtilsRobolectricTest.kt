package dev.harrisonsoftware.stitchCounter.feature.projectDetail

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ImageStorageUtilsRobolectricTest {

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
}
