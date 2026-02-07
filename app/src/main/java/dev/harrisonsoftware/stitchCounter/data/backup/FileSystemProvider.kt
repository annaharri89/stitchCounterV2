package dev.harrisonsoftware.stitchCounter.data.backup

import java.io.File

interface FileSystemProvider {
    fun getCacheDirectory(): File
    fun getFilesDirectory(): File
    fun getExternalFilesDirectory(): File?
}
