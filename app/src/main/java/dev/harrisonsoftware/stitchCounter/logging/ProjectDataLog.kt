package dev.harrisonsoftware.stitchCounter.logging

import dev.harrisonsoftware.stitchCounter.Constants

fun AppLogger.projectDataDebug(message: String) {
    debug(Constants.LOG_TAG_PROJECT_DATA, message)
}

fun AppLogger.projectDataInfo(message: String) {
    info(Constants.LOG_TAG_PROJECT_DATA, message)
}

fun AppLogger.projectDataWarn(message: String, throwable: Throwable? = null) {
    warn(Constants.LOG_TAG_PROJECT_DATA, message, throwable)
}

fun AppLogger.projectDataError(message: String, throwable: Throwable? = null) {
    error(Constants.LOG_TAG_PROJECT_DATA, message, throwable)
}
