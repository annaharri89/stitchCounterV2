package dev.harrisonsoftware.stitchCounter

import android.app.Application
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dev.harrisonsoftware.stitchCounter.logging.LogMaintenanceRunner

@HiltAndroidApp
class StitchCounterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val loggingEntryPoint = EntryPointAccessors.fromApplication(this, LoggingEntryPoint::class.java)
        loggingEntryPoint.logMaintenanceRunner().runStartupRetention()
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LoggingEntryPoint {
    fun logMaintenanceRunner(): LogMaintenanceRunner
}

