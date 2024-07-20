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

import android.content.Context
import android.content.Intent
import android.view.View
import io.github.annaharri89.stitchcounter.doubleCounter.DoubleCounterActivity
import io.github.annaharri89.stitchcounter.library.LibraryActivity
import io.github.annaharri89.stitchcounter.main.MainActivity
import io.github.annaharri89.stitchcounter.settings.SettingsActivity
import io.github.annaharri89.stitchcounter.singleCounter.SingleCounterActivity

/**
 * Created by ETASpare on 7/25/2017.
 */
class Utils /* Constructor */(private val mContext: Context) {
    /* Returns color to be used for Active Capsule Button in Counter*/
    fun determineActiveCapsuleButtonColor(): Int {
        val prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val theme = prefs.getString("theme", "")
        return when (theme) {
            "default", "default_dark" -> R.color.colorAccent
            "pink", "pink_dark" -> R.color.colorAccent2
            "blue", "blue_dark" -> R.color.colorAccent3
            else -> R.color.colorAccent
        }
    }

    /* Returns color to be used for Inactive Capsule Button in Counter */
    fun determineInActiveCapsuleButtonColor(): Int {
        val prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val theme = prefs.getString("theme", "")
        return when (theme) {
            "default", "pink", "blue" -> R.color.lightGrey
            "default_dark", "pink_dark", "blue_dark" -> R.color.darkGrey
            else -> R.color.lightGrey
        }
    }

    /* Returns text color to be used for Active Capsule Button */
    fun determineActiveCapsuleButtonTextColor(): Int {
        val prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val theme = prefs.getString("theme", "")
        return when (theme) {
            "default", "pink", "default_dark", "pink_dark" -> R.color.white
            "blue", "blue_dark" -> R.color.black
            else -> R.color.white
        }
    }

    /* Returns text color to be used for Inactive Capsule Button */
    fun determineInActiveCapsuleButtonTextColor(): Int {
        val prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val theme = prefs.getString("theme", "")
        return when (theme) {
            "default", "pink", "blue" -> R.color.darkGrey
            "default_dark", "pink_dark" -> R.color.white
            "blue_dark" -> R.color.silver
            else -> R.color.darkGrey
        }
    }

    /* Set's theme based on store shared preference */
    fun updateTheme(dialog: Boolean) {
        val prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val theme = prefs.getString("theme", "")
        when (theme) {
            "default" -> if (dialog) {
                mContext.setTheme(R.style.Theme_MyDialog)
            } else {
                mContext.setTheme(R.style.AppTheme)
            }

            "default_dark" -> if (dialog) {
                mContext.setTheme(R.style.Theme_MyDialog_dark)
            } else {
                mContext.setTheme(R.style.AppTheme_dark)
            }

            "pink" -> if (dialog) {
                mContext.setTheme(R.style.Theme_MyDialog_pink)
            } else {
                mContext.setTheme(R.style.AppTheme_pink)
            }

            "pink_dark" -> if (dialog) {
                mContext.setTheme(R.style.Theme_MyDialog_pink_dark)
            } else {
                mContext.setTheme(R.style.AppTheme_pink_dark)
            }

            "blue" -> if (dialog) {
                mContext.setTheme(R.style.Theme_MyDialog_blue)
            } else {
                mContext.setTheme(R.style.AppTheme_blue)
            }

            "blue_dark" -> if (dialog) {
                mContext.setTheme(R.style.Theme_MyDialog_blue_dark)
            } else {
                mContext.setTheme(R.style.AppTheme_blue_dark)
            }
        }
    }

    /* Updates shared preference with appropriate theme title so theme can be set */
    fun updateSharedPreferences(theme: Int) {
        val editor = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        when (theme) {
            0 -> {
                editor.putString("theme", "default")
                editor.apply()
            }

            1 -> {
                editor.putString("theme", "default_dark")
                editor.apply()
            }

            2 -> {
                editor.putString("theme", "pink")
                editor.apply()
            }

            3 -> {
                editor.putString("theme", "pink_dark")
                editor.apply()
            }

            4 -> {
                editor.putString("theme", "blue")
                editor.apply()
            }

            5 -> {
                editor.putString("theme", "blue_dark")
                editor.apply()
            }

            else -> {
                editor.putString("theme", "default")
                editor.apply()
            }
        }
    }

    /* Iterates through views stored in helpModeArray and sets their visibility to visible */
    private fun setViewVisible(helpModeArray: ArrayList<View>) {
        for (view in helpModeArray) {
            if (view != null) {
                view.visibility = View.VISIBLE
            }
        }
    }

    /* Called when the user taps the "+" button (new counter) in the toolbar */
    fun openMainActivity() {
        val intent = Intent(this.mContext, MainActivity::class.java)
        mContext.startActivity(intent)
    }

    /*
    Opens "help mode" Called when help button is clicked in the action bar.
    Shows the annotation bubbles
    */
    fun openHelpMode(activity: String?, helpModeArray: ArrayList<View>) {
        when (activity) {
            "MainActivity" -> {
                val mainContext = mContext as MainActivity
                if (!mainContext.helpMode) {
                    setViewVisible(helpModeArray)
                    mainContext.helpMode = true
                }
            }

            "DoubleCounterActivity" -> {
                val doubleCounterContext = mContext as DoubleCounterActivity
                if (!doubleCounterContext.helpMode) {
                    setViewVisible(helpModeArray)
                    doubleCounterContext.helpMode = true
                }
            }

            "SingleCounterActivity" -> {
                val singleCounterContext = mContext as SingleCounterActivity
                if (!singleCounterContext.helpMode) {
                    setViewVisible(helpModeArray)
                    singleCounterContext.helpMode = true
                }
            }

            "LibraryActivity" -> {
                val libraryContext = mContext as LibraryActivity
                if (!libraryContext.helpMode) {
                    setViewVisible(helpModeArray)
                    libraryContext.helpMode = true
                }
            }
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
    fun openLibrary(activity: String?) {
        when (activity) {
            "MainActivity", "LibraryActivity", "SettingsActivity" -> {
                val intent = Intent(this.mContext, LibraryActivity::class.java)
                mContext.startActivity(intent)
            }

            "DoubleCounterActivity" -> {
                val doubleCounterContext = mContext as DoubleCounterActivity
                doubleCounterContext.sendResults(false)
            }

            "SingleCounterActivity" -> {
                val singleCounterContext = mContext as SingleCounterActivity
                singleCounterContext.sendResults(false)
            }

            else -> {
                val intent = Intent(this.mContext, LibraryActivity::class.java)
                mContext.startActivity(intent)
            }
        }
    }

    /* Called when the user taps the "Settings" button in the overflow menu */
    fun openSettings() {
        val intent = Intent(this.mContext, SettingsActivity::class.java)
        mContext.startActivity(intent)
    }

    companion object {
        private const val PREFS_NAME = "PrefsFile"
    }
}
