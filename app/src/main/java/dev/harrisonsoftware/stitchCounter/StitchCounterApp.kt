package dev.harrisonsoftware.stitchCounter

import android.app.Application
import android.content.pm.ApplicationInfo
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dev.harrisonsoftware.stitchCounter.logging.TimberFileLogTree
import timber.log.Timber

@HiltAndroidApp
class StitchCounterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val loggingEntryPoint = EntryPointAccessors.fromApplication(this, LoggingEntryPoint::class.java)
        val isDebugBuild = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebugBuild) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(loggingEntryPoint.fileLogSink())
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LoggingEntryPoint {
    fun fileLogSink(): TimberFileLogTree
}

