package dev.harrisonsoftware.stitchCounter.logging

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogMaintenanceRunner @Inject constructor(
    private val fileLogSink: FileLogSink,
) {
    fun runStartupRetention() {
        fileLogSink.runRetention()
    }
}
