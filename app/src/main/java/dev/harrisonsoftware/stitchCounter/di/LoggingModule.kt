package dev.harrisonsoftware.stitchCounter.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.harrisonsoftware.stitchCounter.logging.AndroidDeviceMetadataProvider
import dev.harrisonsoftware.stitchCounter.logging.DeviceMetadataProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class LoggingModule {

    @Binds
    abstract fun bindDeviceMetadataProvider(impl: AndroidDeviceMetadataProvider): DeviceMetadataProvider
}
