package dev.harrisonsoftware.stitchCounter.domain.usecase

import dev.harrisonsoftware.stitchCounter.data.backup.BackupData
import dev.harrisonsoftware.stitchCounter.data.backup.BackupManagerError
import dev.harrisonsoftware.stitchCounter.data.backup.BackupManager
import dev.harrisonsoftware.stitchCounter.data.backup.BackupZipCreationResult
import dev.harrisonsoftware.stitchCounter.data.local.ProjectEntity
import dev.harrisonsoftware.stitchCounter.data.repo.ProjectRepository
import dev.harrisonsoftware.stitchCounter.domain.model.ContentUri
import dev.harrisonsoftware.stitchCounter.logging.AppLogger
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExportLibraryTest {

    private lateinit var projectRepository: ProjectRepository
    private lateinit var backupManager: BackupManager
    private lateinit var appLogger: AppLogger
    private lateinit var exportLibrary: ExportLibrary

    private val appVersion = "2.0.0"

    @Before
    fun setUp() {
        projectRepository = mockk()
        backupManager = mockk()
        appLogger = mockk(relaxed = true)
        exportLibrary = ExportLibrary(projectRepository, backupManager, appVersion, appLogger)
    }

    private fun sampleEntity(id: Int = 1, title: String = "Scarf") = ProjectEntity(
        id = id,
        type = "single",
        title = title,
        notes = "",
        stitchCounterNumber = 10,
        stitchAdjustment = 1,
        rowCounterNumber = 0,
        rowAdjustment = 1,
        totalRows = 0,
        imagePaths = emptyList(),
        createdAt = 1_000_000L,
        updatedAt = 1_000_000L,
        completedAt = null,
        totalStitchesEver = 50,
    )

    @Test
    fun `export maps projects and passes correct data to backupManager`() = runTest {
        val entities = listOf(sampleEntity(1, "Scarf"), sampleEntity(2, "Hat"))
        every { projectRepository.observeProjects() } returns flowOf(entities)

        val backupDataSlot = slot<BackupData>()
        every { backupManager.createBackupZip(capture(backupDataSlot), any()) } returns
                BackupZipCreationResult.Success(ContentUri("content://exported.zip"))

        val result = exportLibrary()

        assertTrue(result is ExportLibraryResult.Success)
        val successResult = result as ExportLibraryResult.Success
        assertEquals("content://exported.zip", successResult.contentUri.value)

        val captured = backupDataSlot.captured
        assertEquals(2, captured.projects.size)
        assertEquals("Scarf", captured.projects[0].title)
        assertEquals("Hat", captured.projects[1].title)
        assertEquals(2, captured.metadata.projectCount)
        assertEquals(1, captured.metadata.version)
        assertEquals(appVersion, captured.metadata.appVersion)
    }

    @Test
    fun `export with no projects passes empty list`() = runTest {
        every { projectRepository.observeProjects() } returns flowOf(emptyList())

        val backupDataSlot = slot<BackupData>()
        every { backupManager.createBackupZip(capture(backupDataSlot), any()) } returns
                BackupZipCreationResult.Success(ContentUri("content://empty.zip"))

        val result = exportLibrary()

        assertTrue(result is ExportLibraryResult.Success)
        assertEquals(0, backupDataSlot.captured.projects.size)
        assertEquals(0, backupDataSlot.captured.metadata.projectCount)
    }

    @Test
    fun `export forwards outputContentUri to backupManager`() = runTest {
        every { projectRepository.observeProjects() } returns flowOf(emptyList())
        every { backupManager.createBackupZip(any(), any()) } returns
                BackupZipCreationResult.Success(ContentUri("content://custom.zip"))

        val outputUri = ContentUri("content://user-chosen-location")
        exportLibrary(outputUri)

        verify { backupManager.createBackupZip(any(), outputUri) }
    }

    @Test
    fun `export returns failure when backupManager fails`() = runTest {
        every { projectRepository.observeProjects() } returns flowOf(emptyList())
        every { backupManager.createBackupZip(any(), any()) } returns
                BackupZipCreationResult.Failure(BackupManagerError.ExternalFilesDirectoryUnavailable)

        val result = exportLibrary()

        assertTrue(result is ExportLibraryResult.Failure)
        val errorResult = result as ExportLibraryResult.Failure
        assertTrue(errorResult.error is ExportLibraryError.BackupCreationFailed)
    }

    @Test
    fun `export returns failure when repository throws`() = runTest {
        every { projectRepository.observeProjects() } throws RuntimeException("DB error")

        val result = exportLibrary()

        assertTrue(result is ExportLibraryResult.Failure)
        val errorResult = result as ExportLibraryResult.Failure
        assertTrue(errorResult.error is ExportLibraryError.Unexpected)
    }

    @Test
    fun `export maps double type correctly`() = runTest {
        val entity = sampleEntity().copy(type = "double")
        every { projectRepository.observeProjects() } returns flowOf(listOf(entity))

        val backupDataSlot = slot<BackupData>()
        every { backupManager.createBackupZip(capture(backupDataSlot), any()) } returns
                BackupZipCreationResult.Success(ContentUri("content://out.zip"))

        exportLibrary()

        assertEquals("double", backupDataSlot.captured.projects[0].type)
    }
}
