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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.FragmentActivity;

public class NewCounterActivity extends FragmentActivity {

    private Utils utils = new Utils(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        utils.updateTheme(true);
        super.onCreate(savedInstanceState);

        // Prevents outside clicks to dismiss the dialog
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

        /*
        Sets dialog content based on whether single or double counter
        was tapped in main activity.
        */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("Layout");
            if (value.equals("single")) {
                setContentView(R.layout.activity_dialog_new_single_counter);
            } else if (value.equals("double")) {
                setContentView(R.layout.activity_dialog_new_double_counter);
            }
        }

        /* Single Counter Dialog */
        final Button buttonSingleOk = (Button) findViewById(R.id.button_single_ok);
        final EditText textProjectNameSingle = (EditText) findViewById(R.id.text_project_name_3);
        if (textProjectNameSingle != null) {
            /* Makes the keyboard appear automatically */
            openKeyboard();
        }
        if (buttonSingleOk != null) {
            /* OnClick listener for ok button */
            buttonSingleOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissKeyboard();
                    String name = textProjectNameSingle.getText().toString();
                    Intent intent = new Intent(v.getContext(), SingleCounterActivity.class);
                    intent.putExtra("name", name);
                    startActivity(intent);
                }
            });
        }
        /* Double Counter Dialog */
        final Button buttonDoubleOk = (Button) findViewById(R.id.button_double_ok);
        final EditText textProjectNameDouble = (EditText) findViewById(R.id.text_project_name_4);
        final EditText totalRows = (EditText) findViewById(R.id.text_total_rows_input_2);
        if (textProjectNameDouble != null) {
            /* Makes the keyboard appear automatically */
            openKeyboard();
        }
        if (buttonDoubleOk != null) {
            /* OnClick listener for ok button */
            buttonDoubleOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissKeyboard();
                    Intent intent = new Intent(v.getContext(), DoubleCounterActivity.class);
                    String name = textProjectNameDouble.getText().toString();
                    intent.putExtra("name", name);

                    if (totalRows.getText().toString().length() > 0) {
                        int total_rows = Integer.parseInt(totalRows.getText().toString());
                        intent.putExtra("total_rows", total_rows);
                    }
                    startActivity(intent);
                }
            });
        }
    }

    /* Open Keyboard */
    public void openKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /* Dismiss Keyboard */
    public void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if(imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /* Dismiss Dialog */
    public void dismissDialog (View view) {
        dismissKeyboard();
        finish();
    }
}

