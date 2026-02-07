package dev.harrisonsoftware.stitchCounter.data.backup

import android.net.Uri
import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface UriStreamProvider {
    fun openInputStream(uri: Uri): InputStream?
    fun openOutputStream(uri: Uri): OutputStream?
    fun uriFromFile(file: File): Uri
}
