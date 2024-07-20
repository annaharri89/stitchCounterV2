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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import java.util.ArrayList;

public class LibraryActivity extends FragmentActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Button deleteSingle;
    private Button deleteMany;
    private Button cancelMany;
    private Context context = this;
    protected CounterAdapter mAdapter;
    private ListView mListView;
    private Cursor tempCursor;
    private ArrayList<String> deleteManyArray = new ArrayList<>();
    private Boolean deleteManyMode = false;
    private ConstraintLayout layout;
    protected Boolean helpMode = false;
    private ArrayList<View> helpModeArray;
    private Utils utils = new Utils(this);

    /*
    Defines a projection that specifies which columns from the database will actually
    be used for the query.
    */
    String[] PROJECTION = {
            StitchCounterContract.CounterEntry._ID,
            StitchCounterContract.CounterEntry.COLUMN_TYPE,
            StitchCounterContract.CounterEntry.COLUMN_TITLE,
            StitchCounterContract.CounterEntry.COLUMN_STITCH_COUNTER_NUM,
            StitchCounterContract.CounterEntry.COLUMN_STITCH_ADJUSTMENT,
            StitchCounterContract.CounterEntry.COLUMN_ROW_COUNTER_NUM,
            StitchCounterContract.CounterEntry.COLUMN_ROW_ADJUSTMENT,
            StitchCounterContract.CounterEntry.COLUMN_TOTAL_ROWS
    };

    /* Sort the results in the Cursor in ascending order */
    String SORTORDER =
            StitchCounterContract.CounterEntry.COLUMN_TITLE + " ASC";

    /*
    Was for the cursor adapter, specifies which columns go into which views, but is actually
    unused because of the CounterAdapter method getView overrides them. Still needed to pass
    to mAdpter when it's initialized.
    */
    String[] fromColumns = {StitchCounterContract.CounterEntry.COLUMN_TITLE};
    int[] toViews = {R.id.text1};

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        menu.findItem(R.id.action_delete).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        switch(item.getItemId()) {
            case R.id.action_new_counter:
                utils.openMainActivity();
                break;
            case R.id.action_help:
                utils.openHelpMode("LibraryActivity", helpModeArray);
                break;
            case R.id.action_delete:
                turnOnDeleteManyMode();
                break;
            case R.id.action_library:
                utils.openLibrary("LibraryActivity");
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
        setContentView(R.layout.activity_library);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        //setSupportActionBar(myToolbar);

        /* Help Mode Setup*/
        layout = (ConstraintLayout) findViewById(R.id.layout);
        TextView help1 = (TextView) findViewById(R.id.help_library_activity_1);
        TextView help2 = (TextView) findViewById(R.id.help_library_activity_2);
        View tip1 = findViewById(R.id.help_library_activity_1_tip);
        helpModeArray = new ArrayList<>();
        helpModeArray.add(help1);
        helpModeArray.add(help2);
        helpModeArray.add(tip1);

        /* Setup Delete and Cancel buttons for Delte Many Mode*/
        deleteMany = (Button) findViewById(R.id.delete_many);
        cancelMany = (Button) findViewById(R.id.cancel_many);
        deleteMany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMany(v);
            }
        });
        cancelMany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffDeleteManyMode(v);
            }
        });

        /* Setup listview and adapter, setup empty view for listview*/
        TextView noSavedProjects = (TextView) findViewById(R.id.empty_list_view);
        mListView = (ListView) findViewById(R.id.list);
        mListView.setEmptyView(noSavedProjects);
        setUpAdapter();

        /* Creating a loader for populating listview from sqlite database */
        /* This statement invokes the method onCreatedLoader() */
        getSupportLoaderManager().initLoader(0, null, this);

        /*
        Takes the returned data (counters) and saves them to the database. Resets the adapter and
        restarts the loader so that the list has access to the most recent data. Works for both single
        counter and double counter. Used when a new counter is created and then the library is
        accessed through the drop down menu.
        */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ArrayList<Counter> extractedData = extras.getParcelableArrayList("counters");
            saveCounter(extractedData);
        }

        /* Closes Help Mode when listview is empty, hides the annotation bubbles */
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                closeHelpMode();
                return false;
            }
        });
        /* Closes Help Mode when listview has list items, hides the annotation bubbles */
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                closeHelpMode();
                return false;
            }
        });

        /*
        + When deleteManyMode is off, parses db data for appropriate item and sends the data to
          appropriate activity (SingleCounterActivity or DoubleCounterActivity)/ starts the activity.
        + When deleteManyMode is on, checks and adds id to deleteManyArray or unchecks the check box
          and removes id from deleteManyArray
        */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*todo stitchCounterV2
                tempCursor = (Cursor)parent.getItemAtPosition(position);
                if (!deleteManyMode) {

                    int _id = tempCursor.getInt(tempCursor.getColumnIndex(StitchCounterContract.CounterEntry._ID));
                    String type = tempCursor.getString(tempCursor.getColumnIndex(StitchCounterContract.CounterEntry.COLUMN_TYPE));
                    String name = tempCursor.getString(tempCursor.getColumnIndex(StitchCounterContract.CounterEntry.COLUMN_TITLE));
                    int stitch_counter_number = tempCursor.getInt(tempCursor.getColumnIndex(StitchCounterContract.CounterEntry.COLUMN_STITCH_COUNTER_NUM));
                    int stitch_adjustment = tempCursor.getInt(tempCursor.getColumnIndex(StitchCounterContract.CounterEntry.COLUMN_STITCH_ADJUSTMENT));
                    int row_counter_number = tempCursor.getInt(tempCursor.getColumnIndex(StitchCounterContract.CounterEntry.COLUMN_ROW_COUNTER_NUM));
                    int row_adjustment = tempCursor.getInt(tempCursor.getColumnIndex(StitchCounterContract.CounterEntry.COLUMN_ROW_ADJUSTMENT));
                    int total_rows = tempCursor.getInt(tempCursor.getColumnIndex(StitchCounterContract.CounterEntry.COLUMN_TOTAL_ROWS));

                    Bundle extras = new Bundle();
                    switch(type) {
                        case "Double":
                            extras.putInt("_id", _id);
                            extras.putString("name", name);
                            extras.putInt("stitch_counter_number", stitch_counter_number);
                            extras.putInt("stitch_adjustment", stitch_adjustment);
                            extras.putInt("row_counter_number", row_counter_number);
                            extras.putInt("row_adjustment", row_adjustment);
                            extras.putInt("total_rows", total_rows);

                            Intent intentDouble = new Intent(getBaseContext(), DoubleCounterActivity.class);
                            intentDouble.putExtras(extras);
                            startActivityForResult(intentDouble, 1);
                            break;
                        case "Single":
                            extras.putInt("_id", _id);
                            extras.putString("name", name);
                            extras.putInt("stitch_counter_number", stitch_counter_number);
                            extras.putInt("stitch_adjustment", stitch_adjustment);

                            Intent intentSingle = new Intent(getBaseContext(), SingleCounterActivity.class);
                            intentSingle.putExtras(extras);
                            startActivityForResult(intentSingle, 1);
                            break;
                    }
                } else {
                    CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
                    if (checkBox.isChecked()) {
                        checkBox.setChecked(false);
                        deleteManyArray.remove(tempCursor.getString(tempCursor.getColumnIndex(StitchCounterContract.CounterEntry._ID)));
                    } else {
                        checkBox.setChecked(true);
                        deleteManyArray.add(tempCursor.getString(tempCursor.getColumnIndex(StitchCounterContract.CounterEntry._ID)));
                    }
                }*/
            }
        });

        /*
        Called when a list item is long clicked. If not in deleteManyMode, sets the tempCursor,
        sets the delete button visible, sets the onclicklistener for the delete button,
        sets previously visible delete buttons invisible. Calls closeHelpMode
        */
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                closeHelpMode();
                if (deleteManyMode) {
                    return false;
                }
                tempCursor = (Cursor)parent.getItemAtPosition(position);
                if (deleteSingle != null) {
                    deleteSingle.setVisibility(View.INVISIBLE);
                }
                deleteSingle = (Button) view.findViewById(R.id.button_delete);
                deleteSingle.setOnClickListener(deleteClickListener);
                deleteSingle.setVisibility(View.VISIBLE);
                return true;
            }
        });
    }

    /*
    Called when the delete button is clicked. Sets the delete button invisible and calls
    deleteFromDb AsyncTask
    */
    private View.OnClickListener deleteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /*todo stitchCounterV2
            String id = tempCursor.getString(tempCursor.getColumnIndex(StitchCounterContract.CounterEntry._ID));
            ArrayList<String> ids = new ArrayList<>();
            ids.add(id);
            v.setVisibility(View.INVISIBLE);
            DeleteFromDb deleteFromDb = new DeleteFromDb(context);
            deleteFromDb.execute(ids); //TODO look into warning*/
        }
    };

    /*
    Called when the deleteMany button is pressed. Sends the deleteManyArray to the deleteFromDb
    AsyncTask if there's at least 1 id in the array and then turns off deleteManyMode.
    */
    protected void deleteMany(View view) {
        if (deleteManyArray.size() > 0) {
            DeleteFromDb deleteFromDb = new DeleteFromDb(context);
            deleteFromDb.execute(deleteManyArray);
        }
        turnOffDeleteManyMode(view);
    }

    /*
    Sets the bottom edge of the list view to the edge (top or bottom, passed through
    constrainingEdge) of the element (referenced by elementID).
    */
    protected void setListConstraints(int elementID, int constrainingEdge) {
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.connect(R.id.list, ConstraintSet.BOTTOM, elementID, constrainingEdge, 0);
        set.applyTo(layout);
    }

    /*
    + Sets deleteManyMode to true, thereby changing how the itemClickListener handles clicks and
      blocking long clicks so that single delete buttons cannot appear.
    + Calls setListConstraints to connect the bottom of list to the top of the delete_many button
    + Sets the delete and cancel buttons to visible
    + Calls closeHelpMode
    */
    public void turnOnDeleteManyMode() {
        closeHelpMode();
        deleteManyMode = true;
        setListConstraints(R.id.delete_many, ConstraintSet.TOP);
        deleteMany.setVisibility(View.VISIBLE);
        cancelMany.setVisibility(View.VISIBLE);
    }

    /*
    + Sets the deleteManyMode to false, thereby changing how the itemClickListener handles clicks and
      allowing long clicks to happen so single delete buttons can appear
    + Calls setListConstraints to connect the bottom of list to the bottom of the constraint layout.
    + Sets the delete and cancel buttons to invisible
    */
    protected void turnOffDeleteManyMode(View view) {
        deleteManyMode = false;
        setListConstraints(R.id.layout, ConstraintSet.BOTTOM);
        deleteMany.setVisibility(View.INVISIBLE);
        cancelMany.setVisibility(View.INVISIBLE);
    }

    /* Closes help mode, hides the annotation bubbles */
    protected void closeHelpMode() {
        if (helpMode) {
            for (View view: helpModeArray) {
                if (view != null) {
                    view.setVisibility(View.INVISIBLE);
                }
            }
            helpMode = false;
        }
    }

    /*
    Called from DeleteFromDb AsyncTask during onPostExecute (after project gets deleted from db).
    Updates the listview by removing appropriate project item(s)
    */
    protected void updateCursor() {
        getSupportLoaderManager().restartLoader(0, null, this);
        mAdapter.notifyDataSetChanged();
    }

    /*
    Create an empty adapter that will be used to display the loaded data.
    Pass null for the cursor, then update it in onLoadFinished()
    Sets the adapter to the ListView.
    */
    protected void setUpAdapter() {
        mAdapter = new CounterAdapter(getBaseContext(),
                R.layout.list_item_single_counter,
                null,
                fromColumns,
                toViews,
                0);
        mListView.setAdapter(mAdapter);
    }

    /*
    Saves counters to the db when the library is accessed, either through the back button from a
    counter or through the library menu item from a counter.
    */
    protected void saveCounter(ArrayList<Counter> extractedData) {
        Counter stitchCounter = extractedData.get(0);
        Counter rowCounter = null;
        if (extractedData.size() > 1) {
            rowCounter = extractedData.get(1);
        }
        WriteToDb writeToDb = new WriteToDb(this.context);
        if (stitchCounter != null && rowCounter != null) {
            writeToDb.execute(stitchCounter, rowCounter);
        } else if (stitchCounter != null) {
            writeToDb.execute(stitchCounter);
        }
        startActivity(new Intent (this, LibraryActivity.class));
    }

    /*
    Takes the returned data (counters) and saves them to the database. Resets the adapter and
    restarts the loader so that the list has access to the most recent data. Works for both single
    counter and double counter. Called when the back button is pressed from a counter that was
    loaded from the library.
    */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                if(data != null) {
                    ArrayList<Counter> extractedData = data.getParcelableArrayListExtra("counters");
                    saveCounter(extractedData);
                }

            }
        }
    }

    /* A callback method invoked by the loader when initLoader() is called */
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        Uri uri = CounterProjectContentProvider.CONTENT_URI;
        return new CursorLoader(this, uri, PROJECTION, null, null, SORTORDER);
    }

    /* A callback method, invoked after the requested content provider has returned all the data */
    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        mAdapter.swapCursor(arg1);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onResume() {
        /* Allows list to function properly when accessed through back button */
        getSupportLoaderManager().restartLoader(0, null, this);
        super.onResume();
    }

    @Override
    public void onStop() {
        if (tempCursor != null && !tempCursor.isClosed()) {
            tempCursor.close();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (tempCursor != null && !tempCursor.isClosed()) {
            tempCursor.close();
        }
        super.onDestroy();
    }

    public class CounterAdapter extends SimpleCursorAdapter {

        Context mContext;
        Cursor cursor;

        public CounterAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            this.mContext = context;
        }

        @Override
        public int getViewTypeCount() {
            return 4;
        }

        @Override
        public int getItemViewType(int position) {
            cursor = mAdapter.getCursor();
            cursor.moveToPosition(position);
            String type = cursor.getString(1);
            if (type.equals("Double") && !deleteManyMode) {
                return 0;
            } else if (type.equals("Single") && !deleteManyMode) {
                return 1;
            } else if (type.equals("Double") && deleteManyMode)  {
                return 2;
            } else if (type.equals("Single") && deleteManyMode) {
                return 3;
            }
            return 0;
        }

        /*
        Handles the four different list item layouts: double counter, double counter in delete many
        mode, single counter, and single counter in delete many mode.
        */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            int type = getItemViewType(position);
            cursor = mAdapter.getCursor();
            cursor.moveToPosition(position);
            if (row == null) {
                // Inflate the layout according to the view type
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                TextView textTitle;
                CheckBox checkTitle;
                ProgressBar progress;
                switch(type) {
                    //Sets up view for double counter item
                    case 0:
                        row = inflater.inflate(R.layout.list_item_double_counter, parent, false);

                        textTitle = (TextView) row.findViewById(R.id.text2);
                        progress = (ProgressBar) row.findViewById(R.id.progressBar_list_item);

                        textTitle.setText(cursor.getString(2));
                        progress.setMax(cursor.getInt(7));
                        progress.setProgress(cursor.getInt(5));
                        break;
                    //Sets up view for single counter item
                    case 1:
                        row = inflater.inflate(R.layout.list_item_single_counter, parent, false);

                        textTitle = (TextView) row.findViewById(R.id.text1);

                        textTitle.setText(cursor.getString(2));
                        break;
                    //Sets up view for double counter item in delete many mode
                    case 2:
                        row = inflater.inflate(R.layout.list_item_double_counter_check, parent, false);

                        checkTitle = (CheckBox) row.findViewById(R.id.checkBox);
                        progress = (ProgressBar) row.findViewById(R.id.progressBar_list_item);

                        checkTitle.setText(cursor.getString(2));
                        progress.setMax(cursor.getInt(7));
                        progress.setProgress(cursor.getInt(5));
                        break;
                    //Sets up view for single counter item in delete many mode
                    case 3:
                        row = inflater.inflate(R.layout.list_item_single_counter_check, parent, false);
                        checkTitle = (CheckBox) row.findViewById(R.id.checkBox);

                        checkTitle.setText(cursor.getString(2));
                        break;
                }
            }
            return row;
        }
    }
}

