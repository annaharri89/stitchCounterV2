/*
   Copyright 2024 Anna Harrison
 */
package io.github.annaharri89.stitchcounter.main

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import io.github.annaharri89.stitchcounter.Consts
import io.github.annaharri89.stitchcounter.NavGraphs
import io.github.annaharri89.stitchcounter.dataObjects.OldCounter
import io.github.annaharri89.stitchcounter.db.CounterProjectContentProvider
import io.github.annaharri89.stitchcounter.db.DeleteFromDb
import io.github.annaharri89.stitchcounter.R
import io.github.annaharri89.stitchcounter.dataObjects.Counter
import io.github.annaharri89.stitchcounter.db.StitchCounterContract
import io.github.annaharri89.stitchcounter.utilities.Utils
import io.github.annaharri89.stitchcounter.db.WriteToDb
import io.github.annaharri89.stitchcounter.destinations.LibraryScreenDestination
import io.github.annaharri89.stitchcounter.destinations.PortScreenDestination
import io.github.annaharri89.stitchcounter.destinations.SettingsScreenDestination
import io.github.annaharri89.stitchcounter.doubleCounter.DoubleCounterActivity
import io.github.annaharri89.stitchcounter.enums.ProjectTypes
import io.github.annaharri89.stitchcounter.library.LibraryScreen
import io.github.annaharri89.stitchcounter.library.LibraryViewModel
import io.github.annaharri89.stitchcounter.navigation.BottomBar
import io.github.annaharri89.stitchcounter.port.PortScreen
import io.github.annaharri89.stitchcounter.settings.SettingsScreen
import io.github.annaharri89.stitchcounter.singleCounter.SingleCounterActivity
import io.github.annaharri89.stitchcounter.theme.STTheme


class MainActivity : FragmentActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    @JvmField
    var helpMode: Boolean = false
    private var helpModeArray: ArrayList<View> = arrayListOf()
    private val deleteManyArray = ArrayList<String?>()
    private var deleteManyMode = false
    private val utils = Utils(this)
    private lateinit var libraryViewModel: LibraryViewModel
    private var composeNavController: NavHostController? = null

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
    var mFrom: IntArray? = null//todo stitchCounterV2 remove
    var fromColumns: Array<String> = arrayOf(StitchCounterContract.CounterEntry.COLUMN_TITLE)//todo stitchCounterV2 remove

    override fun onCreateOptionsMenu(menu: Menu): Boolean {//todo stitchCounter remove
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        menu.findItem(R.id.action_delete).setVisible(true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {//todo stitchCounter remove
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
        utils.updateTheme(false)//todo stitchCounter remove
        super.onCreate(savedInstanceState)

        setup()

        setContent {
            Preview()
        }
    }

    @OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class)
    @Composable
    private fun Preview() {
        STTheme {
            composeNavController = rememberNavController()
            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    backgroundColor = STTheme.colors.background,
                    bottomBar = {
                        composeNavController?.let { navController ->
                            BottomBar(navController = navController)
                        }
                    }
                ) { padding ->
                    val cursorObj = libraryViewModel.dbCursor.observeAsState()
                    val cursor = cursorObj.value?.value
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = padding.calculateBottomPadding())) {
                    val navHostEngine = rememberAnimatedNavHostEngine()

                    composeNavController?.let { nav ->
                        DestinationsNavHost(
                            navController = nav,
                            navGraph = NavGraphs.stitchTracker,
                            engine = navHostEngine,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable(LibraryScreenDestination) {
                                LibraryScreen(cursor)
                            }
                            composable(PortScreenDestination) {
                                PortScreen()
                            }
                            composable(SettingsScreenDestination) {
                                SettingsScreen()
                            }
                        }
                    }}
                }
            }
        }
    }

    private fun setup() {
        libraryViewModel = ViewModelProvider(this)[LibraryViewModel::class.java]

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

        libraryViewModel.setDBCursor(cursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        Log.i("composeLibrary", "onLoaderReset, loader $loader")

        libraryViewModel.setDBCursor(null)
    }

    override fun onResume() {
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

