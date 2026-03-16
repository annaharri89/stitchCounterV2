package dev.harrisonsoftware.stitchCounter.logging

import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLoggerImpl @Inject constructor(
    private val logSinks: Set<@JvmSuppressWildcards AppLogSink>,
) : AppLogger {

    override fun debug(tag: String, message: String) {
        log(level = AppLogLevel.DEBUG, tag = tag, message = message, throwable = null)
    }

    override fun info(tag: String, message: String) {
        log(level = AppLogLevel.INFO, tag = tag, message = message, throwable = null)
    }

    override fun warn(tag: String, message: String, throwable: Throwable?) {
        log(level = AppLogLevel.WARN, tag = tag, message = message, throwable = throwable)
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        log(level = AppLogLevel.ERROR, tag = tag, message = message, throwable = throwable)
    }

    private fun log(level: AppLogLevel, tag: String, message: String, throwable: Throwable?) {
        val entry = AppLogEntry(
            timestampEpochMillis = Date().time,
            level = level,
            tag = tag,
            message = message,
            throwable = throwable
        )
        logSinks.forEach { sink ->
            runCatching { sink.log(entry) }
        }
    }
}
