package dev.harrisonsoftware.stitchCounter.domain.usecase

import dev.harrisonsoftware.stitchCounter.data.repo.NoteRepository
import dev.harrisonsoftware.stitchCounter.domain.mapper.toDomain
import dev.harrisonsoftware.stitchCounter.domain.mapper.toEntity
import dev.harrisonsoftware.stitchCounter.domain.model.Note
import dev.harrisonsoftware.stitchCounter.domain.validation.NoteValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveNotes @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> =
        repository.observeNotes().map { notes -> notes.map { it.toDomain() } }
}

@Singleton
class GetNote @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: Int): Note? = repository.getNote(id)?.toDomain()
}

@Singleton
class CreateNote @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(title: String, body: String, timestampMillis: Long): CreateNoteResult {
        if (!NoteValidator.isTitleValid(title)) {
            return CreateNoteResult.InvalidTitle
        }
        val trimmedTitle = title.trim()
        val note = Note(
            title = trimmedTitle,
            body = body,
            createdAt = timestampMillis,
            updatedAt = timestampMillis,
        )
        val noteId = repository.upsert(note.toEntity())
        return CreateNoteResult.Success(noteId)
    }
}

@Singleton
class UpdateNote @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(
        noteId: Int,
        title: String,
        body: String,
        timestampMillis: Long,
    ): UpdateNoteResult {
        if (!NoteValidator.isTitleValid(title)) {
            return UpdateNoteResult.InvalidTitle
        }
        val existing = repository.getNote(noteId) ?: return UpdateNoteResult.NotFound
        val trimmedTitle = title.trim()
        val updatedNote = existing.toDomain().copy(
            title = trimmedTitle,
            body = body,
            updatedAt = timestampMillis,
        )
        repository.upsert(updatedNote.toEntity())
        return UpdateNoteResult.Success
    }
}

sealed interface CreateNoteResult {
    data class Success(val noteId: Long) : CreateNoteResult
    data object InvalidTitle : CreateNoteResult
}

sealed interface UpdateNoteResult {
    data object Success : UpdateNoteResult
    data object InvalidTitle : UpdateNoteResult
    data object NotFound : UpdateNoteResult
}

@Singleton
class DeleteNote @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        repository.delete(note.toEntity())
    }
}
