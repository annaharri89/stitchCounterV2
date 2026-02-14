package dev.harrisonsoftware.stitchCounter.di

import android.content.Context
import androidx.room.Room
import dev.harrisonsoftware.stitchCounter.data.local.AppDatabase
import dev.harrisonsoftware.stitchCounter.data.local.DatabaseMigrations
import dev.harrisonsoftware.stitchCounter.data.local.ProjectDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "StitchCounter.db"
        ).addMigrations(
            DatabaseMigrations.MIGRATION_1_2,
            DatabaseMigrations.MIGRATION_2_3,
            DatabaseMigrations.MIGRATION_3_4
        ).build()

    @Provides
    @Singleton
    fun provideProjectDao(db: AppDatabase): ProjectDao = db.projectDao()
}

