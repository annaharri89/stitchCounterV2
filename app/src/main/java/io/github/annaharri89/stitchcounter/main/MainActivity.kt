/*
   Copyright 2024 Anna Harrison
 */
package io.github.annaharri89.stitchcounter.main

import android.content.Intent
import android.database.Cursor
import android.database.DatabaseUtils
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import io.github.annaharri89.stitchcounter.Consts
import io.github.annaharri89.stitchcounter.dataObjects.OldCounter
import io.github.annaharri89.stitchcounter.db.CounterProjectContentProvider
import io.github.annaharri89.stitchcounter.db.DeleteFromDb
import io.github.annaharri89.stitchcounter.R
import io.github.annaharri89.stitchcounter.dataObjects.Counter
import io.github.annaharri89.stitchcounter.dataObjects.StyledTextData
import io.github.annaharri89.stitchcounter.db.StitchCounterContract
import io.github.annaharri89.stitchcounter.utilities.Utils
import io.github.annaharri89.stitchcounter.db.WriteToDb
import io.github.annaharri89.stitchcounter.doubleCounter.DoubleCounterActivity
import io.github.annaharri89.stitchcounter.enums.DBFields
import io.github.annaharri89.stitchcounter.enums.ProjectTypes
import io.github.annaharri89.stitchcounter.sharedComposables.Card
import io.github.annaharri89.stitchcounter.sharedComposables.NavBar
import io.github.annaharri89.stitchcounter.sharedComposables.StyledText
import io.github.annaharri89.stitchcounter.singleCounter.SingleCounterActivity
import io.github.annaharri89.stitchcounter.theme.STTheme
import io.github.annaharri89.stitchcounter.theme.loraRegular
import io.github.annaharri89.stitchcounter.utilities.capitalized


class MainActivity : FragmentActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    @JvmField
    var helpMode: Boolean = false
    private var helpModeArray: ArrayList<View> = arrayListOf()
    private val deleteManyArray = ArrayList<String?>()
    private var deleteManyMode = false
    private val utils = Utils(this)
    private lateinit var vm: MainViewModel


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
    var SORTBYASCENDING: String = StitchCounterContract.CounterEntry.COLUMN_TITLE + " ASC"
    //todo stitchCounterV2 make it so they can sort by different fields
    //todo stitchCounterV2 make it accessible for all people

    /*
    Was for the cursor adapter, specifies which columns go into which views, but is actually
    unused because of the CounterAdapter method getView overrides them. Still needed to pass
    to mAdpter when it's initialized.
    */
    var mFrom: IntArray? = null
    var fromColumns: Array<String> = arrayOf(StitchCounterContract.CounterEntry.COLUMN_TITLE)//todo stitchCounterV2 remove

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        menu.findItem(R.id.action_delete).setVisible(true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.title) {
            resources.getString(R.string.action_new_counter) -> utils.openMainActivity()
            resources.getString(R.string.action_help) -> utils.openHelpMode(
                "LibraryActivity",
                helpModeArray
            )

            resources.getString(R.string.action_delete) -> turnOnDeleteManyMode()
            resources.getString(R.string.action_library) -> utils.openLibrary("LibraryActivity")
            resources.getString(R.string.action_settings) -> utils.openSettings()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        utils.updateTheme(false)
        super.onCreate(savedInstanceState)

        setup()

        setContent {
            Preview()
        }
    }

    @Preview
    @Composable
    private fun Preview() {
        STTheme {
            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(backgroundColor = STTheme.colors.background, floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            val i = Intent(this@MainActivity, OldMainActivity::class.java)
                            this@MainActivity.startActivity(i)
                        },
                        backgroundColor = STTheme.colors.accentDark,
                        contentColor = STTheme.colors.cWhite
                    ) {
                        Icon(Icons.Filled.Add, "Add")
                    }
                }, topBar = {
                    NavBar(titleId = R.string.action_library)
                }) { padding ->
                    val cursorObj = vm.dbCursor.observeAsState()
                    LazyColumn(modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = padding.calculateBottomPadding())) {
                        val tempCursor = cursorObj.value?.value
                        tempCursor?.let { cursor: Cursor ->
                            try {
                                val shouldAccessCursor = cursor.moveToFirst()
                                Log.i("composeLibrary", "LazyColumn, annaData ${DatabaseUtils.dumpCursorToString(cursor)}")
                                if(shouldAccessCursor) {
                                    do{
                                        val id = cursor.getInt(DBFields.ID.index)
                                        val type = cursor.getString(DBFields.TYPE.index)
                                        val projectTitle = cursor.getString(DBFields.TITLE.index)
                                        val stitchCounterNumber = cursor.getInt(DBFields.STITCH_COUNTER_NUMBER.index)
                                        val stitchAdjustment = cursor.getInt(DBFields.STITCH_ADJUSTMENT.index)
                                        val rowCounterNumber = cursor.getInt(DBFields.ROW_COUNTER_NUMBER.index)
                                        val rowAdjustment = cursor.getInt(DBFields.ROW_ADJUSTMENT.index)
                                        val maxRows = cursor.getInt(DBFields.TOTAL_ROWS.index)
                                        val counter = Counter(
                                            id = id,
                                            type = type,
                                            name = projectTitle,
                                            rowCounterNumber = rowCounterNumber,
                                            stitchCounterNumber = stitchCounterNumber,
                                            stitchAdjustment = stitchAdjustment,
                                            rowAdjustment = rowAdjustment,
                                            totalRows = maxRows)
                                        val progress = (rowCounterNumber.toFloat() / maxRows.toFloat()) * 100
                                        item {
                                            STTheme {//todo stitchCounterV2 I don't think this is needed
                                                Card {
                                                    Column(modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(STTheme.spaces.l)) {
                                                        Row(modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                onListItemClicked(counter)
                                                            }) {
                                                            Column {
                                                                Text(
                                                                    text = projectTitle.capitalized(),
                                                                    style = STTheme.typography.subtitle3,
                                                                    color = STTheme.colors.textPrimary
                                                                )
                                                                if (type == ProjectTypes.DOUBLE.name) {
                                                                    Text(
                                                                        text = "Total Rows: $maxRows",
                                                                        style = STTheme.typography.body5,
                                                                        color = STTheme.colors.textSecondary
                                                                    )
                                                                    Text(
                                                                        text = "Progress: $progress%",
                                                                        style = STTheme.typography.body5,
                                                                        color = STTheme.colors.textSecondary
                                                                    )
                                                                }
                                                            }

                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } while(cursor.moveToNext());
                                } else {
                                    item {
                                        Card {
                                            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                                                .fillMaxSize()
                                                .padding(STTheme.spaces.xxL)) {
                                                val loraSubtitle = SpanStyle(
                                                    fontFamily = STTheme.typography.subtitle1.fontFamily,
                                                    fontSize = STTheme.typography.subtitle1.fontSize,
                                                    color = STTheme.colors.textPrimary,
                                                    fontWeight = STTheme.typography.subtitle1.fontWeight)
                                                val dancingSubtitle = SpanStyle(
                                                    fontFamily = STTheme.typography.h4.fontFamily,
                                                    color = STTheme.colors.textPrimary,
                                                    fontSize = STTheme.typography.h4.fontSize,
                                                    fontWeight = FontWeight.W900)
                                                StyledText(data = listOf(
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.app_desc_1),
                                                        style = loraSubtitle
                                                    ),
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.app_desc_2),
                                                        style = loraSubtitle
                                                    ),
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.app_name),
                                                        style = dancingSubtitle
                                                    ),
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.app_desc_3),
                                                        style = loraSubtitle
                                                    ),
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.app_name),
                                                        style = dancingSubtitle
                                                    ),
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.app_desc_4),
                                                        style = loraSubtitle
                                                    ),
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.action_library),
                                                        style = dancingSubtitle
                                                    ),
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.app_desc_5),
                                                        style = loraSubtitle
                                                    ),
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.app_name),
                                                        style = dancingSubtitle
                                                    ),
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.app_desc_6),
                                                        style = loraSubtitle
                                                    ),
                                                ))
                                            }
                                        }
                                    }
                                }
                            } finally {
                                cursor.close();
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setup() {
        vm = ViewModelProvider(this)[MainViewModel::class.java]

        /* This statement invokes the method onCreatedLoader() */
        LoaderManager.getInstance(this).initLoader(0, null, this)

        /*
        Takes the returned data (counters) and saves them to the database. Resets the adapter and
        restarts the loader so that the list has access to the most recent data. Works for both single
        counter and double counter. Used when a new counter is created and then the library is
        accessed through the drop down menu.
        */
        val extras = intent.extras
        if (extras != null) {
            val extractedData = extras.getParcelableArrayList<OldCounter>(Consts.extras.counters)
            saveCounter(extractedData)
        }
    }

    /**
     * When deleteManyMode is off, parses db data for appropriate item and sends the data to
     * appropriate activity (SingleCounterActivity or DoubleCounterActivity)/ starts the activity.
     * When deleteManyMode is on, checks and adds id to deleteManyArray or unchecks the check box
     * and removes id from deleteManyArray
     */
    private fun onListItemClicked(counter: Counter) {
        if (!deleteManyMode) {
            val extras = Bundle()
            when (counter.type) {
                ProjectTypes.DOUBLE.name -> {
                    extras.putInt("_id", counter.id)
                    extras.putString("name", counter.name)
                    extras.putInt("stitch_counter_number", counter.stitchCounterNumber)
                    extras.putInt("stitch_adjustment", counter.stitchAdjustment)
                    extras.putInt("row_counter_number", counter.rowCounterNumber)
                    extras.putInt("row_adjustment", counter.rowAdjustment)
                    extras.putInt("total_rows", counter.totalRows)

                    val intentDouble =
                        Intent(baseContext, DoubleCounterActivity::class.java)
                    intentDouble.putExtras(extras)
                    startActivityForResult(intentDouble, 1)
                }

                ProjectTypes.SINGLE.name -> {
                    extras.putInt("_id", counter.id)
                    extras.putString("name", counter.name)
                    extras.putInt("stitch_counter_number", counter.stitchCounterNumber)
                    extras.putInt("stitch_adjustment", counter.stitchAdjustment)

                    val intentSingle =
                        Intent(baseContext, SingleCounterActivity::class.java)
                    intentSingle.putExtras(extras)
                    startActivityForResult(intentSingle, 1)
                }

                else -> {}
            }
        } else {
            /* todo stitchCounter2 handle deleteManyMode
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
            }*/
        }
    }

    /*
    Called when the delete button is clicked. Sets the delete button invisible and calls
    deleteFromDb AsyncTask
    */
    private val deleteClickListener = View.OnClickListener { v ->
        /*todo stitchCounterV2 make it so you can delete projects after confirming you want to delete them
        try {
            tempCursor?.let { csr ->
                val id =
                    csr.getString(csr.getColumnIndexOrThrow(StitchCounterContract.CounterEntry._ID))
                val ids = ArrayList<String?>()
                ids.add(id)
                v.visibility = View.INVISIBLE
                val deleteFromDb = DeleteFromDb(this)
                deleteFromDb.execute(ids)
            }
        } catch (e: IllegalArgumentException) {
            //todo stitchCounterV2 show error saying we have failed to delete the app
        }*/
    }

    /*
    Called when the deleteMany button is pressed. Sends the deleteManyArray to the deleteFromDb
    AsyncTask if there's at least 1 id in the array and then turns off deleteManyMode.
    */
    fun deleteMany(view: View?) {
        if (deleteManyArray.size > 0) {
            val deleteFromDb = DeleteFromDb(this)
            deleteFromDb.execute(deleteManyArray)
        }
        turnOffDeleteManyMode(view)
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
        //todo stitchCounterV2 fix this
        //setListConstraints(R.id.delete_many, ConstraintSet.TOP)
       // deleteMany!!.visibility = View.VISIBLE
        //cancelMany!!.visibility = View.VISIBLE
    }

    /*
    + Sets the deleteManyMode to false, thereby changing how the itemClickListener handles clicks and
      allowing long clicks to happen so single delete buttons can appear
    + Calls setListConstraints to connect the bottom of list to the bottom of the constraint layout.
    + Sets the delete and cancel buttons to invisible
    */
    fun turnOffDeleteManyMode(view: View?) {
        deleteManyMode = false
        //todo stitchCounterV2 fix this
        //setListConstraints(R.id.layout, ConstraintSet.BOTTOM)
        //deleteMany!!.visibility = View.INVISIBLE
        //cancelMany!!.visibility = View.INVISIBLE
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
    fun updateCursor() {//todo stitchCounterV2 get this working
        LoaderManager.getInstance(this).restartLoader(0, null, this)
    }

    /*
    Saves counters to the db when the library is accessed, either through the back button from a
    counter or through the library menu item from a counter.
    */
    private fun saveCounter(extractedData: ArrayList<OldCounter>?) {
        val stitchCounter = extractedData!![0]
        var rowOldCounter: OldCounter? = null
        if (extractedData.size > 1) {
            rowOldCounter = extractedData[1]
        }
        val writeToDb = WriteToDb(this)
        if (stitchCounter != null && rowOldCounter != null) {
            writeToDb.execute(stitchCounter, rowOldCounter)
        } else if (stitchCounter != null) {
            writeToDb.execute(stitchCounter)
        }
        startActivity(Intent(this, MainActivity::class.java))
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
                    val extractedData = data.getParcelableArrayListExtra<OldCounter>("counters")
                    saveCounter(extractedData)
                }
            }
        }
    }

    /* A callback method invoked by the loader when initLoader() is called */
    override fun onCreateLoader(arg0: Int, arg1: Bundle?): Loader<Cursor> {
        val uri = CounterProjectContentProvider.CONTENT_URI
        val loader = CursorLoader(this, uri, PROJECTION, null, null, SORTBYASCENDING)
        Log.i("composeLibrary", "uri $uri, loader $loader")
        return loader
    }

    /* A callback method, invoked after the requested content provider has returned all the data */
    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        Log.i("composeLibrary", "onLoadFinished cursor $cursor, loader $loader")

        vm.setDBCursor(cursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        Log.i("composeLibrary", "onLoaderReset, loader $loader")

        vm.setDBCursor(null)
    }

    public override fun onResume() {
        /* Allows list to function properly when accessed through back button */
        LoaderManager.getInstance(this).restartLoader(0, null, this)
        super.onResume()
    }

    public override fun onStop() {
        /*todo stitchCounterV2
        if (tempCursor != null && !tempCursor!!.isClosed) {
            tempCursor!!.close()
        }*/
        super.onStop()
    }

    public override fun onDestroy() {
        /*todo stitchCounterV2
        if (tempCursor != null && !tempCursor!!.isClosed) {
            tempCursor!!.close()
        }*/
        super.onDestroy()
    }

}

