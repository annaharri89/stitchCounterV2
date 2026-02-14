package dev.harrisonsoftware.stitchCounter.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteStatement
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE entry ADD COLUMN image_path TEXT"
            )
        }
    }
    
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE entry_new (
                    _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    type TEXT NOT NULL DEFAULT 'single',
                    title TEXT NOT NULL DEFAULT '',
                    stitch_counter_number INTEGER NOT NULL DEFAULT 0,
                    stitch_adjustment INTEGER NOT NULL DEFAULT 1,
                    row_counter_number INTEGER NOT NULL DEFAULT 0,
                    row_adjustment INTEGER NOT NULL DEFAULT 1,
                    total_rows INTEGER NOT NULL DEFAULT 0,
                    image_paths TEXT NOT NULL DEFAULT '[]'
                )
            """.trimIndent())
            
            val insertStatement: SupportSQLiteStatement = database.compileStatement("""
                INSERT INTO entry_new (
                    _id, type, title, stitch_counter_number, stitch_adjustment,
                    row_counter_number, row_adjustment, total_rows, image_paths
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent())
            
            database.query("SELECT * FROM entry").use { cursor ->
                val idIndex = cursor.getColumnIndex("_id")
                val typeIndex = cursor.getColumnIndex("type")
                val titleIndex = cursor.getColumnIndex("title")
                val stitchCounterIndex = cursor.getColumnIndex("stitch_counter_number")
                val stitchAdjustmentIndex = cursor.getColumnIndex("stitch_adjustment")
                val rowCounterIndex = cursor.getColumnIndex("row_counter_number")
                val rowAdjustmentIndex = cursor.getColumnIndex("row_adjustment")
                val totalRowsIndex = cursor.getColumnIndex("total_rows")
                val imagePathIndex = cursor.getColumnIndex("image_path")
                
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(idIndex)
                    val type = cursor.getString(typeIndex) ?: "single"
                    val title = cursor.getString(titleIndex) ?: ""
                    val stitchCounter = cursor.getInt(stitchCounterIndex)
                    val stitchAdjustment = cursor.getInt(stitchAdjustmentIndex)
                    val rowCounter = cursor.getInt(rowCounterIndex)
                    val rowAdjustment = cursor.getInt(rowAdjustmentIndex)
                    val totalRows = cursor.getInt(totalRowsIndex)
                    val imagePath = cursor.getString(imagePathIndex)
                    
                    val imagePathsJson = if (imagePath != null && imagePath.isNotBlank()) {
                        Json.encodeToString(listOf(imagePath))
                    } else {
                        "[]"
                    }
                    
                    insertStatement.clearBindings()
                    insertStatement.bindLong(1, id.toLong())
                    insertStatement.bindString(2, type)
                    insertStatement.bindString(3, title)
                    insertStatement.bindLong(4, stitchCounter.toLong())
                    insertStatement.bindLong(5, stitchAdjustment.toLong())
                    insertStatement.bindLong(6, rowCounter.toLong())
                    insertStatement.bindLong(7, rowAdjustment.toLong())
                    insertStatement.bindLong(8, totalRows.toLong())
                    insertStatement.bindString(9, imagePathsJson)
                    insertStatement.executeInsert()
                }
            }
            
            insertStatement.close()
            
            database.execSQL("DROP TABLE entry")
            database.execSQL("ALTER TABLE entry_new RENAME TO entry")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE entry ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE entry ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE entry ADD COLUMN completed_at INTEGER")
            database.execSQL("ALTER TABLE entry ADD COLUMN total_stitches_ever INTEGER NOT NULL DEFAULT 0")

            val now = System.currentTimeMillis()
            database.execSQL("UPDATE entry SET total_stitches_ever = stitch_counter_number, created_at = $now, updated_at = $now")
        }
    }
}
