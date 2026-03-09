package dev.harrisonsoftware.stitchCounter.domain.usecase

import dev.harrisonsoftware.stitchCounter.data.backup.BackupData
import dev.harrisonsoftware.stitchCounter.data.backup.BackupExtraction
import dev.harrisonsoftware.stitchCounter.data.backup.BackupManager
import dev.harrisonsoftware.stitchCounter.data.backup.BackupMetadata
import dev.harrisonsoftware.stitchCounter.data.backup.BackupProject
import dev.harrisonsoftware.stitchCounter.data.repo.ProjectRepository
import dev.harrisonsoftware.stitchCounter.domain.model.ContentUri
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ImportLibraryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var projectRepository: ProjectRepository
    private lateinit var backupManager: BackupManager
    private lateinit var importLibrary: ImportLibrary

    private val inputUri = ContentUri("content://backup.zip")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        projectRepository = mockk(relaxed = true)
        backupManager = mockk(relaxed = true)
        importLibrary = ImportLibrary(projectRepository, backupManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sampleBackupProject(
        id: Int = 1,
        title: String = "Scarf",
        type: String = "single",
        imagePaths: List<String> = emptyList()
    ) = BackupProject(
        id = id,
        type = type,
        title = title,
        notes = "",
        stitchCounterNumber = 10,
        stitchAdjustment = 1,
        rowCounterNumber = 0,
        rowAdjustment = 1,
        totalRows = 0,
        imagePaths = imagePaths,
        createdAt = 1_000_000L,
        updatedAt = 1_000_000L,
    )

    private fun sampleMetadata(version: Int = 1) = BackupMetadata(
        version = version,
        exportDate = 1_000_000L,
        appVersion = "2.0.0",
        projectCount = 1,
    )

    private fun sampleExtraction(
        projects: List<BackupProject> = listOf(sampleBackupProject()),
        version: Int = 1
    ): BackupExtraction {
        val tempDir = mockk<File>(relaxed = true)
        val imagesDir = mockk<File>(relaxed = true)
        return BackupExtraction(
            backupData = BackupData(
                metadata = sampleMetadata(version),
                projects = projects,
            ),
            imagesDir = imagesDir,
            tempDir = tempDir,
        )
    }

    @Test
    fun `import succeeds and returns correct import count`() = runTest {
        val extraction = sampleExtraction(
            projects = listOf(sampleBackupProject(1, "Scarf"), sampleBackupProject(2, "Hat"))
        )
        coEvery { backupManager.extractBackupZip(inputUri) } returns Result.success(extraction)
        coEvery { projectRepository.upsert(any()) } returns 100L

        val result = importLibrary(inputUri)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.importedCount)
        assertEquals(0, result.getOrNull()?.failedCount)
    }

    @Test
    fun `import fails for unsupported backup version`() = runTest {
        val extraction = sampleExtraction(version = 999)
        coEvery { backupManager.extractBackupZip(inputUri) } returns Result.success(extraction)

        val result = importLibrary(inputUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Unsupported backup version") == true)
    }

    @Test
    fun `import cleans up temp directory on unsupported version`() = runTest {
        val extraction = sampleExtraction(version = 999)
        coEvery { backupManager.extractBackupZip(inputUri) } returns Result.success(extraction)

        importLibrary(inputUri)

        coVerify { backupManager.cleanupTempDirectory(extraction.tempDir) }
    }

    @Test
    fun `import cleans up temp directory on success`() = runTest {
        val extraction = sampleExtraction()
        coEvery { backupManager.extractBackupZip(inputUri) } returns Result.success(extraction)
        coEvery { projectRepository.upsert(any()) } returns 1L

        importLibrary(inputUri)

        coVerify { backupManager.cleanupTempDirectory(extraction.tempDir) }
    }

    @Test
    fun `import fails when extraction fails`() = runTest {
        coEvery { backupManager.extractBackupZip(inputUri) } returns
                Result.failure(Exception("Corrupt zip"))

        val result = importLibrary(inputUri)

        assertTrue(result.isFailure)
        assertEquals("Corrupt zip", result.exceptionOrNull()?.message)
    }

    @Test
    fun `import with replaceExisting preserves original project id`() = runTest {
        val project = sampleBackupProject(id = 42, title = "Blanket")
        val extraction = sampleExtraction(projects = listOf(project))
        coEvery { backupManager.extractBackupZip(inputUri) } returns Result.success(extraction)

        val entitySlot = slot<dev.harrisonsoftware.stitchCounter.data.local.ProjectEntity>()
        coEvery { projectRepository.upsert(capture(entitySlot)) } returns 42L

        importLibrary(inputUri, replaceExisting = true)

        assertEquals(42, entitySlot.captured.id)
    }

    @Test
    fun `import without replaceExisting sets id to zero`() = runTest {
        val project = sampleBackupProject(id = 42, title = "Blanket")
        val extraction = sampleExtraction(projects = listOf(project))
        coEvery { backupManager.extractBackupZip(inputUri) } returns Result.success(extraction)

        val entitySlot = slot<dev.harrisonsoftware.stitchCounter.data.local.ProjectEntity>()
        coEvery { projectRepository.upsert(capture(entitySlot)) } returns 99L

        importLibrary(inputUri, replaceExisting = false)

        assertEquals(0, entitySlot.captured.id)
    }

    @Test
    fun `import maps double type correctly`() = runTest {
        val project = sampleBackupProject(type = "double")
        val extraction = sampleExtraction(projects = listOf(project))
        coEvery { backupManager.extractBackupZip(inputUri) } returns Result.success(extraction)

        val entitySlot = slot<dev.harrisonsoftware.stitchCounter.data.local.ProjectEntity>()
        coEvery { projectRepository.upsert(capture(entitySlot)) } returns 1L

        importLibrary(inputUri)

        assertEquals("double", entitySlot.captured.type)
    }

    @Test
    fun `import counts partial failures`() = runTest {
        val projects = listOf(
            sampleBackupProject(1, "Good"),
            sampleBackupProject(2, "Bad"),
        )
        val extraction = sampleExtraction(projects = projects)
        coEvery { backupManager.extractBackupZip(inputUri) } returns Result.success(extraction)

        var callCount = 0
        coEvery { projectRepository.upsert(any()) } answers {
            callCount++
            if (callCount == 2) throw RuntimeException("DB error")
            callCount.toLong()
        }

        val result = importLibrary(inputUri)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.importedCount)
        assertEquals(1, result.getOrNull()?.failedCount)
        assertTrue(result.getOrNull()?.failedProjectNames?.any { it.contains("Bad") } == true)
    }

    @Test
    fun `import with empty project list succeeds with zero counts`() = runTest {
        val extraction = sampleExtraction(projects = emptyList())
        coEvery { backupManager.extractBackupZip(inputUri) } returns Result.success(extraction)

        val result = importLibrary(inputUri)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.importedCount)
        assertEquals(0, result.getOrNull()?.failedCount)
    }
}
