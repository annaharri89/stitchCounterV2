package dev.harrisonsoftware.stitchCounter.domain.usecase

import dev.harrisonsoftware.stitchCounter.data.backup.BACKUP_FORMAT_VERSION_1
import dev.harrisonsoftware.stitchCounter.data.backup.BackupData
import dev.harrisonsoftware.stitchCounter.data.backup.BackupExtraction
import dev.harrisonsoftware.stitchCounter.data.backup.BackupManagerError
import dev.harrisonsoftware.stitchCounter.data.backup.BackupManager
import dev.harrisonsoftware.stitchCounter.data.backup.BackupMetadata
import dev.harrisonsoftware.stitchCounter.data.backup.BackupNote
import dev.harrisonsoftware.stitchCounter.data.backup.BackupProject
import dev.harrisonsoftware.stitchCounter.data.backup.BackupZipExtractionResult
import dev.harrisonsoftware.stitchCounter.data.repo.NoteRepository
import dev.harrisonsoftware.stitchCounter.data.repo.ProjectRepository
import dev.harrisonsoftware.stitchCounter.domain.model.ContentUri
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
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
    private lateinit var noteRepository: NoteRepository
    private lateinit var backupManager: BackupManager
    private lateinit var importLibrary: ImportLibrary

    private val inputUri = ContentUri("content://backup.zip")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        projectRepository = mockk(relaxed = true)
        noteRepository = mockk(relaxed = true)
        backupManager = mockk(relaxed = true)
        importLibrary = ImportLibrary(projectRepository, noteRepository, backupManager)
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

    private fun sampleBackupNote(
        id: Int = 1,
        title: String = "Yarn note",
        body: String = "Use merino",
    ) = BackupNote(
        id = id,
        title = title,
        body = body,
        createdAt = 2_000_000L,
        updatedAt = 2_000_000L,
    )

    private fun sampleMetadata(version: Int = BACKUP_FORMAT_VERSION_1, noteCount: Int = 0) = BackupMetadata(
        version = version,
        exportDate = 1_000_000L,
        appVersion = "2.0.0",
        projectCount = 1,
        noteCount = noteCount,
    )

    private fun sampleExtraction(
        projects: List<BackupProject> = listOf(sampleBackupProject()),
        notes: List<BackupNote> = emptyList(),
        version: Int = BACKUP_FORMAT_VERSION_1
    ): BackupExtraction {
        val tempDir = mockk<File>(relaxed = true)
        val imagesDir = mockk<File>(relaxed = true)
        return BackupExtraction(
            backupData = BackupData(
                metadata = sampleMetadata(version, noteCount = notes.size),
                projects = projects,
                notes = notes,
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
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)
        coEvery { projectRepository.upsert(any()) } returns 100L

        val result = importLibrary(inputUri)

        assertTrue(result is ImportLibraryResult.Success)
        val successResult = result as ImportLibraryResult.Success
        assertEquals(2, successResult.result.importedCount)
        assertEquals(0, successResult.result.failedCount)
    }

    @Test
    fun `import restores notes to repository`() = runTest {
        val notes = listOf(
            sampleBackupNote(1, "Yarn note", "Use merino"),
            sampleBackupNote(2, "Pattern tweak", ""),
        )
        val extraction = sampleExtraction(projects = emptyList(), notes = notes, version = 2)
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)
        coEvery { noteRepository.upsert(any()) } returnsMany listOf(10L, 11L)

        val result = importLibrary(inputUri)

        assertTrue(result is ImportLibraryResult.Success)
        val successResult = result as ImportLibraryResult.Success
        assertEquals(2, successResult.result.importedNotesCount)
        assertEquals(0, successResult.result.failedNotesCount)
        coVerify(exactly = 2) { noteRepository.upsert(any()) }
    }

    @Test
    fun `import version 1 backup without notes field succeeds`() = runTest {
        val extraction = sampleExtraction(projects = listOf(sampleBackupProject()), notes = emptyList(), version = 1)
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)
        coEvery { projectRepository.upsert(any()) } returns 1L

        val result = importLibrary(inputUri)

        assertTrue(result is ImportLibraryResult.Success)
        val successResult = result as ImportLibraryResult.Success
        assertEquals(1, successResult.result.importedCount)
        assertEquals(0, successResult.result.importedNotesCount)
        coVerify(exactly = 0) { noteRepository.upsert(any()) }
    }

    @Test
    fun `import maps unsupported type to single and imports all projects`() = runTest {
        val extraction = sampleExtraction(
            projects = listOf(
                sampleBackupProject(1, "Scarf", type = "single"),
                sampleBackupProject(2, "Weird", type = "triple"),
            )
        )
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)

        val entitySlots = mutableListOf<dev.harrisonsoftware.stitchCounter.data.local.ProjectEntity>()
        coEvery { projectRepository.upsert(capture(entitySlots)) } returnsMany listOf(1L, 2L)

        val result = importLibrary(inputUri)

        assertTrue(result is ImportLibraryResult.Success)
        val successResult = result as ImportLibraryResult.Success
        assertEquals(2, successResult.result.importedCount)
        assertEquals(0, successResult.result.failedCount)
        assertEquals("single", entitySlots[1].type)
        coVerify(exactly = 2) { projectRepository.upsert(any()) }
    }

    @Test
    fun `import maps unknown export type to UNKNOWN project type`() = runTest {
        val extraction = sampleExtraction(
            projects = listOf(sampleBackupProject(1, "Legacy", type = "unknown")),
        )
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)

        val entitySlot = slot<dev.harrisonsoftware.stitchCounter.data.local.ProjectEntity>()
        coEvery { projectRepository.upsert(capture(entitySlot)) } returns 1L

        val result = importLibrary(inputUri)

        assertTrue(result is ImportLibraryResult.Success)
        assertEquals("unknown", entitySlot.captured.type)
    }

    @Test
    fun `import maps row_and_repeat type correctly`() = runTest {
        val extraction = sampleExtraction(
            projects = listOf(sampleBackupProject(type = "row_and_repeat")),
        )
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)

        val entitySlot = slot<dev.harrisonsoftware.stitchCounter.data.local.ProjectEntity>()
        coEvery { projectRepository.upsert(capture(entitySlot)) } returns 1L

        importLibrary(inputUri)

        assertEquals("row_and_repeat", entitySlot.captured.type)
    }

    @Test
    fun `import notes does not affect project import when note fails`() = runTest {
        val extraction = sampleExtraction(
            projects = listOf(sampleBackupProject(1, "Scarf")),
            notes = listOf(sampleBackupNote(1, "Bad note")),
        )
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)
        coEvery { projectRepository.upsert(any()) } returns 1L
        coEvery { noteRepository.upsert(any()) } throws RuntimeException("DB error")

        val result = importLibrary(inputUri)

        assertTrue(result is ImportLibraryResult.Success)
        val successResult = result as ImportLibraryResult.Success
        assertEquals(1, successResult.result.importedCount)
        assertEquals(0, successResult.result.importedNotesCount)
        assertEquals(1, successResult.result.failedNotesCount)
        assertTrue(successResult.result.failedNoteNames.any { it.contains("Bad note") })
    }

    @Test
    fun `import with replaceExisting preserves original note id`() = runTest {
        val note = sampleBackupNote(id = 42, title = "Saved note")
        val extraction = sampleExtraction(projects = emptyList(), notes = listOf(note))
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)

        val entitySlot = slot<dev.harrisonsoftware.stitchCounter.data.local.NoteEntity>()
        coEvery { noteRepository.upsert(capture(entitySlot)) } returns 42L

        importLibrary(inputUri, replaceExisting = true)

        assertEquals(42, entitySlot.captured.id)
    }

    @Test
    fun `import without replaceExisting sets note id to zero`() = runTest {
        val note = sampleBackupNote(id = 42, title = "Saved note")
        val extraction = sampleExtraction(projects = emptyList(), notes = listOf(note))
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)

        val entitySlot = slot<dev.harrisonsoftware.stitchCounter.data.local.NoteEntity>()
        coEvery { noteRepository.upsert(capture(entitySlot)) } returns 99L

        importLibrary(inputUri, replaceExisting = false)

        assertEquals(0, entitySlot.captured.id)
    }

    @Test
    fun `import note with empty body succeeds`() = runTest {
        val note = sampleBackupNote(id = 1, title = "Empty body note", body = "")
        val extraction = sampleExtraction(projects = emptyList(), notes = listOf(note))
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)

        val entitySlot = slot<dev.harrisonsoftware.stitchCounter.data.local.NoteEntity>()
        coEvery { noteRepository.upsert(capture(entitySlot)) } returns 1L

        val result = importLibrary(inputUri)

        assertTrue(result is ImportLibraryResult.Success)
        assertEquals("", entitySlot.captured.body)
        assertEquals(1, (result as ImportLibraryResult.Success).result.importedNotesCount)
    }

    @Test
    fun `import fails for unsupported backup version`() = runTest {
        val extraction = sampleExtraction(version = 999)
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)

        val result = importLibrary(inputUri)

        assertTrue(result is ImportLibraryResult.Failure)
        val errorResult = result as ImportLibraryResult.Failure
        assertTrue(errorResult.error is ImportLibraryError.UnsupportedBackupVersion)
    }

    @Test
    fun `import accepts version 2 backup`() = runTest {
        val extraction = sampleExtraction(version = 2)
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)
        coEvery { projectRepository.upsert(any()) } returns 1L

        val result = importLibrary(inputUri)

        assertTrue(result is ImportLibraryResult.Success)
    }

    @Test
    fun `import cleans up temp directory on unsupported version`() = runTest {
        val extraction = sampleExtraction(version = 999)
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)

        importLibrary(inputUri)

        verify { backupManager.cleanupTempDirectory(extraction.tempDir) }
    }

    @Test
    fun `import cleans up temp directory on success`() = runTest {
        val extraction = sampleExtraction()
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)
        coEvery { projectRepository.upsert(any()) } returns 1L

        importLibrary(inputUri)

        verify { backupManager.cleanupTempDirectory(extraction.tempDir) }
    }

    @Test
    fun `import fails when extraction fails`() = runTest {
        every { backupManager.extractBackupZip(inputUri) } returns
                BackupZipExtractionResult.Failure(BackupManagerError.BackupJsonMissing)

        val result = importLibrary(inputUri)

        assertTrue(result is ImportLibraryResult.Failure)
        val errorResult = result as ImportLibraryResult.Failure
        assertTrue(errorResult.error is ImportLibraryError.BackupExtractionFailed)
    }

    @Test
    fun `import with replaceExisting preserves original project id`() = runTest {
        val project = sampleBackupProject(id = 42, title = "Blanket")
        val extraction = sampleExtraction(projects = listOf(project))
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)

        val entitySlot = slot<dev.harrisonsoftware.stitchCounter.data.local.ProjectEntity>()
        coEvery { projectRepository.upsert(capture(entitySlot)) } returns 42L

        importLibrary(inputUri, replaceExisting = true)

        assertEquals(42, entitySlot.captured.id)
    }

    @Test
    fun `import without replaceExisting sets id to zero`() = runTest {
        val project = sampleBackupProject(id = 42, title = "Blanket")
        val extraction = sampleExtraction(projects = listOf(project))
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)

        val entitySlot = slot<dev.harrisonsoftware.stitchCounter.data.local.ProjectEntity>()
        coEvery { projectRepository.upsert(capture(entitySlot)) } returns 99L

        importLibrary(inputUri, replaceExisting = false)

        assertEquals(0, entitySlot.captured.id)
    }

    @Test
    fun `import maps double type correctly`() = runTest {
        val project = sampleBackupProject(type = "double")
        val extraction = sampleExtraction(projects = listOf(project))
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)

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
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)

        var callCount = 0
        coEvery { projectRepository.upsert(any()) } answers {
            callCount++
            if (callCount == 2) throw RuntimeException("DB error")
            callCount.toLong()
        }

        val result = importLibrary(inputUri)

        assertTrue(result is ImportLibraryResult.Success)
        val successResult = result as ImportLibraryResult.Success
        assertEquals(1, successResult.result.importedCount)
        assertEquals(1, successResult.result.failedCount)
        assertTrue(successResult.result.failedProjectNames.any { it.contains("Bad") })
    }

    @Test
    fun `import with empty project list succeeds with zero counts`() = runTest {
        val extraction = sampleExtraction(projects = emptyList())
        every { backupManager.extractBackupZip(inputUri) } returns BackupZipExtractionResult.Success(extraction)

        val result = importLibrary(inputUri)

        assertTrue(result is ImportLibraryResult.Success)
        val successResult = result as ImportLibraryResult.Success
        assertEquals(0, successResult.result.importedCount)
        assertEquals(0, successResult.result.failedCount)
    }
}
