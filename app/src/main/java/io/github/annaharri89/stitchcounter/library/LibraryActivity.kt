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
package io.github.annaharri89.stitchcounter.library

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.Button
import android.widget.CheckBox
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import io.github.annaharri89.stitchcounter.Counter
import io.github.annaharri89.stitchcounter.CounterProjectContentProvider
import io.github.annaharri89.stitchcounter.DeleteFromDb
import io.github.annaharri89.stitchcounter.R
import io.github.annaharri89.stitchcounter.StitchCounterContract
import io.github.annaharri89.stitchcounter.Utils
import io.github.annaharri89.stitchcounter.WriteToDb
import io.github.annaharri89.stitchcounter.doubleCounter.DoubleCounterActivity
import io.github.annaharri89.stitchcounter.singleCounter.SingleCounterActivity

class LibraryActivity : FragmentActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    private var deleteSingle: Button? = null
    private var deleteMany: Button? = null
    private var cancelMany: Button? = null
    private val context: Context = this
    protected var mAdapter: CounterAdapter? = null
    private var mListView: ListView? = null
    private var tempCursor: Cursor? = null
    private val deleteManyArray = ArrayList<String>()
    private var deleteManyMode = false
    private var layout: ConstraintLayout? = null
    var helpMode: Boolean = false
    private var helpModeArray: ArrayList<View> = arrayListOf()
    private val utils = Utils(this)

    /*
    Defines a projection that specifies which columns from the database will actually
    be used for the query.
    */
    var PROJECTION: Array<String> = arrayOf(
        StitchCounterContract.CounterEntry._ID,
        StitchCounterContract.CounterEntry.COLUMN_TYPE,
        StitchCounterContract.CounterEntry.COLUMN_TITLE,
        StitchCounterContract.CounterEntry.COLUMN_STITCH_COUNTER_NUM,
        StitchCounterContract.CounterEntry.COLUMN_STITCH_ADJUSTMENT,
        StitchCounterContract.CounterEntry.COLUMN_ROW_COUNTER_NUM,
        StitchCounterContract.CounterEntry.COLUMN_ROW_ADJUSTMENT,
        StitchCounterContract.CounterEntry.COLUMN_TOTAL_ROWS
    )

    /* Sort the results in the Cursor in ascending order */
    var SORTORDER: String = StitchCounterContract.CounterEntry.COLUMN_TITLE + " ASC"

    /*
    Was for the cursor adapter, specifies which columns go into which views, but is actually
    unused because of the CounterAdapter method getView overrides them. Still needed to pass
    to mAdpter when it's initialized.
    */
    var fromColumns: Array<String> = arrayOf(StitchCounterContract.CounterEntry.COLUMN_TITLE)
    var toViews: IntArray = intArrayOf(R.id.text1)

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        menu.findItem(R.id.action_delete).setVisible(true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.title) {
            resources.getString(R.string.action_new_counter) -> utils.openMainActivity()
            resources.getString(R.string.action_help) -> utils.openHelpMode("LibraryActivity", helpModeArray)
            resources.getString(R.string.action_delete) -> turnOnDeleteManyMode()
            resources.getString(R.string.action_library) -> utils.openLibrary("LibraryActivity")
            resources.getString(R.string.action_settings) -> utils.openSettings()
            else -> return super.onOptionsItemSelected(item);
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        utils.updateTheme(false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)
        val myToolbar = findViewById<View>(R.id.toolbar_main) as Toolbar
        setActionBar(myToolbar)

        /* Help Mode Setup*/
        layout = findViewById<View>(R.id.layout) as ConstraintLayout
        val help1 = findViewById<View>(R.id.help_library_activity_1) as TextView
        val help2 = findViewById<View>(R.id.help_library_activity_2) as TextView
        val tip1 = findViewById<View>(R.id.help_library_activity_1_tip)
        helpModeArray = ArrayList()
        helpModeArray!!.add(help1)
        helpModeArray!!.add(help2)
        helpModeArray!!.add(tip1)

        /* Setup Delete and Cancel buttons for Delte Many Mode*/
        deleteMany = findViewById<View>(R.id.delete_many) as Button
        cancelMany = findViewById<View>(R.id.cancel_many) as Button
        deleteMany!!.setOnClickListener { v -> deleteMany(v) }
        cancelMany!!.setOnClickListener { v -> turnOffDeleteManyMode(v) }

        /* Setup listview and adapter, setup empty view for listview*/
        val noSavedProjects = findViewById<View>(R.id.empty_list_view) as TextView
        mListView = findViewById<View>(R.id.list) as ListView
        mListView!!.emptyView = noSavedProjects
        setUpAdapter()

        /* Creating a loader for populating listview from sqlite database */
        /* This statement invokes the method onCreatedLoader() */
        supportLoaderManager.initLoader(0, null, this)

        /*
        Takes the returned data (counters) and saves them to the database. Resets the adapter and
        restarts the loader so that the list has access to the most recent data. Works for both single
        counter and double counter. Used when a new counter is created and then the library is
        accessed through the drop down menu.
        */
        val extras = intent.extras
        if (extras != null) {
            val extractedData = extras.getParcelableArrayList<Counter>("counters")
            saveCounter(extractedData)
        }

        /* Closes Help Mode when listview is empty, hides the annotation bubbles */
        layout!!.setOnTouchListener { v, event ->
            closeHelpMode()
            false
        }
        /* Closes Help Mode when listview has list items, hides the annotation bubbles */
        mListView!!.setOnTouchListener { v, event ->
            closeHelpMode()
            false
        }

        /*
        + When deleteManyMode is off, parses db data for appropriate item and sends the data to
          appropriate activity (SingleCounterActivity or DoubleCounterActivity)/ starts the activity.
        + When deleteManyMode is on, checks and adds id to deleteManyArray or unchecks the check box
          and removes id from deleteManyArray
        */
        mListView!!.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            try {
                tempCursor = parent.getItemAtPosition(position) as? Cursor
                tempCursor?.let { csr ->
                    if (!deleteManyMode) {

                        val _id =
                            csr.getInt(csr.getColumnIndexOrThrow(StitchCounterContract.CounterEntry._ID))
                        val type =
                            csr.getString(csr.getColumnIndexOrThrow(StitchCounterContract.CounterEntry.COLUMN_TYPE))
                        val name =
                            csr.getString(csr.getColumnIndexOrThrow(StitchCounterContract.CounterEntry.COLUMN_TITLE))
                        val stitch_counter_number =
                            csr.getInt(csr.getColumnIndexOrThrow(StitchCounterContract.CounterEntry.COLUMN_STITCH_COUNTER_NUM))
                        val stitch_adjustment =
                            csr.getInt(csr.getColumnIndexOrThrow(StitchCounterContract.CounterEntry.COLUMN_STITCH_ADJUSTMENT))
                        val row_counter_number =
                            csr.getInt(csr.getColumnIndexOrThrow(StitchCounterContract.CounterEntry.COLUMN_ROW_COUNTER_NUM))
                        val row_adjustment =
                            csr.getInt(csr.getColumnIndexOrThrow(StitchCounterContract.CounterEntry.COLUMN_ROW_ADJUSTMENT))
                        val total_rows =
                            csr.getInt(csr.getColumnIndexOrThrow(StitchCounterContract.CounterEntry.COLUMN_TOTAL_ROWS))

                        val extras = Bundle()
                        when (type) {
                            "Double" -> {
                                extras.putInt("_id", _id)
                                extras.putString("name", name)
                                extras.putInt("stitch_counter_number", stitch_counter_number)
                                extras.putInt("stitch_adjustment", stitch_adjustment)
                                extras.putInt("row_counter_number", row_counter_number)
                                extras.putInt("row_adjustment", row_adjustment)
                                extras.putInt("total_rows", total_rows)

                                val intentDouble =
                                    Intent(baseContext, DoubleCounterActivity::class.java)
                                intentDouble.putExtras(extras)
                                startActivityForResult(intentDouble, 1)
                            }

                            "Single" -> {
                                extras.putInt("_id", _id)
                                extras.putString("name", name)
                                extras.putInt("stitch_counter_number", stitch_counter_number)
                                extras.putInt("stitch_adjustment", stitch_adjustment)

                                val intentSingle =
                                    Intent(baseContext, SingleCounterActivity::class.java)
                                intentSingle.putExtras(extras)
                                startActivityForResult(intentSingle, 1)
                            }
                            else -> {}
                        }
                    } else {
                        val checkBox = view.findViewById<View>(R.id.checkBox) as CheckBox
                        if (checkBox.isChecked) {
                            checkBox.isChecked = false
                            deleteManyArray.remove(
                                csr.getString(
                                    csr.getColumnIndexOrThrow(
                                        StitchCounterContract.CounterEntry._ID
                                    )
                                )
                            )
                        } else {
                            checkBox.isChecked = true
                            deleteManyArray.add(
                                csr.getString(
                                    csr.getColumnIndexOrThrow(
                                        StitchCounterContract.CounterEntry._ID
                                    )
                                )
                            )
                        }
                    }
                }
            } catch (e: IllegalArgumentException) {
                //todo stitchCounterV2 show error that we couldn't retreive the saved project
            }
        }

        /*
        Called when a list item is long clicked. If not in deleteManyMode, sets the tempCursor,
        sets the delete button visible, sets the onclicklistener for the delete button,
        sets previously visible delete buttons invisible. Calls closeHelpMode
        */
        mListView!!.onItemLongClickListener =
            OnItemLongClickListener { parent, view, position, id ->
                closeHelpMode()
                if (deleteManyMode) {
                    return@OnItemLongClickListener false
                }
                tempCursor = parent.getItemAtPosition(position) as Cursor
                if (deleteSingle != null) {
                    deleteSingle!!.visibility = View.INVISIBLE
                }
                deleteSingle = view.findViewById<View>(R.id.button_delete) as Button
                deleteSingle!!.setOnClickListener(deleteClickListener)
                deleteSingle!!.visibility = View.VISIBLE
                true
            }
    }

    /*
    Called when the delete button is clicked. Sets the delete button invisible and calls
    deleteFromDb AsyncTask
    */
    private val deleteClickListener = View.OnClickListener { v ->
        try {
            tempCursor?.let { csr ->
                val id =
                    csr.getString(csr.getColumnIndexOrThrow(StitchCounterContract.CounterEntry._ID))
                val ids = ArrayList<String>()
                ids.add(id)
                v.visibility = View.INVISIBLE
                val deleteFromDb = DeleteFromDb(context)
                deleteFromDb.execute(ids)
            }
        } catch (e: IllegalArgumentException) {
            //todo stitchCounterV2 show error saying we have failed to delete the app
        }
    }

    /*
    Called when the deleteMany button is pressed. Sends the deleteManyArray to the deleteFromDb
    AsyncTask if there's at least 1 id in the array and then turns off deleteManyMode.
    */
    protected fun deleteMany(view: View?) {
        if (deleteManyArray.size > 0) {
            val deleteFromDb = DeleteFromDb(context)
            deleteFromDb.execute(deleteManyArray)
        }
        turnOffDeleteManyMode(view)
    }

    /*
    Sets the bottom edge of the list view to the edge (top or bottom, passed through
    constrainingEdge) of the element (referenced by elementID).
    */
    protected fun setListConstraints(elementID: Int, constrainingEdge: Int) {
        val set = ConstraintSet()
        set.clone(layout)
        set.connect(R.id.list, ConstraintSet.BOTTOM, elementID, constrainingEdge, 0)
        set.applyTo(layout)
    }

    /*
    + Sets deleteManyMode to true, thereby changing how the itemClickListener handles clicks and
      blocking long clicks so that single delete buttons cannot appear.
    + Calls setListConstraints to connect the bottom of list to the top of the delete_many button
    + Sets the delete and cancel buttons to visible
    + Calls closeHelpMode
    */
    fun turnOnDeleteManyMode() {
        closeHelpMode()
        deleteManyMode = true
        setListConstraints(R.id.delete_many, ConstraintSet.TOP)
        deleteMany!!.visibility = View.VISIBLE
        cancelMany!!.visibility = View.VISIBLE
    }

    /*
    + Sets the deleteManyMode to false, thereby changing how the itemClickListener handles clicks and
      allowing long clicks to happen so single delete buttons can appear
    + Calls setListConstraints to connect the bottom of list to the bottom of the constraint layout.
    + Sets the delete and cancel buttons to invisible
    */
    protected fun turnOffDeleteManyMode(view: View?) {
        deleteManyMode = false
        setListConstraints(R.id.layout, ConstraintSet.BOTTOM)
        deleteMany!!.visibility = View.INVISIBLE
        cancelMany!!.visibility = View.INVISIBLE
    }

    /* Closes help mode, hides the annotation bubbles */
    protected fun closeHelpMode() {
        if (helpMode) {
            for (view in helpModeArray!!) {
                if (view != null) {
                    view.visibility = View.INVISIBLE
                }
            }
            helpMode = false
        }
    }

    /*
    Called from DeleteFromDb AsyncTask during onPostExecute (after project gets deleted from db).
    Updates the listview by removing appropriate project item(s)
    */
    fun updateCursor() {
        supportLoaderManager.restartLoader(0, null, this)
        mAdapter!!.notifyDataSetChanged()
    }

    /*
    Create an empty adapter that will be used to display the loaded data.
    Pass null for the cursor, then update it in onLoadFinished()
    Sets the adapter to the ListView.
    */
    protected fun setUpAdapter() {
        mAdapter = CounterAdapter(
            baseContext,
            R.layout.list_item_single_counter,
            null,
            fromColumns,
            toViews,
            0
        )
        mListView!!.adapter = mAdapter
    }

    /*
    Saves counters to the db when the library is accessed, either through the back button from a
    counter or through the library menu item from a counter.
    */
    protected fun saveCounter(extractedData: ArrayList<Counter>?) {
        val stitchCounter = extractedData!![0]
        var rowCounter: Counter? = null
        if (extractedData.size > 1) {
            rowCounter = extractedData[1]
        }
        val writeToDb = WriteToDb(this.context)
        if (stitchCounter != null && rowCounter != null) {
            writeToDb.execute(stitchCounter, rowCounter)
        } else if (stitchCounter != null) {
            writeToDb.execute(stitchCounter)
        }
        startActivity(Intent(this, LibraryActivity::class.java))
    }

    /*
    Takes the returned data (counters) and saves them to the database. Resets the adapter and
    restarts the loader so that the list has access to the most recent data. Works for both single
    counter and double counter. Called when the back button is pressed from a counter that was
    loaded from the library.
    */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    val extractedData = data.getParcelableArrayListExtra<Counter>("counters")
                    saveCounter(extractedData)
                }
            }
        }
    }

    /* A callback method invoked by the loader when initLoader() is called */
    override fun onCreateLoader(arg0: Int, arg1: Bundle?): Loader<Cursor> {
        val uri = CounterProjectContentProvider.CONTENT_URI
        return CursorLoader(this, uri, PROJECTION, null, null, SORTORDER)
    }

    /* A callback method, invoked after the requested content provider has returned all the data */
    override fun onLoadFinished(arg0: Loader<Cursor>, arg1: Cursor) {
        mAdapter!!.swapCursor(arg1)
    }

    override fun onLoaderReset(arg0: Loader<Cursor>) {
        mAdapter!!.swapCursor(null)
    }

    public override fun onResume() {
        /* Allows list to function properly when accessed through back button */
        supportLoaderManager.restartLoader(0, null, this)
        super.onResume()
    }

    public override fun onStop() {
        if (tempCursor != null && !tempCursor!!.isClosed) {
            tempCursor!!.close()
        }
        super.onStop()
    }

    public override fun onDestroy() {
        if (tempCursor != null && !tempCursor!!.isClosed) {
            tempCursor!!.close()
        }
        super.onDestroy()
    }

    inner class CounterAdapter(
        context: Context,
        layout: Int,
        c: Cursor?,
        from: Array<String>?,
        to: IntArray?,
        flags: Int
    ) : SimpleCursorAdapter(context, layout, c, from, to, flags) {
        var counterContext: Context = context
        var counterCursor: Cursor? = null

        override fun getViewTypeCount(): Int {
            return 4
        }

        override fun getItemViewType(position: Int): Int {
            counterCursor = mAdapter?.getCursor()
            counterCursor?.moveToPosition(position)
            val type = counterCursor?.getString(1)
            if (type == "Double" && !deleteManyMode) {
                return 0
            } else if (type == "Single" && !deleteManyMode) {
                return 1
            } else if (type == "Double" && deleteManyMode) {
                return 2
            } else if (type == "Single" && deleteManyMode) {
                return 3
            }
            return 0
        }

        /*
        Handles the four different list item layouts: double counter, double counter in delete many
        mode, single counter, and single counter in delete many mode.
        */

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var row = convertView
            val type = getItemViewType(position)
            counterCursor = mAdapter!!.getCursor()
            counterCursor?.moveToPosition(position)
            if (row == null) {
                // Inflate the layout according to the view type
                val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val textTitle: TextView
                val checkTitle: CheckBox
                val progress: ProgressBar
                when (type) {
                    0 -> {
                        row = inflater.inflate(R.layout.list_item_double_counter, parent, false)

                        textTitle = row.findViewById<View>(R.id.text2) as TextView
                        progress = row.findViewById<View>(R.id.progressBar_list_item) as ProgressBar

                        textTitle.text = counterCursor?.getString(2)
                        progress.max = counterCursor?.getInt(7) ?: -1
                        progress.progress = counterCursor?.getInt(5) ?: -1
                    }

                    1 -> {
                        row = inflater.inflate(R.layout.list_item_single_counter, parent, false)

                        textTitle = row.findViewById<View>(R.id.text1) as TextView

                        textTitle.text = counterCursor?.getString(2)
                    }

                    2 -> {
                        row =
                            inflater.inflate(R.layout.list_item_double_counter_check, parent, false)

                        checkTitle = row.findViewById<View>(R.id.checkBox) as CheckBox
                        progress = row.findViewById<View>(R.id.progressBar_list_item) as ProgressBar

                        checkTitle.text = counterCursor?.getString(2)
                        progress.max = counterCursor?.getInt(7) ?: -1
                        progress.progress = counterCursor?.getInt(5) ?: -1
                    }

                    3 -> {
                        row =
                            inflater.inflate(R.layout.list_item_single_counter_check, parent, false)
                        checkTitle = row.findViewById<View>(R.id.checkBox) as CheckBox

                        checkTitle.text = counterCursor?.getString(2)
                    }
                }
            }
            return row ?: super.getView(position, convertView, parent)
        }
    }
}

