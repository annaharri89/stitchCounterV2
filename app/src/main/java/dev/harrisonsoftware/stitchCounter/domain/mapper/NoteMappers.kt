package dev.harrisonsoftware.stitchCounter.domain.mapper

import dev.harrisonsoftware.stitchCounter.data.local.NoteEntity
import dev.harrisonsoftware.stitchCounter.domain.model.Note

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    body = body,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    body = body,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
