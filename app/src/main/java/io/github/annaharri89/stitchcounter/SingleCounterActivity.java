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

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;

public class SingleCounterActivity extends FragmentActivity {

    private Counter counter;
    protected Boolean helpMode = false;
    private ArrayList<View> helpModeArray;
    private Utils utils = new Utils(this);

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        menu.findItem(R.id.action_delete).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*todo stitchCounterV2
        switch(item.getItemId()) {
            case R.id.action_new_counter:
                utils.openMainActivity();
                break;
            case R.id.action_help:
                utils.openHelpMode("SingleCounterActivity", helpModeArray);
                break;
            case R.id.action_library:
                utils.openLibrary("SingleCounterActivity");
                break;
            case R.id.action_settings:
                utils.openSettings();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }*/
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        utils.updateTheme(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_counter);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        //setSupportActionBar(myToolbar);

        /* Help Mode Setup*/
        TextView help1 = (TextView) findViewById(R.id.help_single_counter_activity_1);
        TextView help2 = (TextView) findViewById(R.id.help_single_counter_activity_2);
        TextView help3 = (TextView) findViewById(R.id.help_single_counter_activity_3);
        TextView help4 = (TextView) findViewById(R.id.help_single_counter_activity_4);
        View tip1 = findViewById(R.id.help_single_counter_activity_1_tip);
        View tip2 = findViewById(R.id.help_single_counter_activity_2_tip);
        View tip4 = findViewById(R.id.help_single_counter_activity_4_tip);
        helpModeArray = new ArrayList<>();
        helpModeArray.add(help1);
        helpModeArray.add(help2);
        helpModeArray.add(help3);
        helpModeArray.add(help4);
        helpModeArray.add(tip1);
        helpModeArray.add(tip2);
        helpModeArray.add(tip4);

        /* Closes Help Mode, hides the annotation bubbles */
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.layout);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (helpMode) {
                    for (View view: helpModeArray) {
                        view.setVisibility(View.INVISIBLE);
                    }
                    helpMode = false;
                }
                return false;
            }
        });

        /* Project Name Listener, updates the project name in the counter */
        final EditText textProjectName = (EditText) findViewById(R.id.text_project_name);
        textProjectName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            /*
            When the done button is pressed, if a project name has been entered, setProjectName is called
            */
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String projectName;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    projectName = textProjectName.getText().toString();
                    if (projectName.length() > 0) {
                        counter.setProjectName(projectName);
                    }
                    //Sets textProjectName's cursor invisible
                    textProjectName.setCursorVisible(false);
                }
                return false;
            }
        });

        /* Sets the projectName's cursor visible */
        textProjectName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                textProjectName.setCursorVisible(true);
                return false;
            }
        });

        /* Counter */
        final TextView textCounter = (TextView) findViewById(R.id.text_counter);
        final Button buttonPlus = (Button) findViewById(R.id.button_counter_plus);
        final Button buttonMinus = (Button) findViewById(R.id.button_counter_minus);
        final Button buttonReset = (Button) findViewById(R.id.button_counter_reset);
        final Button buttonCapsuleLeft = (Button) findViewById(R.id.button_capsule_left);
        final Button buttonCapsuleMiddle = (Button) findViewById(R.id.button_capsule_middle);
        final Button buttonCapsuleRight = (Button) findViewById(R.id.button_capsule_right);

        counter = new Counter(this, textCounter, R.string.counter_number_basic, buttonCapsuleLeft, buttonCapsuleMiddle, buttonCapsuleRight);

        buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter.incrementCounter();
            }
        });
        buttonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                counter.decrementCounter();
            }
        });
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                counter.resetCounterCheck("counter");
            }
        });
        buttonCapsuleLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter.changeAdjustment(1);
            }
        });
        buttonCapsuleMiddle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter.changeAdjustment(5);
            }
        });
        buttonCapsuleRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter.changeAdjustment(10);
            }
        });

        /*
        + If savedInstanceState bundle is not null, gets all pertinent counter data from
          savedInstanceState bundle and updates the counters and the UI where needed. Used to keep
          counter progress up to date when orientation change occurs.
        + If savedInstanceState is null (meaning a new counter has been created or a counter has been
          loaded from the library) and extras is not null, gets all pertinent counter data from
          extras bundle and updates the counters and the UI where needed.
        */
        Bundle extras = getIntent().getExtras();
        if (savedInstanceState != null) {
            parseData(savedInstanceState, textProjectName);
        } else if (extras != null) {
            parseData(extras, textProjectName);
        }

        /* Save Counter project to DB if counter project doesn't already exist in the db*/
        if (counter.ID == 0) {
            counter.saveCounter(counter, null);
        }
    }

    /*
    Gets all pertinent counter data from the passed bundle and updates the counters and the UI
    where needed.
    */
    protected void parseData(Bundle bundle, TextView projectName) {
        int _id = bundle.getInt("_id");
        String name = bundle.getString("name");
        int stitch_counter_number = bundle.getInt("stitch_counter_number");
        int stitch_adjustment = bundle.getInt("stitch_adjustment");

        if (_id > 0) {
            counter.ID = _id;
        }
        if (name != null && name.length() > 0) {
            counter.setProjectName(name);
            projectName.setText(name);
        }
        if (stitch_adjustment > 0) {
            counter.changeAdjustment(stitch_adjustment);
        } else {
            /* Sets default colors for adjustment buttons */
            counter.changeAdjustment(1);
        }
        if (stitch_counter_number > 0) {
            counter.counterNumber = stitch_counter_number;
            counter.setCounter();
        }
    }

    /*
    Creates a new intent which gets extras put in it in setUpExtras. Sends the intent and extras
    to the new activity.
    */
    protected void sendResults(Boolean backPressed) {
        Intent intent;
        if (backPressed) {
            intent = new Intent();
        } else {
            intent = new Intent(this, LibraryActivity.class);
        }
        setUpExtras(intent);
        if (backPressed) {
            setResult(RESULT_OK, intent);
            finish();
        } else {
            startActivity(intent);
        }
    }

    /* Adds stitchCounter and rowCounter as extras in a parcelable array to the passed intent. */
    protected void setUpExtras(Intent i) {
        ArrayList<Counter> counterList= new ArrayList<>();
        counterList.add(counter);
        i.putParcelableArrayListExtra("counters", counterList);
    }

    /* Starts a new activity/sends results/extras to new activity when back button is pressed. */
    @Override
    public void onBackPressed() {
        sendResults(true);
    }

    /*
    Saves all of the pertinent counter data to savedInstanceState bundle so it can be used to
    populate the activity if orientation change occurs.
    */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("_id", counter.ID);
        savedInstanceState.putString("name", counter.projectName);
        savedInstanceState.putInt("stitch_counter_number", counter.counterNumber);
        savedInstanceState.putInt("stitch_adjustment", counter.adjustment);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStop() {
        /* Save Counter to DB */
        counter.saveCounter(counter, null);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        /* Save Counter to DB */
        counter.saveCounter(counter, null);
        super.onDestroy();
    }
}
