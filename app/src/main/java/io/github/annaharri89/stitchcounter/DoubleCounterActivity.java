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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;

public class DoubleCounterActivity extends FragmentActivity {

    private Counter stitchCounter;
    private Counter rowCounter;
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
                utils.openHelpMode("DoubleCounterActivity", helpModeArray);
                break;
            case R.id.action_library:
                utils.openLibrary("DoubleCounterActivity");
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
        setContentView(R.layout.activity_double_counter);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_main);

        /* Help Mode Setup*/
        TextView help1 = (TextView) findViewById(R.id.help_double_counter_activity_1);
        TextView help2 = (TextView) findViewById(R.id.help_double_counter_activity_2);
        TextView help3 = (TextView) findViewById(R.id.help_double_counter_activity_3);
        TextView help4 = (TextView) findViewById(R.id.help_double_counter_activity_4);
        TextView help5 = (TextView) findViewById(R.id.help_double_counter_activity_5);
        TextView help6 = (TextView) findViewById(R.id.help_double_counter_activity_6);
        View tip1 = findViewById(R.id.help_double_counter_activity_1_tip);
        View tip2 = findViewById(R.id.help_double_counter_activity_2_tip);
        View tip3 = findViewById(R.id.help_double_counter_activity_3_tip);
        View tip4 = findViewById(R.id.help_double_counter_activity_4_tip);
        View tip5 = findViewById(R.id.help_double_counter_activity_5_tip);
        View tip6 = findViewById(R.id.help_double_counter_activity_6_tip);
        helpModeArray = new ArrayList<>();
        helpModeArray.add(help1);
        helpModeArray.add(help2);
        helpModeArray.add(help3);
        helpModeArray.add(help4);
        helpModeArray.add(help5);
        helpModeArray.add(help6);
        helpModeArray.add(tip1);
        helpModeArray.add(tip2);
        helpModeArray.add(tip3);
        helpModeArray.add(tip4);
        helpModeArray.add(tip5);
        helpModeArray.add(tip6);

        /* Closes Help Mode, hides the annotation bubbles */
        final ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.layout);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (helpMode) {
                    for (View view: helpModeArray) {
                        if (view != null) {
                            view.setVisibility(View.INVISIBLE);
                        }
                    }
                    helpMode = false;
                }
                return false;
            }
        });

        /* Progress Listener, updates the counter's progress bar */
        final EditText totalRows = (EditText) findViewById(R.id.text_total_rows_input);
        totalRows.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            /*

            */
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String totalRowsValueString;
                int totalRowsValueInt;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    totalRowsValueString = totalRows.getText().toString();
                    if (totalRowsValueString.length() > 0) {
                        totalRowsValueInt = Integer.parseInt(totalRowsValueString);
                    } else {
                        totalRowsValueInt = 0;
                    }
                    if (totalRowsValueInt > 0) {
                        rowCounter.setProgressBarMax(totalRowsValueInt);
                    }
                    //Sets totalRows' cursor invisible
                    totalRows.setCursorVisible(false);
                }
                return false;
            }
        });

        /* Project Name Listener, updates the project name in the counter */
        final EditText textProjectName = (EditText) findViewById(R.id.text_project_name_2);
        textProjectName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            /*
            When the done button is pressed, if a project name has been entered, setProjectName is called
            */
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String projectName;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    projectName = textProjectName.getText().toString();
                    if (projectName.length() > 0) {
                        rowCounter.setProjectName(projectName);
                    }
                    //Sets textProjectName's cursor invisible
                    textProjectName.setCursorVisible(false);
                    //Sets totalRows' cursor visible and places the cursor at the end of totalRows' text
                    totalRows.setCursorVisible(true);
                    totalRows.setSelection(totalRows.getText().length());
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

        /* Sets the totalRows' cursor visible */
        totalRows.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                totalRows.setCursorVisible(true);
                return false;
            }
        });

        /* Stitch Counter */
        final TextView textCounterStitch = (TextView) findViewById(R.id.text_counter_stitch);
        final Button buttonPlusStitch = (Button) findViewById(R.id.button_counter_plus_stitch);
        final Button buttonMinusStitch = (Button) findViewById(R.id.button_counter_minus_stitch);
        final Button buttonCapsuleTopStitch = (Button) findViewById(R.id.button_capsule_top_stitch);
        final Button buttonCapsuleMiddleStitch = (Button) findViewById(R.id.button_capsule_middle_stitch);
        final Button buttonCapsuleBottomStitch = (Button) findViewById(R.id.button_capsule_bottom_stitch);
        final Button buttonResetStitch = (Button) findViewById(R.id.button_counter_reset_stitch);

        stitchCounter = new Counter(this, textCounterStitch, null, R.string.counter_number_stitch, buttonCapsuleTopStitch, buttonCapsuleMiddleStitch, buttonCapsuleBottomStitch, null);

        buttonPlusStitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stitchCounter.incrementCounter();
            }
        });
        buttonMinusStitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stitchCounter.decrementCounter();
            }
        });
        buttonResetStitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stitchCounter.resetCounterCheck("stitch counter");
            }
        });
        buttonCapsuleTopStitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stitchCounter.changeAdjustment(1);
            }
        });
        buttonCapsuleMiddleStitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stitchCounter.changeAdjustment(5);
            }
        });
        buttonCapsuleBottomStitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stitchCounter.changeAdjustment(10);
            }
        });

        /* Row Counter */
        final TextView textCounterRow = (TextView) findViewById(R.id.text_counter_row);
        final TextView textProgress = (TextView) findViewById(R.id.text_counter_progress);
        final Button buttonPlusRow = (Button) findViewById(R.id.button_counter_plus_row);
        final Button buttonMinusRow = (Button) findViewById(R.id.button_counter_minus_row);
        final Button buttonResetRow = (Button) findViewById(R.id.button_counter_reset_row);
        final Button buttonCapsuleTopRow = (Button) findViewById(R.id.button_capsule_top_row);
        final Button buttonCapsuleMiddleRow = (Button) findViewById(R.id.button_capsule_middle_row);
        final Button buttonCapsuleBottomRow = (Button) findViewById(R.id.button_capsule_bottom_row);
        final ProgressBar progress = (ProgressBar) findViewById(R.id.progress_bar);

        rowCounter = new Counter(this, textCounterRow, textProgress, R.string.counter_number_row, buttonCapsuleTopRow, buttonCapsuleMiddleRow, buttonCapsuleBottomRow, progress);

        buttonPlusRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rowCounter.incrementCounter();
            }
        });
        buttonMinusRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rowCounter.decrementCounter();
            }
        });
        buttonResetRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rowCounter.resetCounterCheck("row counter");
            }
        });
        buttonCapsuleTopRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rowCounter.changeAdjustment(1);
            }
        });
        buttonCapsuleMiddleRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rowCounter.changeAdjustment(5);
            }
        });
        buttonCapsuleBottomRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rowCounter.changeAdjustment(10);
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
            parseData(savedInstanceState, textProjectName, totalRows, textProgress);
        } else if (extras != null) {
            parseData(extras, textProjectName, totalRows, textProgress);
        }

        /* Save Counter project to DB if counter project doesn't already exist in the db*/
        if (stitchCounter.ID == 0) {
            stitchCounter.saveCounter(stitchCounter, rowCounter);
        }
    }

    /*
    Gets all pertinent counter data from the passed bundle and updates the counters and the UI
    where needed.
    */
    protected void parseData(Bundle bundle, TextView projectName, TextView totalRows, TextView progress) {
        int _id = bundle.getInt("_id");
        String name = bundle.getString("name");
        int stitch_counter_number = bundle.getInt("stitch_counter_number");
        int stitch_adjustment = bundle.getInt("stitch_adjustment");
        int row_counter_number = bundle.getInt("row_counter_number");
        int row_adjustment = bundle.getInt("row_adjustment");
        int total_rows = bundle.getInt("total_rows");

        if (_id > 0) {
            stitchCounter.ID = _id;
        }
        if (name != null && name.length() > 0) {
            rowCounter.setProjectName(name);
            projectName.setText(name);
        }
        if (stitch_adjustment > 0) {
            stitchCounter.changeAdjustment(stitch_adjustment);
        } else {
            /* Sets default colors for adjustment buttons */
            stitchCounter.changeAdjustment(1);
        }
        if (row_adjustment > 0) {
            rowCounter.changeAdjustment(row_adjustment);
        } else {
            /* Sets default colors for adjustment buttons */
            rowCounter.changeAdjustment(1);
        }
        if (stitch_counter_number > 0) {
            stitchCounter.counterNumber = stitch_counter_number;
            stitchCounter.setCounter();
        }
        if (row_counter_number > 0) {
            rowCounter.counterNumber = row_counter_number;
            rowCounter.setCounter();
        }
        if (total_rows > 0) {
            rowCounter.setProgressBarMax(total_rows);
            rowCounter.totalRows = total_rows;
            totalRows.setText(Integer.toString(total_rows));
        } else {
            /* Sets default progress percent */
            String formattedProgressNumber = String.format(rowCounter.strResProgress, "0.0");
            progress.setText(formattedProgressNumber);
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
        counterList.add(stitchCounter);
        counterList.add(rowCounter);
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
        savedInstanceState.putInt("_id", stitchCounter.ID);
        savedInstanceState.putString("name", rowCounter.projectName);
        savedInstanceState.putInt("stitch_counter_number", stitchCounter.counterNumber);
        savedInstanceState.putInt("stitch_adjustment", stitchCounter.adjustment);
        savedInstanceState.putInt("row_counter_number", rowCounter.counterNumber);
        savedInstanceState.putInt("row_adjustment", rowCounter.adjustment);
        savedInstanceState.putInt("total_rows", rowCounter.totalRows);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStop() {
        /* Save Counters to DB */
        stitchCounter.saveCounter(stitchCounter, rowCounter);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        /* Save Counters to DB */
        stitchCounter.saveCounter(stitchCounter, rowCounter);
        super.onDestroy();
    }
}
