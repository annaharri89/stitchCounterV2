package dev.harrisonsoftware.stitchCounter.data.repo

import dev.harrisonsoftware.stitchCounter.data.local.NoteDao
import dev.harrisonsoftware.stitchCounter.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    fun observeNotes(): Flow<List<NoteEntity>> = noteDao.observeAll()

    suspend fun getNote(id: Int): NoteEntity? = noteDao.getById(id)

    suspend fun upsert(entity: NoteEntity): Long = noteDao.upsert(entity)

    suspend fun delete(entity: NoteEntity) = noteDao.delete(entity)
}
