package dev.harrisonsoftware.stitchCounter.data.backup

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class AndroidUriStreamProvider(private val context: Context) : UriStreamProvider {
    override fun openInputStream(uri: Uri): InputStream? = context.contentResolver.openInputStream(uri)
    override fun openOutputStream(uri: Uri): OutputStream? = context.contentResolver.openOutputStream(uri)
    override fun uriFromFile(file: File): Uri = Uri.fromFile(file)
}
