package dev.harrisonsoftware.stitchCounter.logging

import dev.harrisonsoftware.stitchCounter.Constants
import timber.log.Timber

fun projectDataDebug(message: String) {
    Timber.tag(Constants.LOG_TAG_PROJECT_DATA).d(message)
}

fun projectDataInfo(message: String) {
    Timber.tag(Constants.LOG_TAG_PROJECT_DATA).i(message)
}

fun projectDataWarn(message: String, throwable: Throwable? = null) {
    if (throwable == null) {
        Timber.tag(Constants.LOG_TAG_PROJECT_DATA).w(message)
    } else {
        Timber.tag(Constants.LOG_TAG_PROJECT_DATA).w(throwable, message)
    }
}

fun projectDataError(message: String, throwable: Throwable? = null) {
    if (throwable == null) {
        Timber.tag(Constants.LOG_TAG_PROJECT_DATA).e(message)
    } else {
        Timber.tag(Constants.LOG_TAG_PROJECT_DATA).e(throwable, message)
    }
}
