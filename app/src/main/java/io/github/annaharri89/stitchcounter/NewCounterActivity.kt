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

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.FragmentActivity
import io.github.annaharri89.stitchcounter.doubleCounter.DoubleCounterActivity
import io.github.annaharri89.stitchcounter.singleCounter.SingleCounterActivity

class NewCounterActivity : FragmentActivity() {
    private val utils = Utils(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        utils.updateTheme(true)
        super.onCreate(savedInstanceState)

        // Prevents outside clicks to dismiss the dialog
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        )

        /*
        Sets dialog content based on whether single or double counter
        was tapped in main activity.
        */
        val extras = intent.extras
        if (extras != null) {
            val value = extras.getString("Layout")
            if (value == "single") {
                setContentView(R.layout.activity_dialog_new_single_counter)
            } else if (value == "double") {
                setContentView(R.layout.activity_dialog_new_double_counter)
            }
        }

        /* Single Counter Dialog */
        val buttonSingleOk = findViewById<View>(R.id.button_single_ok) as Button
        val textProjectNameSingle = findViewById<View>(R.id.text_project_name_3) as EditText
        if (textProjectNameSingle != null) {
            /* Makes the keyboard appear automatically */
            openKeyboard()
        }
        buttonSingleOk?.setOnClickListener { v ->
            dismissKeyboard()
            val name = textProjectNameSingle.text.toString()
            val intent = Intent(v.context, SingleCounterActivity::class.java)
            intent.putExtra("name", name)
            startActivity(intent)
        }
        /* Double Counter Dialog */
        val buttonDoubleOk = findViewById<View>(R.id.button_double_ok) as Button
        val textProjectNameDouble = findViewById<View>(R.id.text_project_name_4) as EditText
        val totalRows = findViewById<View>(R.id.text_total_rows_input_2) as EditText
        if (textProjectNameDouble != null) {
            /* Makes the keyboard appear automatically */
            openKeyboard()
        }
        buttonDoubleOk?.setOnClickListener { v ->
            dismissKeyboard()
            val intent = Intent(v.context, DoubleCounterActivity::class.java)
            val name = textProjectNameDouble.text.toString()
            intent.putExtra("name", name)

            if (totalRows.text.toString().length > 0) {
                val total_rows = totalRows.text.toString().toInt()
                intent.putExtra("total_rows", total_rows)
            }
            startActivity(intent)
        }
    }

    /* Open Keyboard */
    fun openKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    /* Dismiss Keyboard */
    fun dismissKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        if (imm.isAcceptingText) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    /* Dismiss Dialog */
    fun dismissDialog(view: View?) {
        dismissKeyboard()
        finish()
    }
}

