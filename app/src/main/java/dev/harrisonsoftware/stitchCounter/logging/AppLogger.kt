package dev.harrisonsoftware.stitchCounter.logging

interface AppLogger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warn(tag: String, message: String, throwable: Throwable? = null)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

enum class AppLogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR,
}

data class AppLogEntry(
    val timestampEpochMillis: Long,
    val level: AppLogLevel,
    val tag: String,
    val message: String,
    val throwable: Throwable?,
)

interface AppLogSink {
    fun log(entry: AppLogEntry)
}
