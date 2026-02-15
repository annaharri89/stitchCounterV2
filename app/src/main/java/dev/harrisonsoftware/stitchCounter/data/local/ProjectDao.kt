package dev.harrisonsoftware.stitchCounter.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for ProjectEntity database operations.
 * 
 * This interface defines all database operations for managing knitting/crochet projects.
 * All write operations are suspend functions to ensure they run on background threads.
 * Query operations return Flow for reactive UI updates.
 */
@Dao
interface ProjectDao {
    
    /**
     * Observes all projects in the database with reactive updates.
     * 
     * Returns a Flow that emits the complete list of projects whenever the database changes.
     * Projects are ordered by ID in descending order (most recent first).
     * 
     * @return Flow<List<ProjectEntity>> Reactive stream of all projects
     */
    @Query("SELECT * FROM entry ORDER BY _id DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    /**
     * Retrieves a specific project by its unique ID.
     * 
     * @param id The unique identifier of the project to retrieve
     * @return ProjectEntity? The project if found, null otherwise
     */
    @Query("SELECT * FROM entry WHERE _id = :id")
    suspend fun getById(id: Int): ProjectEntity?

    /**
     * Inserts a new project or replaces an existing one.
     * 
     * Uses REPLACE conflict strategy, which means if a project with the same ID exists,
     * it will be replaced with the new data. If no conflict occurs, a new project is inserted.
     * 
     * @param entity The project entity to insert or replace
     * @return Long The row ID of the inserted/updated record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ProjectEntity): Long

    /**
     * Deletes a project from the database.
     * 
     * The project must exist in the database. If the project doesn't exist,
     * this operation will have no effect.
     * 
     * @param entity The project entity to delete
     */
    @Delete
    suspend fun delete(entity: ProjectEntity)

    /**
     * Deletes multiple projects from the database by their IDs.
     * 
     * Projects that don't exist will be silently ignored.
     * 
     * @param ids List of project IDs to delete
     */
    @Query("DELETE FROM entry WHERE _id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    @Query("""
        UPDATE entry 
        SET stitch_counter_number = :stitchCount,
            stitch_adjustment = :stitchAdjustment,
            total_stitches_ever = :totalStitchesEver,
            updated_at = :updatedAt
        WHERE _id = :id
    """)
    suspend fun updateSingleCounterValues(
        id: Int,
        stitchCount: Int,
        stitchAdjustment: Int,
        totalStitchesEver: Int,
        updatedAt: Long
    )

    @Query("""
        UPDATE entry 
        SET stitch_counter_number = :stitchCount,
            stitch_adjustment = :stitchAdjustment,
            row_counter_number = :rowCount,
            row_adjustment = :rowAdjustment,
            total_stitches_ever = :totalStitchesEver,
            updated_at = :updatedAt
        WHERE _id = :id
    """)
    suspend fun updateDoubleCounterValues(
        id: Int,
        stitchCount: Int,
        stitchAdjustment: Int,
        rowCount: Int,
        rowAdjustment: Int,
        totalStitchesEver: Int,
        updatedAt: Long
    )
}

