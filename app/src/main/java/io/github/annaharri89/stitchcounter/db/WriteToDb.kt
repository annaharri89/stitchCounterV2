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
package io.github.annaharri89.stitchcounter.db

import android.content.ContentValues
import android.content.Context
import android.os.AsyncTask
import io.github.annaharri89.stitchcounter.dataObjects.OldCounter
import io.github.annaharri89.stitchcounter.enums.ProjectTypes

// todo stitchCounterV2 bug: when you make a change to a counter and then go to the library screen and back to the project it hasn't saved the last change to the project

class WriteToDb(private val mContext: Context) : AsyncTask<OldCounter, Void?, Void?>() {//todo stitchCounterV2 redo the async task and get rid of the context that is causing a leak
    override fun doInBackground(vararg oldCounter: OldCounter): Void? {
        val dbHelper = StitchCounterDbHelper(this.mContext)
        val db = dbHelper.writableDatabase // Gets the data repository in write mode
        try {
            val values = if (oldCounter.size > 1) {
                doubleCounterUpdate(*oldCounter)
            } else {
                singleCounterUpdate(*oldCounter)
            }
            if (values[StitchCounterContract.CounterEntry._ID] != null) {
                val strFilter =
                    StitchCounterContract.CounterEntry._ID + "=" + values[StitchCounterContract.CounterEntry._ID]
                db.update(StitchCounterContract.CounterEntry.TABLE_NAME, values, strFilter, null)
            } else {
                oldCounter[0]?.ID =
                    db.insert(StitchCounterContract.CounterEntry.TABLE_NAME, null, values).toInt()
            }
        } finally {
            db.close()
        }
        return null
    }


    /* Called when a single counter object is passed in the asynctask */
    private fun singleCounterUpdate(vararg oldCounter: OldCounter): ContentValues {
        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        if (oldCounter[0].ID > 0) {
            values.put(StitchCounterContract.CounterEntry._ID, oldCounter[0].ID)
        }
        values.put(StitchCounterContract.CounterEntry.COLUMN_TYPE, ProjectTypes.SINGLE.name)
        values.put(StitchCounterContract.CounterEntry.COLUMN_TITLE, oldCounter[0].projectName)
        values.put(
            StitchCounterContract.CounterEntry.COLUMN_STITCH_COUNTER_NUM,
            oldCounter[0].counterNumber
        )
        values.put(
            StitchCounterContract.CounterEntry.COLUMN_STITCH_ADJUSTMENT,
            oldCounter[0].adjustment
        )
        values.put(StitchCounterContract.CounterEntry.COLUMN_ROW_COUNTER_NUM, "")
        values.put(StitchCounterContract.CounterEntry.COLUMN_ROW_ADJUSTMENT, "")
        values.put(StitchCounterContract.CounterEntry.COLUMN_TOTAL_ROWS, "")
        return values
    }

    /* Called when two counter objects are passed in the asynctask */
    protected fun doubleCounterUpdate(vararg oldCounter: OldCounter): ContentValues {
        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        if (oldCounter[0].ID > 0) {
            values.put(StitchCounterContract.CounterEntry._ID, oldCounter[0].ID)
        }
        values.put(StitchCounterContract.CounterEntry.COLUMN_TYPE, ProjectTypes.DOUBLE.name)
        values.put(StitchCounterContract.CounterEntry.COLUMN_TITLE, oldCounter[1].projectName)
        values.put(
            StitchCounterContract.CounterEntry.COLUMN_STITCH_COUNTER_NUM,
            oldCounter[0].counterNumber
        )
        values.put(
            StitchCounterContract.CounterEntry.COLUMN_STITCH_ADJUSTMENT,
            oldCounter[0].adjustment
        )
        values.put(
            StitchCounterContract.CounterEntry.COLUMN_ROW_COUNTER_NUM,
            oldCounter[1].counterNumber
        )
        values.put(StitchCounterContract.CounterEntry.COLUMN_ROW_ADJUSTMENT, oldCounter[1].adjustment)
        values.put(StitchCounterContract.CounterEntry.COLUMN_TOTAL_ROWS, oldCounter[1].totalRows)
        return values
    }
}
