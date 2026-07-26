package dev.harrisonsoftware.stitchCounter.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM note ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM note WHERE _id = :id")
    suspend fun getById(id: Int): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: NoteEntity): Long

    @Delete
    suspend fun delete(entity: NoteEntity)
}
