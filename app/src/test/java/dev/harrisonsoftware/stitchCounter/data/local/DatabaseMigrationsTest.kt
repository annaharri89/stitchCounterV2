package dev.harrisonsoftware.stitchCounter.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DatabaseMigrationsTest {

    @Test
    fun `migrate from 1 to 2 preserves entry table and creates note table`() {
        val database = openVersionOneDatabase()
        database.execSQL(
            """
            INSERT INTO entry (
                type, title, notes, stitch_counter_number, stitch_adjustment,
                row_counter_number, row_adjustment, total_rows, image_paths,
                created_at, updated_at, completed_at, total_stitches_ever
            ) VALUES (
                'single', 'Preserved project', 'project notes', 0, 1,
                0, 1, 0, '[]',
                100, 200, NULL, 0
            )
            """.trimIndent()
        )

        DatabaseMigrations.MIGRATION_1_2.migrate(database)

        database.query("SELECT COUNT(*) FROM entry").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        database.query("SELECT title FROM entry WHERE _id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Preserved project", cursor.getString(0))
        }
        database.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'note'"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("note", cursor.getString(0))
        }
        database.query("SELECT COUNT(*) FROM note").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        database.close()
    }

    @Test
    fun `MIGRATION_1_2 start and end versions are 1 and 2`() {
        assertEquals(1, DatabaseMigrations.MIGRATION_1_2.startVersion)
        assertEquals(2, DatabaseMigrations.MIGRATION_1_2.endVersion)
    }

    private fun openVersionOneDatabase(): SupportSQLiteDatabase {
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(
            RuntimeEnvironment.getApplication()
        )
            .name("migration-test-db")
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS entry (
                            _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            type TEXT NOT NULL,
                            title TEXT NOT NULL,
                            notes TEXT NOT NULL,
                            stitch_counter_number INTEGER NOT NULL,
                            stitch_adjustment INTEGER NOT NULL,
                            row_counter_number INTEGER NOT NULL,
                            row_adjustment INTEGER NOT NULL,
                            total_rows INTEGER NOT NULL,
                            image_paths TEXT NOT NULL,
                            created_at INTEGER NOT NULL,
                            updated_at INTEGER NOT NULL,
                            completed_at INTEGER,
                            total_stitches_ever INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) = Unit
            })
            .build()
        return FrameworkSQLiteOpenHelperFactory().create(configuration).writableDatabase
    }
}
