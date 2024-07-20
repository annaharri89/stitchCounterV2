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
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import java.util.ArrayList;

/**
 * Created by ETASpare on 7/3/2017.
 */

public class DeleteFromDb extends AsyncTask<ArrayList<String>, Void, Integer> {

    private Context mContext;

    public DeleteFromDb(Context context) {
        this.mContext = context;
    }

    @Override
    protected Integer doInBackground(ArrayList<String>... ids) {
        StitchCounterDbHelper dbHelper = new StitchCounterDbHelper(this.mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Gets the data repository in write mode
        try {
            for (String id: ids[0]) {
                db.delete(StitchCounterContract.CounterEntry.TABLE_NAME,
                        "_id = ?",
                        new String[]{id});
            }
        } finally {
            db.close();
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer z) {
        LibraryActivity context = (LibraryActivity) this.mContext;
        context.updateCursor();
    }
}
