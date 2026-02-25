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

    suspend fun updateSingleCounterValues(
        id: Int,
        stitchCount: Int,
        stitchAdjustment: Int,
        totalStitchesEver: Int,
        updatedAt: Long
    ) = projectDao.updateSingleCounterValues(id, stitchCount, stitchAdjustment, totalStitchesEver, updatedAt)

    suspend fun updateDoubleCounterValues(
        id: Int,
        stitchCount: Int,
        stitchAdjustment: Int,
        rowCount: Int,
        rowAdjustment: Int,
        totalStitchesEver: Int,
        updatedAt: Long
    ) = projectDao.updateDoubleCounterValues(id, stitchCount, stitchAdjustment, rowCount, rowAdjustment, totalStitchesEver, updatedAt)
}

