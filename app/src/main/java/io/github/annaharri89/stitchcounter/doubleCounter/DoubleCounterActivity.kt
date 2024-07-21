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
package io.github.annaharri89.stitchcounter.doubleCounter

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import io.github.annaharri89.stitchcounter.dataObjects.OldCounter
import io.github.annaharri89.stitchcounter.R
import io.github.annaharri89.stitchcounter.utilities.Utils
import io.github.annaharri89.stitchcounter.library.LibraryActivity

class DoubleCounterActivity : FragmentActivity() {
    private var stitchOldCounter: OldCounter? = null
    private var rowOldCounter: OldCounter? = null
    var helpMode: Boolean = false
    private var helpModeArray: ArrayList<View> = arrayListOf()
    private val utils = Utils(this)

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        menu.findItem(R.id.action_delete).setVisible(false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.title) {
            resources.getString(R.string.action_new_counter) -> utils.openMainActivity()
            resources.getString(R.string.action_help) -> utils.openHelpMode("LibraryActivity", helpModeArray)
            resources.getString(R.string.action_library) -> utils.openLibrary("LibraryActivity")
            resources.getString(R.string.action_settings) -> utils.openSettings()
            else -> return super.onOptionsItemSelected(item);
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        utils.updateTheme(false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_double_counter)

        val myToolbar = findViewById<View>(R.id.toolbar_main) as Toolbar
        setActionBar(myToolbar)

        /* Help Mode Setup*/
        val help1 = findViewById<View>(R.id.help_double_counter_activity_1) as TextView
        val help2 = findViewById<View>(R.id.help_double_counter_activity_2) as TextView
        val help3 = findViewById<View>(R.id.help_double_counter_activity_3) as TextView
        val help4 = findViewById<View>(R.id.help_double_counter_activity_4) as TextView
        val help5 = findViewById<View>(R.id.help_double_counter_activity_5) as TextView
        val help6 = findViewById<View>(R.id.help_double_counter_activity_6) as TextView
        val tip1 = findViewById<View>(R.id.help_double_counter_activity_1_tip)
        val tip2 = findViewById<View>(R.id.help_double_counter_activity_2_tip)
        val tip3 = findViewById<View>(R.id.help_double_counter_activity_3_tip)
        val tip4 = findViewById<View>(R.id.help_double_counter_activity_4_tip)
        val tip5 = findViewById<View>(R.id.help_double_counter_activity_5_tip)
        val tip6 = findViewById<View>(R.id.help_double_counter_activity_6_tip)
        helpModeArray = ArrayList()
        helpModeArray!!.add(help1)
        helpModeArray!!.add(help2)
        helpModeArray!!.add(help3)
        helpModeArray!!.add(help4)
        helpModeArray!!.add(help5)
        helpModeArray!!.add(help6)
        helpModeArray!!.add(tip1)
        helpModeArray!!.add(tip2)
        helpModeArray!!.add(tip3)
        helpModeArray!!.add(tip4)
        helpModeArray!!.add(tip5)
        helpModeArray!!.add(tip6)

        /* Closes Help Mode, hides the annotation bubbles */
        val layout = findViewById<View>(R.id.layout) as ConstraintLayout
        layout.setOnTouchListener { v, event ->
            if (helpMode) {
                for (view in helpModeArray!!) {
                    if (view != null) {
                        view.visibility = View.INVISIBLE
                    }
                }
                helpMode = false
            }
            false
        }

        /* Progress Listener, updates the counter's progress bar */
        val totalRows = findViewById<View>(R.id.text_total_rows_input) as EditText
        totalRows.setOnEditorActionListener { v, actionId, event ->

            /*
    
                */
            val totalRowsValueString: String
            val totalRowsValueInt: Int
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                totalRowsValueString = totalRows.text.toString()
                totalRowsValueInt = if (totalRowsValueString.length > 0) {
                    totalRowsValueString.toInt()
                } else {
                    0
                }
                if (totalRowsValueInt > 0) {
                    rowOldCounter!!.setProgressBarMax(totalRowsValueInt)
                }
                //Sets totalRows' cursor invisible
                totalRows.isCursorVisible = false
            }
            false
        }

        /* Project Name Listener, updates the project name in the counter */
        val textProjectName = findViewById<View>(R.id.text_project_name_2) as EditText
        textProjectName.setOnEditorActionListener { v, actionId, event ->

            /*
                When the done button is pressed, if a project name has been entered, setProjectName is called
                */
            val projectName: String
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                projectName = textProjectName.text.toString()
                if (projectName.length > 0) {
                    rowOldCounter!!.setProjectName(projectName)
                }
                //Sets textProjectName's cursor invisible
                textProjectName.isCursorVisible = false
                //Sets totalRows' cursor visible and places the cursor at the end of totalRows' text
                totalRows.isCursorVisible = true
                totalRows.setSelection(totalRows.text.length)
            }
            false
        }

        /* Sets the projectName's cursor visible */
        textProjectName.setOnTouchListener { v, event ->
            textProjectName.isCursorVisible = true
            false
        }

        /* Sets the totalRows' cursor visible */
        totalRows.setOnTouchListener { v, event ->
            totalRows.isCursorVisible = true
            false
        }

        /* Stitch Counter */
        val textCounterStitch = findViewById<View>(R.id.text_counter_stitch) as TextView
        val buttonPlusStitch = findViewById<View>(R.id.button_counter_plus_stitch) as Button
        val buttonMinusStitch = findViewById<View>(R.id.button_counter_minus_stitch) as Button
        val buttonCapsuleTopStitch = findViewById<View>(R.id.button_capsule_top_stitch) as Button
        val buttonCapsuleMiddleStitch =
            findViewById<View>(R.id.button_capsule_middle_stitch) as Button
        val buttonCapsuleBottomStitch =
            findViewById<View>(R.id.button_capsule_bottom_stitch) as Button
        val buttonResetStitch = findViewById<View>(R.id.button_counter_reset_stitch) as Button

        stitchOldCounter = OldCounter(
            this,
            textCounterStitch,
            null,
            R.string.counter_number_stitch,
            buttonCapsuleTopStitch,
            buttonCapsuleMiddleStitch,
            buttonCapsuleBottomStitch,
            null
        )

        buttonPlusStitch.setOnClickListener { stitchOldCounter!!.incrementCounter() }
        buttonMinusStitch.setOnClickListener { stitchOldCounter!!.decrementCounter() }
        buttonResetStitch.setOnClickListener { stitchOldCounter!!.resetCounterCheck("stitch counter") }
        buttonCapsuleTopStitch.setOnClickListener { stitchOldCounter!!.changeAdjustment(1) }
        buttonCapsuleMiddleStitch.setOnClickListener { stitchOldCounter!!.changeAdjustment(5) }
        buttonCapsuleBottomStitch.setOnClickListener { stitchOldCounter!!.changeAdjustment(10) }

        /* Row Counter */
        val textCounterRow = findViewById<View>(R.id.text_counter_row) as TextView
        val textProgress = findViewById<View>(R.id.text_counter_progress) as TextView
        val buttonPlusRow = findViewById<View>(R.id.button_counter_plus_row) as Button
        val buttonMinusRow = findViewById<View>(R.id.button_counter_minus_row) as Button
        val buttonResetRow = findViewById<View>(R.id.button_counter_reset_row) as Button
        val buttonCapsuleTopRow = findViewById<View>(R.id.button_capsule_top_row) as Button
        val buttonCapsuleMiddleRow = findViewById<View>(R.id.button_capsule_middle_row) as Button
        val buttonCapsuleBottomRow = findViewById<View>(R.id.button_capsule_bottom_row) as Button
        val progress = findViewById<View>(R.id.progress_bar) as ProgressBar

        rowOldCounter = OldCounter(
            this,
            textCounterRow,
            textProgress,
            R.string.counter_number_row,
            buttonCapsuleTopRow,
            buttonCapsuleMiddleRow,
            buttonCapsuleBottomRow,
            progress
        )

        buttonPlusRow.setOnClickListener { rowOldCounter!!.incrementCounter() }
        buttonMinusRow.setOnClickListener { rowOldCounter!!.decrementCounter() }
        buttonResetRow.setOnClickListener { rowOldCounter!!.resetCounterCheck("row counter") }
        buttonCapsuleTopRow.setOnClickListener { rowOldCounter!!.changeAdjustment(1) }
        buttonCapsuleMiddleRow.setOnClickListener { rowOldCounter!!.changeAdjustment(5) }
        buttonCapsuleBottomRow.setOnClickListener { rowOldCounter!!.changeAdjustment(10) }

        /*
        + If savedInstanceState bundle is not null, gets all pertinent counter data from
          savedInstanceState bundle and updates the counters and the UI where needed. Used to keep
          counter progress up to date when orientation change occurs.
        + If savedInstanceState is null (meaning a new counter has been created or a counter has been
          loaded from the library) and extras is not null, gets all pertinent counter data from
          extras bundle and updates the counters and the UI where needed.
        */
        val extras = intent.extras
        if (savedInstanceState != null) {
            parseData(savedInstanceState, textProjectName, totalRows, textProgress)
        } else if (extras != null) {
            parseData(extras, textProjectName, totalRows, textProgress)
        }

        /* Save Counter project to DB if counter project doesn't already exist in the db*/
        if (stitchOldCounter!!.ID == 0) {
            stitchOldCounter!!.saveCounter(stitchOldCounter, rowOldCounter)
        }
    }

    /*
    Gets all pertinent counter data from the passed bundle and updates the counters and the UI
    where needed.
    */
    protected fun parseData(
        bundle: Bundle,
        projectName: TextView,
        totalRows: TextView,
        progress: TextView
    ) {
        val _id = bundle.getInt("_id")
        val name = bundle.getString("name")
        val stitch_counter_number = bundle.getInt("stitch_counter_number")
        val stitch_adjustment = bundle.getInt("stitch_adjustment")
        val row_counter_number = bundle.getInt("row_counter_number")
        val row_adjustment = bundle.getInt("row_adjustment")
        val total_rows = bundle.getInt("total_rows")

        if (_id > 0) {
            stitchOldCounter!!.ID = _id
        }
        if (name != null && name.length > 0) {
            rowOldCounter!!.setProjectName(name)
            projectName.text = name
        }
        if (stitch_adjustment > 0) {
            stitchOldCounter!!.changeAdjustment(stitch_adjustment)
        } else {
            /* Sets default colors for adjustment buttons */
            stitchOldCounter!!.changeAdjustment(1)
        }
        if (row_adjustment > 0) {
            rowOldCounter!!.changeAdjustment(row_adjustment)
        } else {
            /* Sets default colors for adjustment buttons */
            rowOldCounter!!.changeAdjustment(1)
        }
        if (stitch_counter_number > 0) {
            stitchOldCounter!!.counterNumber = stitch_counter_number
            stitchOldCounter!!.setCounter()
        }
        if (row_counter_number > 0) {
            rowOldCounter!!.counterNumber = row_counter_number
            rowOldCounter!!.setCounter()
        }
        if (total_rows > 0) {
            rowOldCounter!!.setProgressBarMax(total_rows)
            rowOldCounter!!.totalRows = total_rows
            totalRows.text = total_rows.toString()
        } else {
            /* Sets default progress percent */
            val formattedProgressNumber = String.format(rowOldCounter?.strResProgress ?: "", "0.0")
            progress.text = formattedProgressNumber
        }
    }

    /*
    Creates a new intent which gets extras put in it in setUpExtras. Sends the intent and extras
    to the new activity.
    */
    fun sendResults(backPressed: Boolean) {
        val intent = if (backPressed) {
            Intent()
        } else {
            Intent(this, LibraryActivity::class.java)
        }
        setUpExtras(intent)
        if (backPressed) {
            setResult(RESULT_OK, intent)
            finish()
        } else {
            startActivity(intent)
        }
    }

    /* Adds stitchCounter and rowCounter as extras in a parcelable array to the passed intent. */
    protected fun setUpExtras(i: Intent) {
        val oldCounterList = ArrayList<OldCounter?>()
        oldCounterList.add(stitchOldCounter)
        oldCounterList.add(rowOldCounter)
        i.putParcelableArrayListExtra("counters", oldCounterList)
    }

    /* Starts a new activity/sends results/extras to new activity when back button is pressed. */
    override fun onBackPressed() {
        sendResults(true)
    }

    /*
    Saves all of the pertinent counter data to savedInstanceState bundle so it can be used to
    populate the activity if orientation change occurs.
    */
    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt("_id", stitchOldCounter!!.ID)
        savedInstanceState.putString("name", rowOldCounter!!.projectName)
        savedInstanceState.putInt("stitch_counter_number", stitchOldCounter!!.counterNumber)
        savedInstanceState.putInt("stitch_adjustment", stitchOldCounter!!.adjustment)
        savedInstanceState.putInt("row_counter_number", rowOldCounter!!.counterNumber)
        savedInstanceState.putInt("row_adjustment", rowOldCounter!!.adjustment)
        savedInstanceState.putInt("total_rows", rowOldCounter!!.totalRows)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onStop() {
        /* Save Counters to DB */
        stitchOldCounter!!.saveCounter(stitchOldCounter, rowOldCounter)
        super.onStop()
    }

    override fun onDestroy() {
        /* Save Counters to DB */
        stitchOldCounter!!.saveCounter(stitchOldCounter, rowOldCounter)
        super.onDestroy()
    }
}
