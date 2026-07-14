package dev.harrisonsoftware.stitchCounter.data.repo

import dev.harrisonsoftware.stitchCounter.data.local.ProjectDao
import dev.harrisonsoftware.stitchCounter.data.local.ProjectEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for managing knitting/crochet projects.
 * 
 * This repository implements the Repository pattern, providing a clean abstraction
 * over the data layer. It acts as a single source of truth for project data and
 * encapsulates the complexity of data access operations.
 * 
 * The repository is marked as a Singleton to ensure a single instance across the app
 * and uses dependency injection to receive the ProjectDao.
 */
@Singleton
class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao
) {
    /**
     * Observes all projects with reactive updates.
     * 
     * Returns a Flow that automatically emits updated project lists whenever
     * the database changes. This enables reactive UI updates without manual refresh.
     * 
     * @return Flow<List<ProjectEntity>> Reactive stream of all projects
     */
    fun observeProjects(): Flow<List<ProjectEntity>> = projectDao.observeAll()

    /**
     * Retrieves a specific project by its unique ID.
     */
    suspend fun getProject(id: Int): ProjectEntity? = projectDao.getById(id)

    /**
     * Creates a new project or updates an existing one.
     */
    suspend fun upsert(entity: ProjectEntity): Long = projectDao.upsert(entity)

    /**
     * Deletes a project from the database.
     */
    suspend fun delete(entity: ProjectEntity) = projectDao.delete(entity)

    /**
     * Deletes multiple projects from the database by their IDs.
     */
    suspend fun deleteByIds(ids: List<Int>) = projectDao.deleteByIds(ids)

    suspend fun updateProjectDetailValues(
        id: Int,
        title: String,
        notes: String,
        totalRows: Int,
        imagePaths: List<String>,
        completedAt: Long?,
        updatedAt: Long
    ) {
        projectDao.updateProjectDetailValues(
            id = id,
            title = title,
            notes = notes,
            totalRows = totalRows,
            imagePaths = imagePaths,
            completedAt = completedAt,
            updatedAt = updatedAt
        )
    }

    suspend fun updateRowAndRepeatProjectDetailValues(
        id: Int,
        title: String,
        notes: String,
        repeatGoal: Int,
        rowsPerRepeat: Int,
        imagePaths: List<String>,
        completedAt: Long?,
        updatedAt: Long
    ) {
        projectDao.updateRowAndRepeatProjectDetailValues(
            id = id,
            title = title,
            notes = notes,
            repeatGoal = repeatGoal,
            rowsPerRepeat = rowsPerRepeat,
            imagePaths = imagePaths,
            completedAt = completedAt,
            updatedAt = updatedAt
        )
    }

    suspend fun updateSingleCounterValues(
        id: Int,
        stitchCount: Int,
        stitchAdjustment: Int,
        totalStitchesEver: Int,
        clearCompletedAt: Boolean,
        updatedAt: Long
    ) {
        projectDao.updateSingleCounterValues(
            id = id,
            stitchCount = stitchCount,
            stitchAdjustment = stitchAdjustment,
            totalStitchesEver = totalStitchesEver,
            clearCompletedAt = clearCompletedAt,
            updatedAt = updatedAt
        )
    }

    suspend fun updateDoubleCounterValues(
        id: Int,
        stitchCount: Int,
        stitchAdjustment: Int,
        rowCount: Int,
        rowAdjustment: Int,
        totalStitchesEver: Int,
        clearCompletedAt: Boolean,
        updatedAt: Long
    ) {
        projectDao.updateDoubleCounterValues(
            id = id,
            stitchCount = stitchCount,
            stitchAdjustment = stitchAdjustment,
            rowCount = rowCount,
            rowAdjustment = rowAdjustment,
            totalStitchesEver = totalStitchesEver,
            clearCompletedAt = clearCompletedAt,
            updatedAt = updatedAt
        )
    }

    suspend fun updateRowAndRepeatValues(
        id: Int,
        repeatCount: Int,
        rowCount: Int,
        rowsPerRepeat: Int,
        repeatGoal: Int,
        clearCompletedAt: Boolean,
        updatedAt: Long
    ) {
        projectDao.updateRowAndRepeatValues(
            id = id,
            repeatCount = repeatCount,
            rowCount = rowCount,
            rowsPerRepeat = rowsPerRepeat,
            repeatGoal = repeatGoal,
            clearCompletedAt = clearCompletedAt,
            updatedAt = updatedAt
        )
    }
}

