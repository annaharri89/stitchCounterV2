package dev.harrisonsoftware.stitchCounter.feature.projectDetail

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class ImageStorageUtilsHelperTest {

    @Test
    fun `calculateInSampleSizeForDimensions returns 1 when image is under max dimension`() {
        val result = calculateInSampleSizeForDimensions(
            imageWidth = 800,
            imageHeight = 600,
            maxDimension = 1024
        )

        assertEquals(1, result)
    }

    @Test
    fun `calculateInSampleSizeForDimensions scales down with power-of-two sample size`() {
        val result = calculateInSampleSizeForDimensions(
            imageWidth = 4032,
            imageHeight = 3024,
            maxDimension = 1024
        )

        assertEquals(2, result)
    }

    @Test
    fun `calculateScaledDimensions returns null when no scaling is needed`() {
        val result = calculateScaledDimensions(width = 900, height = 600, maxDimension = 1024)

        assertNull(result)
    }

    @Test
    fun `calculateScaledDimensions keeps aspect ratio when scaling down landscape bitmap`() {
        val result = calculateScaledDimensions(width = 2400, height = 1200, maxDimension = 1024)

        assertEquals(1024 to 512, result)
    }

    @Test
    fun `isPathInsideDirectory returns true for child path and false for traversal path`() {
        val filesDirectory = Files.createTempDirectory("image-utils-files").toFile()
        val childFile = File(filesDirectory, "project_images/photo.jpg")
        val traversalFile = File(filesDirectory, "../outside.jpg")

        assertTrue(isPathInsideDirectory(filesDirectory, childFile))
        assertFalse(isPathInsideDirectory(filesDirectory, traversalFile))
    }

    @Test
    fun `resolveImagePathToAbsolutePath returns canonical path for file inside files directory`() {
        val filesDirectory = Files.createTempDirectory("image-utils-files").toFile()
        val projectImage = File(filesDirectory, "project_images/photo.jpg").apply {
            parentFile?.mkdirs()
            writeText("image")
        }
        val context = mockk<Context>()
        every { context.filesDir } returns filesDirectory

        val resolved = resolveImagePathToAbsolutePath(context, "project_images/photo.jpg")

        assertEquals(projectImage.canonicalPath, resolved)
    }
}
