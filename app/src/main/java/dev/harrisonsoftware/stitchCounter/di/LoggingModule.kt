package dev.harrisonsoftware.stitchCounter.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import dev.harrisonsoftware.stitchCounter.logging.AndroidDeviceMetadataProvider
import dev.harrisonsoftware.stitchCounter.logging.AppLogSink
import dev.harrisonsoftware.stitchCounter.logging.AppLogger
import dev.harrisonsoftware.stitchCounter.logging.AppLoggerImpl
import dev.harrisonsoftware.stitchCounter.logging.DeviceMetadataProvider
import dev.harrisonsoftware.stitchCounter.logging.FileLogSink
import dev.harrisonsoftware.stitchCounter.logging.LogcatLogSink
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LoggingModule {

    @Binds
    @Singleton
    abstract fun bindAppLogger(impl: AppLoggerImpl): AppLogger

    @Binds
    @Singleton
    abstract fun bindDeviceMetadataProvider(impl: AndroidDeviceMetadataProvider): DeviceMetadataProvider

    @Binds
    @IntoSet
    abstract fun bindLogcatSinkAsAppLogSink(logcatLogSink: LogcatLogSink): AppLogSink

    @Binds
    @IntoSet
    abstract fun bindFileLogSinkAsAppLogSink(fileLogSink: FileLogSink): AppLogSink
}
