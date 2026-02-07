package dev.harrisonsoftware.stitchCounter.di

import android.content.Context
import android.content.pm.PackageManager
import dev.harrisonsoftware.stitchCounter.data.backup.AndroidFileSystemProvider
import dev.harrisonsoftware.stitchCounter.data.backup.AndroidUriStreamProvider
import dev.harrisonsoftware.stitchCounter.data.backup.BackupManager
import dev.harrisonsoftware.stitchCounter.data.backup.FileSystemProvider
import dev.harrisonsoftware.stitchCounter.data.backup.UriStreamProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackupModule {
    
    @Provides
    @Singleton
    fun provideFileSystemProvider(@ApplicationContext context: Context): FileSystemProvider =
        AndroidFileSystemProvider(context)
    
    @Provides
    @Singleton
    fun provideUriStreamProvider(@ApplicationContext context: Context): UriStreamProvider =
        AndroidUriStreamProvider(context)
    
    @Provides
    @Singleton
    fun provideBackupManager(
        fileSystemProvider: FileSystemProvider,
        uriStreamProvider: UriStreamProvider
    ): BackupManager = BackupManager(fileSystemProvider, uriStreamProvider)
    
    @Provides
    @Singleton
    fun provideAppVersion(@ApplicationContext context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0"
        }
    }
}
