/*
   Copyright 2017 Anna Harrison

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package io.github.annaharri89.stitchcounter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ETASpare on 6/15/2017.
 */

public class StitchCounterDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "StitchCounter.db";

    //Statement to create a table
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + StitchCounterContract.CounterEntry.TABLE_NAME + " (" +
                    StitchCounterContract.CounterEntry._ID + " INTEGER PRIMARY KEY," +
                    StitchCounterContract.CounterEntry.COLUMN_TYPE + " TEXT," +
                    StitchCounterContract.CounterEntry.COLUMN_TITLE + " TEXT," +
                    StitchCounterContract.CounterEntry.COLUMN_STITCH_COUNTER_NUM + " TEXT," +
                    StitchCounterContract.CounterEntry.COLUMN_STITCH_ADJUSTMENT + " TEXT," +
                    StitchCounterContract.CounterEntry.COLUMN_ROW_COUNTER_NUM + " TEXT," +
                    StitchCounterContract.CounterEntry.COLUMN_ROW_ADJUSTMENT + " TEXT," +
                    StitchCounterContract.CounterEntry.COLUMN_TOTAL_ROWS + " TEXT)";

    //Statement to delete a table
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + StitchCounterContract.CounterEntry.TABLE_NAME;

    public StitchCounterDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    /* Returns all the projects in the table */
    public Cursor getAllProjects(SQLiteDatabase db, String[] PROJECTION, String SELECTION, String[] SELECTIONARGS, String SORTORDER){
        return db.query(
                StitchCounterContract.CounterEntry.TABLE_NAME,  // The table to query
                PROJECTION,                                     // The columns to return
                SELECTION,                                      // The columns for the WHERE clause
                SELECTIONARGS,                                  // The values for the WHERE clause
                null,                                           // don't group the rows
                null,                                           // don't filter by row groups
                SORTORDER                                       // The sort order
        );
    }
}
