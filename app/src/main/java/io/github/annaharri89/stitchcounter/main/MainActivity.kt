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
package io.github.annaharri89.stitchcounter.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import io.github.annaharri89.stitchcounter.NewCounterActivity
import io.github.annaharri89.stitchcounter.R
import io.github.annaharri89.stitchcounter.Utils

class MainActivity : FragmentActivity() {
    @JvmField
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
        when (item.title.toString()) {
            this.getString(R.string.action_new_counter) -> utils.openMainActivity()
            this.getString(R.string.action_help) -> utils.openHelpMode(
                "MainActivity",
                helpModeArray
            )

            this.getString(R.string.action_library) -> utils.openLibrary("MainActivity")
            this.getString(R.string.action_settings) -> utils.openSettings()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        utils.updateTheme(false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myToolbar = findViewById(R.id.toolbar_main) as Toolbar
        setActionBar(myToolbar)

        /* Help Mode Setup*/
        val help1 = findViewById(R.id.help_main_activity_1) as TextView
        val help2 = findViewById(R.id.help_main_activity_2) as TextView
        val tip1 = findViewById(R.id.help_main_activity_1_tip) as View
        val tip2 = findViewById(R.id.help_main_activity_2_tip) as View
        helpModeArray = ArrayList()
        helpModeArray.add(help1)
        helpModeArray.add(help2)
        helpModeArray.add(tip1)
        helpModeArray.add(tip2)

        /* Closes Help Mode, hides the annotation bubbles */
        val layout = findViewById(R.id.layout) as ConstraintLayout
        layout.setOnTouchListener { v, event ->
            if (helpMode) {
                for (view in helpModeArray!!) {
                    view.visibility = View.INVISIBLE
                }
                helpMode = false
            }
            false
        }
    }

    /* Called when the user taps the "New Basic Counter" button in the main activity */
    fun createNewSingleCounter(view: View?) {
        val intent = Intent(this, NewCounterActivity::class.java)
        intent.putExtra("Layout", "single")
        startActivity(intent)
    }

    /* Called when the user taps the "New Advanced Counter" button in the main activity */
    fun createNewDoubleCounter(view: View?) {
        val intent = Intent(this, NewCounterActivity::class.java)
        intent.putExtra("Layout", "double")
        startActivity(intent)
    }
}
