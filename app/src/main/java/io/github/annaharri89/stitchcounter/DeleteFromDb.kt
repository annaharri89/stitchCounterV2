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
package io.github.annaharri89.stitchcounter

import android.content.Context
import android.os.AsyncTask
import io.github.annaharri89.stitchcounter.db.StitchCounterContract
import io.github.annaharri89.stitchcounter.db.StitchCounterDbHelper
import io.github.annaharri89.stitchcounter.library.LibraryActivity

/**
 * Created by ETASpare on 7/3/2017.
 */
class DeleteFromDb(private val mContext: Context) : AsyncTask<ArrayList<String?>, Void?, Int>() {
    override fun doInBackground(vararg ids: ArrayList<String?>): Int {
        val dbHelper = StitchCounterDbHelper(this.mContext)
        val db = dbHelper.writableDatabase // Gets the data repository in write mode
        try {
            for (id in ids[0]) {
                db.delete(
                    StitchCounterContract.CounterEntry.TABLE_NAME,
                    "_id = ?",
                    arrayOf(id)
                )
            }
        } finally {
            db.close()
        }
        return 0
    }

    override fun onPostExecute(z: Int) {
        val context = mContext as LibraryActivity
        context.updateCursor()
    }
}
