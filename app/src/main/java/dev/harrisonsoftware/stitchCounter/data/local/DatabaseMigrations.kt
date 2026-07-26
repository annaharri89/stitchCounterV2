package dev.harrisonsoftware.stitchCounter.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `note` (
                    `_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `body` TEXT NOT NULL,
                    `created_at` INTEGER NOT NULL,
                    `updated_at` INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2)
}
