package dev.harrisonsoftware.stitchCounter.logging

import android.util.Log
import dev.harrisonsoftware.stitchCounter.Constants
import dev.harrisonsoftware.stitchCounter.data.backup.FileSystemProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

private const val LOG_DIRECTORY_NAME = "logs"

@Singleton
class TimberFileLogTree @Inject constructor(
    private val fileSystemProvider: FileSystemProvider,
    private val logRetentionPolicy: LogRetentionPolicy,
) : Timber.Tree() {
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val commandChannel = Channel<TimberLogTreeCommand>(Channel.UNLIMITED)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        runRetention()
        ioScope.launch {
            for (command in commandChannel) {
                when (command) {
                    is TimberLogTreeCommand.WriteEntry -> {
                        runCatching { appendLogEntry(command.entry) }
                            .onFailure { throwable ->
                                Log.w(Constants.LOG_TAG_TIMBER_FILE_LOG_TREE, "Failed writing log entry to file", throwable)
                            }
                    }

                    is TimberLogTreeCommand.Flush -> {
                        command.completion.complete(Unit)
                    }
                }
            }
        }
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.DEBUG || priority == Log.VERBOSE) return
        val sendResult = commandChannel.trySend(
            TimberLogTreeCommand.WriteEntry(
                TimberLogEntry(
                    timestampEpochMillis = Instant.now().toEpochMilli(),
                    priority = priority,
                    tag = tag ?: "UnknownTag",
                    message = message,
                    throwable = t
                )
            )
        )
        if (sendResult.isFailure) {
            Log.w(Constants.LOG_TAG_TIMBER_FILE_LOG_TREE, "Failed enqueuing log entry for file persistence")
        }
    }

    suspend fun flushAndWait() {
        val completion = CompletableDeferred<Unit>()
        commandChannel.send(TimberLogTreeCommand.Flush(completion))
        completion.await()
    }

    suspend fun flushAndSyncForPackaging() {
        flushAndWait()
        runCatching { syncPersistedLogFiles() }
            .onFailure { throwable ->
                Log.w(Constants.LOG_TAG_TIMBER_FILE_LOG_TREE, "Failed syncing persisted log files before packaging", throwable)
            }
    }

    fun runRetention(currentDate: LocalDate = LocalDate.now(ZoneOffset.UTC)) {
        runCatching { logRetentionPolicy.apply(resolveLogDirectory(), currentDate) }
            .onFailure { throwable ->
                Log.w(Constants.LOG_TAG_TIMBER_FILE_LOG_TREE, "Failed applying log retention policy", throwable)
            }
    }

    fun resolveLogDirectory(): File {
        val logDirectory = File(fileSystemProvider.getFilesDirectory(), LOG_DIRECTORY_NAME)
        if (!logDirectory.exists()) {
            runCatching { logDirectory.mkdirs() }
        }
        return logDirectory
    }

    private fun appendLogEntry(entry: TimberLogEntry) {
        val logFile = File(resolveLogDirectory(), buildFileName(entry.timestampEpochMillis))
        val sanitizedMessage = entry.message.replace("\n", "\\n")
        val logLine = buildString {
            append(Instant.ofEpochMilli(entry.timestampEpochMillis).toString())
            append(" | ")
            append(LogPriorityFormatter.toLabel(entry.priority))
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

    private fun syncPersistedLogFiles() {
        resolveLogDirectory()
            .listFiles()
            ?.asSequence()
            ?.filter { it.isFile && it.extension == "log" }
            ?.forEach { logFile ->
                FileOutputStream(logFile, true).use { outputStream ->
                    outputStream.fd.sync()
                }
            }
    }
}

private sealed interface TimberLogTreeCommand {
    data class WriteEntry(val entry: TimberLogEntry) : TimberLogTreeCommand
    data class Flush(val completion: CompletableDeferred<Unit>) : TimberLogTreeCommand
}

private data class TimberLogEntry(
    val timestampEpochMillis: Long,
    val priority: Int,
    val tag: String,
    val message: String,
    val throwable: Throwable?,
)

private object LogPriorityFormatter {
    fun toLabel(priority: Int): String = when (priority) {
        Log.INFO -> "INFO"
        Log.WARN -> "WARN"
        Log.ERROR -> "ERROR"
        Log.ASSERT -> "ASSERT"
        else -> "LOG"
    }
}
