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

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

/**
 * Created by ETASpare on 6/15/2017.
 */

public class WriteToDb extends AsyncTask<Counter, Void, Void> {

    private Context mContext;

    public WriteToDb (Context context) {
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(Counter... counter) {
        StitchCounterDbHelper dbHelper = new StitchCounterDbHelper(this.mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Gets the data repository in write mode
        try {
            ContentValues values;
            if (counter.length > 1) {
                values = doubleCounterUpdate(counter);
            } else {
                values = singleCounterUpdate(counter);
            }
            if (values.get(StitchCounterContract.CounterEntry._ID) != null) {
                String strFilter = StitchCounterContract.CounterEntry._ID + "=" + values.get(StitchCounterContract.CounterEntry._ID);
                db.update(StitchCounterContract.CounterEntry.TABLE_NAME, values, strFilter, null);
            } else {
                counter[0].ID = (int) db.insert(StitchCounterContract.CounterEntry.TABLE_NAME, null, values);
            }
        } finally {
            db.close();
        }
        return null;
    }

    /* Called when a single counter object is passed in the asynctask */
    protected ContentValues singleCounterUpdate(Counter... counter) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        if (counter[0].ID > 0) {
            values.put(StitchCounterContract.CounterEntry._ID, counter[0].ID);
        }
        values.put(StitchCounterContract.CounterEntry.COLUMN_TYPE, "Single");
        values.put(StitchCounterContract.CounterEntry.COLUMN_TITLE, counter[0].projectName);
        values.put(StitchCounterContract.CounterEntry.COLUMN_STITCH_COUNTER_NUM, counter[0].counterNumber);
        values.put(StitchCounterContract.CounterEntry.COLUMN_STITCH_ADJUSTMENT, counter[0].adjustment);
        values.put(StitchCounterContract.CounterEntry.COLUMN_ROW_COUNTER_NUM, "");
        values.put(StitchCounterContract.CounterEntry.COLUMN_ROW_ADJUSTMENT, "");
        values.put(StitchCounterContract.CounterEntry.COLUMN_TOTAL_ROWS, "");
        return values;
    }

    /* Called when two counter objects are passed in the asynctask */
    protected ContentValues doubleCounterUpdate(Counter... counter) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        if (counter[0].ID > 0) {
            values.put(StitchCounterContract.CounterEntry._ID, counter[0].ID);
        }
        values.put(StitchCounterContract.CounterEntry.COLUMN_TYPE, "Double");
        values.put(StitchCounterContract.CounterEntry.COLUMN_TITLE, counter[1].projectName);
        values.put(StitchCounterContract.CounterEntry.COLUMN_STITCH_COUNTER_NUM, counter[0].counterNumber);
        values.put(StitchCounterContract.CounterEntry.COLUMN_STITCH_ADJUSTMENT, counter[0].adjustment);
        values.put(StitchCounterContract.CounterEntry.COLUMN_ROW_COUNTER_NUM, counter[1].counterNumber);
        values.put(StitchCounterContract.CounterEntry.COLUMN_ROW_ADJUSTMENT, counter[1].adjustment);
        values.put(StitchCounterContract.CounterEntry.COLUMN_TOTAL_ROWS, counter[1].totalRows);
        return values;
    }
}
