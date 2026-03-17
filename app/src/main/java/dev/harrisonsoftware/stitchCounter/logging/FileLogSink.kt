package dev.harrisonsoftware.stitchCounter.logging

import android.util.Log
import dev.harrisonsoftware.stitchCounter.data.backup.FileSystemProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private const val FILE_LOG_SINK_LOG_TAG = "SCFileLogSink"
private const val LOG_DIRECTORY_NAME = "logs"

@Singleton
/**
 * Persists non-debug [AppLogEntry] records to daily files under app-private storage.
 *
 * Entries are queued and written on a background IO coroutine so callers do not block.
 * The sink also applies [LogRetentionPolicy] at startup and when explicitly requested.
 */
class FileLogSink @Inject constructor(
    private val fileSystemProvider: FileSystemProvider,
    private val logRetentionPolicy: LogRetentionPolicy,
) : AppLogSink {
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val commandChannel = Channel<FileLogCommand>(Channel.UNLIMITED)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        runRetention()
        ioScope.launch {
            for (command in commandChannel) {
                when (command) {
                    is FileLogCommand.WriteEntry -> {
                        runCatching { appendLogEntry(command.entry) }
                            .onFailure { throwable ->
                                Log.w(FILE_LOG_SINK_LOG_TAG, "Failed writing log entry to file", throwable)
                            }
                    }
                    is FileLogCommand.Flush -> {
                        command.completion.complete(Unit)
                    }
                }
            }
        }
    }

    override fun log(entry: AppLogEntry) {
        if (entry.level == AppLogLevel.DEBUG) return
        val sendResult = commandChannel.trySend(FileLogCommand.WriteEntry(entry))
        if (sendResult.isFailure) {
            Log.w(FILE_LOG_SINK_LOG_TAG, "Failed enqueuing log entry for file persistence")
        }
    }

    suspend fun flushAndWait() {
        val completion = CompletableDeferred<Unit>()
        commandChannel.send(FileLogCommand.Flush(completion))
        completion.await()
    }

    /** Applies retention cleanup to the resolved log directory. */
    fun runRetention() {
        runCatching { logRetentionPolicy.apply(resolveLogDirectory()) }
            .onFailure { throwable ->
                Log.w(FILE_LOG_SINK_LOG_TAG, "Failed applying log retention policy", throwable)
            }
    }

    /** Returns the app-private logs directory, creating it when missing. */
    fun resolveLogDirectory(): File {
        val logDirectory = File(fileSystemProvider.getFilesDirectory(), LOG_DIRECTORY_NAME)
        if (!logDirectory.exists()) {
            runCatching { logDirectory.mkdirs() }
        }
        return logDirectory
    }

    private fun appendLogEntry(entry: AppLogEntry) {
        val logFile = File(resolveLogDirectory(), buildFileName(entry.timestampEpochMillis))
        val sanitizedMessage = entry.message.replace("\n", "\\n")
        val logLine = buildString {
            append(Instant.ofEpochMilli(entry.timestampEpochMillis).toString())
            append(" | ")
            append(entry.level.name)
            append(" | ")
            append(entry.tag)
            append(" | ")
            append(sanitizedMessage)
            if (entry.throwable != null) {
                append(" | throwable=")
                append(entry.throwable::class.java.simpleName)
                append(":")
                append(entry.throwable.message ?: "no_message")
            }
        }
        logFile.appendText("$logLine\n")
    }

    private fun buildFileName(timestampEpochMillis: Long): String {
        val date = Instant.ofEpochMilli(timestampEpochMillis)
            .atOffset(ZoneOffset.UTC)
            .toLocalDate()
        return "app-log-${dateFormatter.format(date)}.log"
    }
}

private sealed interface FileLogCommand {
    data class WriteEntry(val entry: AppLogEntry) : FileLogCommand
    data class Flush(val completion: CompletableDeferred<Unit>) : FileLogCommand
}
