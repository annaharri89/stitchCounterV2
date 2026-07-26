package dev.harrisonsoftware.stitchCounter.domain.mapper

import dev.harrisonsoftware.stitchCounter.data.local.NoteEntity
import dev.harrisonsoftware.stitchCounter.domain.model.Note
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteMappersTest {

    private val entity = NoteEntity(
        id = 3,
        title = "Pattern notes",
        body = "Use 4mm needles",
        createdAt = 100L,
        updatedAt = 200L,
    )

    private val domain = Note(
        id = 3,
        title = "Pattern notes",
        body = "Use 4mm needles",
        createdAt = 100L,
        updatedAt = 200L,
    )

    @Test
    fun `toDomain maps all fields from entity`() {
        assertEquals(domain, entity.toDomain())
    }

    @Test
    fun `toEntity maps all fields from domain`() {
        assertEquals(entity, domain.toEntity())
    }
}
