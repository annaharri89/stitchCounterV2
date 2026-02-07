package dev.harrisonsoftware.stitchCounter.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a knitting/crochet project stored in the local database.
 * 
 * This entity maps to the "entry" table and contains all the necessary fields
 * to track a project's progress including stitch counts, row counts, and adjustments.
 * 
 * @property id Primary key, auto-generated unique identifier
 * @property type Project type: "single" for single counter or "double" for double counter
 * @property title User-defined project name or title
 * @property stitchCounterNumber Current stitch count in the project
 * @property stitchAdjustment Increment value for stitch counter (default: 1)
 * @property rowCounterNumber Current row count in the project
 * @property rowAdjustment Increment value for row counter (default: 1)
 * @property totalRows Total number of rows planned for the project
 */
@Entity(tableName = "entry")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Int = 0,

    @ColumnInfo(name = "type")
    val type: String = "single", // "single" or "double"

    @ColumnInfo(name = "title")
    val title: String = "",

    @ColumnInfo(name = "stitch_counter_number")
    val stitchCounterNumber: Int = 0,

    @ColumnInfo(name = "stitch_adjustment")
    val stitchAdjustment: Int = 1,

    @ColumnInfo(name = "row_counter_number")
    val rowCounterNumber: Int = 0,

    @ColumnInfo(name = "row_adjustment")
    val rowAdjustment: Int = 1,

    @ColumnInfo(name = "total_rows")
    val totalRows: Int = 0,

    @ColumnInfo(name = "image_paths")
    val imagePaths: List<String> = emptyList(),
)

