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
import android.content.SharedPreferences;
import android.view.View;
import java.util.ArrayList;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ETASpare on 7/25/2017.
 */

public class Utils {

    private static final String PREFS_NAME = "PrefsFile";
    private Context mContext;

    /* Constructor */
    public Utils(Context context) {
        this.mContext = context;
    }

    /* Returns color to be used for Active Capsule Button in Counter*/
    protected int determineActiveCapsuleButtonColor() {
        SharedPreferences prefs = this.mContext.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String theme = prefs.getString("theme", "");
        switch (theme) {
            default:
            case "default":
            case "default_dark":
                return R.color.colorAccent;
            case "pink":
            case "pink_dark":
                return R.color.colorAccent2;
            case "blue":
            case "blue_dark":
                return R.color.colorAccent3;
        }
    }

    /* Returns color to be used for Inactive Capsule Button in Counter */
    protected int determineInActiveCapsuleButtonColor() {
        SharedPreferences prefs = this.mContext.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String theme = prefs.getString("theme", "");
        switch (theme) {
            default:
            case "default":
            case "pink":
            case "blue":
                return R.color.lightGrey;
            case "default_dark":
            case "pink_dark":
            case "blue_dark":
                return R.color.darkGrey;
        }
    }

    /* Returns text color to be used for Active Capsule Button */
    protected int determineActiveCapsuleButtonTextColor() {
        SharedPreferences prefs = this.mContext.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String theme = prefs.getString("theme", "");
        switch (theme) {
            default:
            case "default":
            case "pink":
            case "default_dark":
            case "pink_dark":
                return R.color.white;
            case "blue":
            case "blue_dark":
                return R.color.black;
        }
    }

    /* Returns text color to be used for Inactive Capsule Button */
    protected int determineInActiveCapsuleButtonTextColor() {
        SharedPreferences prefs = this.mContext.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String theme = prefs.getString("theme", "");
        switch (theme) {
            default:
            case "default":
            case "pink":
            case "blue":
                return R.color.darkGrey;
            case "default_dark":
            case "pink_dark":
                return R.color.white;
            case "blue_dark":
                return R.color.silver;
        }
    }

    /* Set's theme based on store shared preference */
    protected void updateTheme(Boolean dialog) {
        SharedPreferences prefs = this.mContext.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String theme = prefs.getString("theme", "");
        switch (theme) {
            case "default":
                if (dialog) {
                    this.mContext.setTheme(R.style.Theme_MyDialog);
                } else {
                    this.mContext.setTheme(R.style.AppTheme);
                }
                break;
            case "default_dark":
                if (dialog) {
                    this.mContext.setTheme(R.style.Theme_MyDialog_dark);
                } else {
                    this.mContext.setTheme(R.style.AppTheme_dark);
                }
                break;
            case "pink":
                if (dialog) {
                    this.mContext.setTheme(R.style.Theme_MyDialog_pink);
                } else {
                    this.mContext.setTheme(R.style.AppTheme_pink);
                }
                break;
            case "pink_dark":
                if (dialog) {
                    this.mContext.setTheme(R.style.Theme_MyDialog_pink_dark);
                } else {
                    this.mContext.setTheme(R.style.AppTheme_pink_dark);
                }
                break;
            case "blue":
                if (dialog) {
                    this.mContext.setTheme(R.style.Theme_MyDialog_blue);
                } else {
                    this.mContext.setTheme(R.style.AppTheme_blue);
                }
                break;
            case "blue_dark":
                if (dialog) {
                    this.mContext.setTheme(R.style.Theme_MyDialog_blue_dark);
                } else {
                    this.mContext.setTheme(R.style.AppTheme_blue_dark);
                }
        }
    }

    /* Updates shared preference with appropriate theme title so theme can be set */
    protected void updateSharedPreferences(int theme) {
        SharedPreferences.Editor editor = this.mContext.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        switch (theme) {
            default:
            case 0:
                editor.putString("theme", "default");
                editor.apply();
                break;
            case 1:
                editor.putString("theme", "default_dark");
                editor.apply();
                break;
            case 2:
                editor.putString("theme", "pink");
                editor.apply();
                break;
            case 3:
                editor.putString("theme", "pink_dark");
                editor.apply();
                break;
            case 4:
                editor.putString("theme", "blue");
                editor.apply();
                break;
            case 5:
                editor.putString("theme", "blue_dark");
                editor.apply();
                break;
        }
    }

    /* Iterates through views stored in helpModeArray and sets their visibility to visible */
    private void setViewVisible(ArrayList<View> helpModeArray) {
        for (View view: helpModeArray) {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    /* Called when the user taps the "+" button (new counter) in the toolbar */
    protected void openMainActivity () {
        Intent intent = new Intent(this.mContext, MainActivity.class);
        this.mContext.startActivity(intent);
    }

    /*
    Opens "help mode" Called when help button is clicked in the action bar.
    Shows the annotation bubbles
    */
    protected void openHelpMode(String activity, ArrayList<View> helpModeArray) {
        switch (activity) {
            case "MainActivity":
                MainActivity mainContext = (MainActivity) this.mContext;
                if (!mainContext.helpMode) {
                    setViewVisible(helpModeArray);
                    mainContext.helpMode = true;
                }
                break;
            case "DoubleCounterActivity":
                DoubleCounterActivity doubleCounterContext = (DoubleCounterActivity) this.mContext;
                if (!doubleCounterContext.helpMode) {
                    setViewVisible(helpModeArray);
                    doubleCounterContext.helpMode = true;
                }
                break;
            case "SingleCounterActivity":
                SingleCounterActivity singleCounterContext = (SingleCounterActivity) this.mContext;
                if (!singleCounterContext.helpMode) {
                    setViewVisible(helpModeArray);
                    singleCounterContext.helpMode = true;
                }
                break;
            case "LibraryActivity":
                LibraryActivity libraryContext = (LibraryActivity) this.mContext;
                if (!libraryContext.helpMode) {
                    setViewVisible(helpModeArray);
                    libraryContext.helpMode = true;
                }
                break;
        }

    }

    /*
    + Called when the user taps the "Library" button in the overflow menu.
    + If called from MainActivity, LibraryActivity, or SettingsActivity, starts a new library
      activity.
    + If called from doubleCounterActivity, starts a new library activity and sends the
      stitchCounter and rowCounter in an parcelable array to the LibraryActivity so they can be
      saved.
    + If called from singleCounterActivity, starts a new library activity and sends the counter an
      parcelable array to the LibraryActivity so it can be saved.
    */
    protected void openLibrary (String activity) {
        switch (activity) {
            default:
            case "MainActivity":
            case "LibraryActivity":
            case "SettingsActivity":
                Intent intent = new Intent(this.mContext, LibraryActivity.class);
                this.mContext.startActivity(intent);
                break;
            case "DoubleCounterActivity":
                DoubleCounterActivity doubleCounterContext = (DoubleCounterActivity) this.mContext;
                doubleCounterContext.sendResults(false);
                break;
            case "SingleCounterActivity":
                SingleCounterActivity singleCounterContext = (SingleCounterActivity) this.mContext;
                singleCounterContext.sendResults(false);
                break;
        }

    }

    /* Called when the user taps the "Settings" button in the overflow menu */
    protected void openSettings () {
        Intent intent = new Intent(this.mContext, SettingsActivity.class);
        this.mContext.startActivity(intent);
    }
}
