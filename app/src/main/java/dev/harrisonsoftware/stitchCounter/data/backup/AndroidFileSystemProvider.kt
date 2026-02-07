package dev.harrisonsoftware.stitchCounter.data.backup

import android.content.Context
import java.io.File

class AndroidFileSystemProvider(private val context: Context) : FileSystemProvider {
    override fun getCacheDirectory(): File = context.cacheDir
    override fun getFilesDirectory(): File = context.filesDir
    override fun getExternalFilesDirectory(): File? = context.getExternalFilesDir(null)
}
