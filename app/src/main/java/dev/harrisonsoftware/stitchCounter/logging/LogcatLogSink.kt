package dev.harrisonsoftware.stitchCounter.logging

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogcatLogSink @Inject constructor() : AppLogSink {

    override fun log(entry: AppLogEntry) {
        when (entry.level) {
            AppLogLevel.DEBUG -> Log.d(entry.tag, entry.message)
            AppLogLevel.INFO -> Log.i(entry.tag, entry.message)
            AppLogLevel.WARN -> {
                if (entry.throwable == null) {
                    Log.w(entry.tag, entry.message)
                } else {
                    Log.w(entry.tag, entry.message, entry.throwable)
                }
            }
            AppLogLevel.ERROR -> {
                if (entry.throwable == null) {
                    Log.e(entry.tag, entry.message)
                } else {
                    Log.e(entry.tag, entry.message, entry.throwable)
                }
            }
        }
    }
}
