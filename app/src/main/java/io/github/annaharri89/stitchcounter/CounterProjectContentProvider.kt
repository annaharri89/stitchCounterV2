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

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri

/**
 * Created by ETASpare on 7/3/2017.
 */

/* A custom Content Provider to do the database operations */
class CounterProjectContentProvider : ContentProvider() {
    var dbHelper: StitchCounterDbHelper? =
        null /* This content provider does the database operations by this object */
    var db: SQLiteDatabase? = null

    /* A callback method which is invoked when the content provider is starting up */
    override fun onCreate(): Boolean {
        dbHelper = StitchCounterDbHelper(context)
        db = dbHelper?.readableDatabase
        return true
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    /* A callback method which is by the default content uri */
    override fun query(
        uri: Uri,
        projection: Array<String?>?,
        selection: String?,
        selectionArgs: Array<String?>?,
        sortOrder: String?
    ): Cursor? {
        return if (uriMatcher.match(uri) == PROJECTS) {
            dbHelper?.getAllProjects(db!!, projection, selection, selectionArgs, sortOrder)
        } else {
            null
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        // TODO Auto-generated method stub
        return 0
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // TODO Auto-generated method stub
        return null
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        // TODO Auto-generated method stub
        return 0
    }

    companion object {
        const val PROVIDER_NAME: String = "io.github.annaharri89.stitchcounter.counterProject"

        /* A uri to do operations on cust_master table. A content provider is identified by its uri */
        val CONTENT_URI: Uri = Uri.parse("content://" + PROVIDER_NAME + "/projects")

        /* Constants to identify the requested operation */
        private const val PROJECTS = 1

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            uriMatcher.addURI(PROVIDER_NAME, "projects", PROJECTS)
        }
    }
}