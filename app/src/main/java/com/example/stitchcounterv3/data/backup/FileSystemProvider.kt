package com.example.stitchcounterv3.data.backup

import java.io.File

interface FileSystemProvider {
    fun getCacheDirectory(): File
    fun getFilesDirectory(): File
    fun getExternalFilesDirectory(): File?
}
