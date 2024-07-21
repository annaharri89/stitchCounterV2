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
package io.github.annaharri89.stitchcounter.singleCounter

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import io.github.annaharri89.stitchcounter.dataObjects.OldCounter
import io.github.annaharri89.stitchcounter.R
import io.github.annaharri89.stitchcounter.utilities.Utils
import io.github.annaharri89.stitchcounter.library.LibraryActivity

class SingleCounterActivity : FragmentActivity() {
    private var oldCounter: OldCounter? = null
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
        setContentView(R.layout.activity_single_counter)

        val myToolbar = findViewById<View>(R.id.toolbar_main) as Toolbar
        setActionBar(myToolbar)

        /* Help Mode Setup*/
        val help1 = findViewById<View>(R.id.help_single_counter_activity_1) as TextView
        val help2 = findViewById<View>(R.id.help_single_counter_activity_2) as TextView
        val help3 = findViewById<View>(R.id.help_single_counter_activity_3) as TextView
        val help4 = findViewById<View>(R.id.help_single_counter_activity_4) as TextView
        val tip1 = findViewById<View>(R.id.help_single_counter_activity_1_tip)
        val tip2 = findViewById<View>(R.id.help_single_counter_activity_2_tip)
        val tip4 = findViewById<View>(R.id.help_single_counter_activity_4_tip)
        helpModeArray = ArrayList()
        helpModeArray!!.add(help1)
        helpModeArray!!.add(help2)
        helpModeArray!!.add(help3)
        helpModeArray!!.add(help4)
        helpModeArray!!.add(tip1)
        helpModeArray!!.add(tip2)
        helpModeArray!!.add(tip4)

        /* Closes Help Mode, hides the annotation bubbles */
        val layout = findViewById<View>(R.id.layout) as ConstraintLayout
        layout.setOnTouchListener { v, event ->
            if (helpMode) {
                for (view in helpModeArray!!) {
                    view.visibility = View.INVISIBLE
                }
                helpMode = false
            }
            false
        }

        /* Project Name Listener, updates the project name in the counter */
        val textProjectName = findViewById<View>(R.id.text_project_name) as EditText
        textProjectName.setOnEditorActionListener { v, actionId, event ->

            /*
                When the done button is pressed, if a project name has been entered, setProjectName is called
                */
            val projectName: String
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                projectName = textProjectName.text.toString()
                if (projectName.length > 0) {
                    oldCounter!!.setProjectName(projectName)
                }
                //Sets textProjectName's cursor invisible
                textProjectName.isCursorVisible = false
            }
            false
        }

        /* Sets the projectName's cursor visible */
        textProjectName.setOnTouchListener { v, event ->
            textProjectName.isCursorVisible = true
            false
        }

        /* Counter */
        val textCounter = findViewById<View>(R.id.text_counter) as TextView
        val buttonPlus = findViewById<View>(R.id.button_counter_plus) as Button
        val buttonMinus = findViewById<View>(R.id.button_counter_minus) as Button
        val buttonReset = findViewById<View>(R.id.button_counter_reset) as Button
        val buttonCapsuleLeft = findViewById<View>(R.id.button_capsule_left) as Button
        val buttonCapsuleMiddle = findViewById<View>(R.id.button_capsule_middle) as Button
        val buttonCapsuleRight = findViewById<View>(R.id.button_capsule_right) as Button

        oldCounter = OldCounter(
            this,
            textCounter,
            R.string.counter_number_basic,
            buttonCapsuleLeft,
            buttonCapsuleMiddle,
            buttonCapsuleRight
        )

        buttonPlus.setOnClickListener { oldCounter!!.incrementCounter() }
        buttonMinus.setOnClickListener { oldCounter!!.decrementCounter() }
        buttonReset.setOnClickListener { oldCounter!!.resetCounterCheck("counter") }
        buttonCapsuleLeft.setOnClickListener { oldCounter!!.changeAdjustment(1) }
        buttonCapsuleMiddle.setOnClickListener { oldCounter!!.changeAdjustment(5) }
        buttonCapsuleRight.setOnClickListener { oldCounter!!.changeAdjustment(10) }

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
            parseData(savedInstanceState, textProjectName)
        } else if (extras != null) {
            parseData(extras, textProjectName)
        }

        /* Save Counter project to DB if counter project doesn't already exist in the db*/
        if (oldCounter!!.ID == 0) {
            oldCounter!!.saveCounter(oldCounter, null)
        }
    }

    /*
    Gets all pertinent counter data from the passed bundle and updates the counters and the UI
    where needed.
    */
    protected fun parseData(bundle: Bundle, projectName: TextView) {
        val _id = bundle.getInt("_id")
        val name = bundle.getString("name")
        val stitch_counter_number = bundle.getInt("stitch_counter_number")
        val stitch_adjustment = bundle.getInt("stitch_adjustment")

        if (_id > 0) {
            oldCounter!!.ID = _id
        }
        if (name != null && name.length > 0) {
            oldCounter!!.setProjectName(name)
            projectName.text = name
        }
        if (stitch_adjustment > 0) {
            oldCounter!!.changeAdjustment(stitch_adjustment)
        } else {
            /* Sets default colors for adjustment buttons */
            oldCounter!!.changeAdjustment(1)
        }
        if (stitch_counter_number > 0) {
            oldCounter!!.counterNumber = stitch_counter_number
            oldCounter!!.setCounter()
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
        oldCounterList.add(oldCounter)
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
        savedInstanceState.putInt("_id", oldCounter!!.ID)
        savedInstanceState.putString("name", oldCounter!!.projectName)
        savedInstanceState.putInt("stitch_counter_number", oldCounter!!.counterNumber)
        savedInstanceState.putInt("stitch_adjustment", oldCounter!!.adjustment)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onStop() {
        /* Save Counter to DB */
        oldCounter!!.saveCounter(oldCounter, null)
        super.onStop()
    }

    override fun onDestroy() {
        /* Save Counter to DB */
        oldCounter!!.saveCounter(oldCounter, null)
        super.onDestroy()
    }
}
