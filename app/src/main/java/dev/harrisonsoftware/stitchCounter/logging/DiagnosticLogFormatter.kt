package dev.harrisonsoftware.stitchCounter.logging

internal const val LOG_APP_VERSION_PREFIX = "app="

internal fun formatLogLine(
    timestamp: String,
    levelLabel: String,
    tag: String,
    appVersion: String,
    message: String,
    throwable: Throwable? = null
): String {
    val sanitizedMessage = message.replace("\n", "\\n")
    return buildString {
        append(timestamp)
        append(" | ")
        append(levelLabel)
        append(" | ")
        append(tag)
        append(" | ")
        append(LOG_APP_VERSION_PREFIX)
        append(appVersion)
        append(" | ")
        append(sanitizedMessage)
        if (throwable != null) {
            append(" | throwable=")
            append(throwable::class.java.simpleName)
            append(":")
            append(throwable.message ?: "no_message")
        }
    }
}

internal fun logLineHasAppVersion(logLine: String): Boolean {
    return logLine.contains(" | $LOG_APP_VERSION_PREFIX")
}

internal fun addAppVersionToLine(logLine: String, appVersion: String): String {
    if (logLine.isBlank()) return logLine
    if (logLineHasAppVersion(logLine)) return logLine

    val fields = logLine.split(" | ", limit = 4)
    return if (fields.size == 4) {
        "${fields[0]} | ${fields[1]} | ${fields[2]} | " +
            "$LOG_APP_VERSION_PREFIX$appVersion | ${fields[3]}"
    } else {
        "$LOG_APP_VERSION_PREFIX$appVersion | $logLine"
    }
}

internal fun addAppVersionToFile(logFileContent: String, appVersion: String): String {
    return logFileContent
        .lineSequence()
        .map { line -> addAppVersionToLine(line, appVersion) }
        .joinToString("\n")
}
