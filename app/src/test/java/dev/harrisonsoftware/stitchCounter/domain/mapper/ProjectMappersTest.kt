package dev.harrisonsoftware.stitchCounter.domain.mapper

import dev.harrisonsoftware.stitchCounter.data.local.ProjectEntity
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProjectMappersTest {

    private val fixedTimestamp = 1_700_000_000_000L

    private fun sampleEntity(
        type: String = "single",
        completedAt: Long? = null
    ) = ProjectEntity(
        id = 42,
        type = type,
        title = "Cozy Scarf",
        notes = "Blue yarn",
        stitchCounterNumber = 15,
        stitchAdjustment = 5,
        rowCounterNumber = 3,
        rowAdjustment = 1,
        totalRows = 20,
        imagePaths = listOf("img/a.jpg", "img/b.png"),
        createdAt = fixedTimestamp,
        updatedAt = fixedTimestamp + 1000,
        completedAt = completedAt,
        totalStitchesEver = 100,
    )

    private fun sampleDomain(
        type: ProjectType = ProjectType.SINGLE,
        completedAt: Long? = null
    ) = Project(
        id = 42,
        type = type,
        title = "Cozy Scarf",
        notes = "Blue yarn",
        stitchCounterNumber = 15,
        stitchAdjustment = 5,
        rowCounterNumber = 3,
        rowAdjustment = 1,
        totalRows = 20,
        imagePaths = listOf("img/a.jpg", "img/b.png"),
        createdAt = fixedTimestamp,
        updatedAt = fixedTimestamp + 1000,
        completedAt = completedAt,
        totalStitchesEver = 100,
    )

    @Test
    fun `toDomain maps single entity correctly`() {
        val domain = sampleEntity(type = "single").toDomain()
        assertEquals(sampleDomain(type = ProjectType.SINGLE), domain)
    }

    @Test
    fun `toDomain maps double entity correctly`() {
        val domain = sampleEntity(type = "double").toDomain()
        assertEquals(ProjectType.DOUBLE, domain.type)
    }

    @Test
    fun `toDomain is case insensitive for double type`() {
        assertEquals(ProjectType.DOUBLE, sampleEntity(type = "Double").toDomain().type)
        assertEquals(ProjectType.DOUBLE, sampleEntity(type = "DOUBLE").toDomain().type)
        assertEquals(ProjectType.DOUBLE, sampleEntity(type = "dOuBlE").toDomain().type)
    }

    @Test
    fun `toDomain treats unknown type as SINGLE`() {
        assertEquals(ProjectType.SINGLE, sampleEntity(type = "triple").toDomain().type)
        assertEquals(ProjectType.SINGLE, sampleEntity(type = "").toDomain().type)
    }

    @Test
    fun `toDomain preserves completedAt`() {
        val completedTimestamp = fixedTimestamp + 5000
        val domain = sampleEntity(completedAt = completedTimestamp).toDomain()
        assertEquals(completedTimestamp, domain.completedAt)
    }

    @Test
    fun `toDomain preserves null completedAt`() {
        val domain = sampleEntity(completedAt = null).toDomain()
        assertNull(domain.completedAt)
    }

    @Test
    fun `toEntity maps SINGLE to single string`() {
        val entity = sampleDomain(type = ProjectType.SINGLE).toEntity()
        assertEquals("single", entity.type)
    }

    @Test
    fun `toEntity maps DOUBLE to double string`() {
        val entity = sampleDomain(type = ProjectType.DOUBLE).toEntity()
        assertEquals("double", entity.type)
    }

    @Test
    fun `round-trip entity to domain to entity preserves all fields`() {
        val original = sampleEntity(type = "single", completedAt = fixedTimestamp + 9999)
        val roundTripped = original.toDomain().toEntity()
        assertEquals(original, roundTripped)
    }

    @Test
    fun `round-trip domain to entity to domain preserves all fields`() {
        val original = sampleDomain(type = ProjectType.DOUBLE, completedAt = fixedTimestamp + 9999)
        val roundTripped = original.toEntity().toDomain()
        assertEquals(original, roundTripped)
    }
}
