package dev.harrisonsoftware.stitchCounter.di

import android.content.Context
import androidx.room.Room
import dev.harrisonsoftware.stitchCounter.data.local.AppDatabase
import dev.harrisonsoftware.stitchCounter.data.local.DatabaseMigrations
import dev.harrisonsoftware.stitchCounter.data.local.NoteDao
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
        )
            .addMigrations(*DatabaseMigrations.ALL)
            .build()

    @Provides
    @Singleton
    fun provideProjectDao(db: AppDatabase): ProjectDao = db.projectDao()

    @Provides
    @Singleton
    fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()
}

