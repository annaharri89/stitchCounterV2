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
package io.github.annaharri89.stitchcounter.settings

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.SimpleExpandableListAdapter
import android.widget.TextView
import android.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import io.github.annaharri89.stitchcounter.R
import io.github.annaharri89.stitchcounter.utilities.Utils

class SettingsActivity : FragmentActivity() {
    private var mListView: ExpandableListView? = null
    private var mAdapter: ExpandableListAdapter? = null
    private val utils = Utils(this)
    var numberOfGroups: Int = 0
    var groupExpandedArray: BooleanArray? = booleanArrayOf()
    var firstVisiblePosition: Int = 0

    private val group = arrayOf("Themes", "About")
    private val child = arrayOf(
        arrayOf(
            "_default",
            "_defaultDark",
            "cottonCandy",
            "cottonCandyDark",
            "robinsEggBlue",
            "robinsEggBlueDark"
        ), arrayOf("Version")
    )

    var _default: Theme = Theme(
        "Default",
        Color.parseColor("#3F51B5"),
        Color.parseColor("#303F9F"),
        Color.parseColor("#FF4081")
    )
    var _defaultDark: Theme = Theme(
        "Default Dark",
        Color.parseColor("#3F51B5"),
        Color.parseColor("#303F9F"),
        Color.parseColor("#FF4081")
    )
    var cottonCandy: Theme = Theme(
        "Cotton Candy",
        Color.parseColor("#F48FB1"),
        Color.parseColor("#F06292"),
        Color.parseColor("#CE93D8")
    )
    var cottonCandyDark: Theme = Theme(
        "Cotton Candy Dark",
        Color.parseColor("#F48FB1"),
        Color.parseColor("#F06292"),
        Color.parseColor("#CE93D8")
    )
    var robinsEggBlue: Theme = Theme(
        "Robins Egg Blue",
        Color.parseColor("#18FFFF"),
        Color.parseColor("#00E5FF"),
        Color.parseColor("#FFD740")
    )
    var robinsEggBlueDark: Theme = Theme(
        "Robins Egg Blue Dark",
        Color.parseColor("#18FFFF"),
        Color.parseColor("#00E5FF"),
        Color.parseColor("#FFD740")
    )

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        menu.findItem(R.id.action_delete).setVisible(false)
        menu.findItem(R.id.action_help).setVisible(false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.title) {
            resources.getString(R.string.action_new_counter) -> utils.openMainActivity()
            resources.getString(R.string.action_library) -> utils.openLibrary("LibraryActivity")
            resources.getString(R.string.action_settings) -> utils.openSettings()
            else -> return super.onOptionsItemSelected(item);
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        utils.updateTheme(false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val myToolbar = findViewById<View>(R.id.toolbar_main) as Toolbar
        setActionBar(myToolbar)

        /* Prepares list groupData and childData for the adapter */
        val groupData: MutableList<Map<String, String>?> = ArrayList()
        val childData: MutableList<List<Map<String, String>?>> = ArrayList()
        for (i in group.indices) {
            val curGroupMap: MutableMap<String, String> = HashMap()
            groupData.add(curGroupMap)
            curGroupMap[NAME] = group[i]

            val children: MutableList<Map<String, String>?> = ArrayList()
            for (j in child[i].indices) {
                val curChildMap: MutableMap<String, String> = HashMap()
                children.add(curChildMap)
                curChildMap[NAME] = child[i][j]
            }
            childData.add(children)
        }

        // Set up the adapter
        mAdapter = MySimpleExpandableListAdapter(
            this,
            groupData,
            android.R.layout.simple_expandable_list_item_1,
            arrayOf(NAME),
            intArrayOf(android.R.id.text1),
            childData, R.layout.list_item_theme,
            arrayOf(NAME),
            intArrayOf(R.id.theme_title)
        )
        mListView = findViewById<View>(R.id.list) as ExpandableListView
        mListView!!.setAdapter(mAdapter)

        /*
        The following code expands whatever list groups that were expanded before theme change.
        (Extras will be provided if theme change occurs.)
        */
        val extras = intent.extras
        if (extras != null) {
            groupExpandedArray = extras.getBooleanArray("groupExpandedArray")
            firstVisiblePosition = extras.getInt("firstVisiblePosition")
            for (i in groupExpandedArray!!.indices) {
                if (groupExpandedArray!![i]) mListView!!.expandGroup(i)
            }
            mListView!!.setSelection(firstVisiblePosition)
        }

        /*
        + Records expanded items. (so they can be re expanded when the settings activity gets
          restarted.
        + If a child is tapped in groupPosition 0 (themes), update the theme stored in shared
          preferences, finish this activity, start a new activity.
        */
        mListView!!.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            recordExpandedItems()
            if (groupPosition == 0) {
                val intent = Intent(baseContext, SettingsActivity::class.java)
                intent.putExtra("groupExpandedArray", groupExpandedArray)
                intent.putExtra("firstVisiblePosition", firstVisiblePosition)
                when (childPosition) {
                    0 -> {
                        utils.updateSharedPreferences(0)
                        finish()
                        startActivity(intent)
                    }

                    1 -> {
                        utils.updateSharedPreferences(1)
                        finish()
                        startActivity(intent)
                    }

                    2 -> {
                        utils.updateSharedPreferences(2)
                        finish()
                        startActivity(intent)
                    }

                    3 -> {
                        utils.updateSharedPreferences(3)
                        finish()
                        startActivity(intent)
                    }

                    4 -> {
                        utils.updateSharedPreferences(4)
                        finish()
                        startActivity(intent)
                    }

                    5 -> {
                        utils.updateSharedPreferences(5)
                        finish()
                        startActivity(intent)
                    }

                    else -> {
                        utils.updateSharedPreferences(0)
                        finish()
                        startActivity(intent)
                    }
                }
            } else if (groupPosition == 1) {
                //Do nothing
            }
            false
        }
    }

    /* Records which group in mListView is expanded*/
    fun recordExpandedItems() {
        numberOfGroups = mAdapter!!.groupCount
        groupExpandedArray = BooleanArray(numberOfGroups)
        for (i in 0 until numberOfGroups) {
            groupExpandedArray!![i] = mListView!!.isGroupExpanded(i)
        }
        firstVisiblePosition = mListView!!.firstVisiblePosition
    }

    /* A theme is made up of a title and three colors. */
    inner class Theme /* Theme Constructor */(
        val title: String,
        val color1: Int,
        val color2: Int,
        val color3: Int
    )

    internal inner class MySimpleExpandableListAdapter(
        private val context: Context,
        groupData: List<Map<String, String>?>,
        groupLayout: Int,
        groupFrom: Array<String>?,
        groupTo: IntArray?,
        private val data: MutableList<List<Map<String, String>?>>,
        childLayout: Int,
        childFrom: Array<String>?,
        childTo: IntArray?
    ) : SimpleExpandableListAdapter(
        context, groupData, groupLayout, groupFrom, groupTo, data, childLayout,
        childFrom, childTo
    ) {
        override fun getChildTypeCount(): Int {
            return 2
        }

        override fun getChildType(groupPosition: Int, childPosition: Int): Int {
            if (groupPosition == 0) {
                return 0
            } else if (groupPosition == 1) {
                return 1
            }
            return 0
        }

        override fun getChildView(
            groupPosition: Int, childPosition: Int, isLastChild: Boolean,
            convertView: View, parent: ViewGroup
        ): View {
            var row =
                super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent)
            val res = resources
            val type = getChildType(groupPosition, childPosition)
            if (row != null) {
                val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                when (type) {
                    0 -> {
                        row = inflater.inflate(R.layout.list_item_theme, parent, false)

                        val textView1 = row.findViewById<View>(R.id.theme_title) as TextView
                        val color1View = row.findViewById<View>(R.id.color_1_view)
                        val color2View = row.findViewById<View>(R.id.color_2_view)
                        val color3View = row.findViewById<View>(R.id.color_3_view)

                        val title = data[0][childPosition]!!["NAME"].toString()
                        val theme = when (title) {
                            "_default" -> _default
                            "_defaultDark" -> _defaultDark
                            "cottonCandy" -> cottonCandy
                            "cottonCandyDark" -> cottonCandyDark
                            "robinsEggBlue" -> robinsEggBlue
                            "robinsEggBlueDark" -> robinsEggBlueDark
                            else -> _default
                        }
                        textView1.text = theme.title
                        color1View.setBackgroundColor(theme.color1)
                        color2View.setBackgroundColor(theme.color2)
                        color3View.setBackgroundColor(theme.color3)

                        /*
                        Sets odd rows background color to darkgrey and textcolor to white, sets even rows
                        background color to white and textcolor to black
                        */
                        if (childPosition % 2 == 0) {
                            if (Build.VERSION.SDK_INT < 23) {
                                /* Android Support Library 22 and earlier compatible */
                                textView1.setTextColor(res.getColor(R.color.black))
                            } else {
                                /* Android Support Library 23 compatible */
                                textView1.setTextColor(
                                    ContextCompat.getColor(
                                        this.context,
                                        R.color.black
                                    )
                                )
                            }
                            row.setBackgroundColor(Color.parseColor("#FFFAFAFA"))
                        } else {
                            if (Build.VERSION.SDK_INT < 23) {
                                /* Android Support Library 22 and earlier compatible */
                                textView1.setTextColor(res.getColor(R.color.white))
                            } else {
                                /* Android Support Library 23 compatible */
                                textView1.setTextColor(
                                    ContextCompat.getColor(
                                        this.context,
                                        R.color.white
                                    )
                                )
                            }
                            row.setBackgroundColor(Color.parseColor("#303030"))
                        }
                    }

                    1 -> {
                        row = inflater.inflate(R.layout.list_item_about, parent, false)
                        //String versionName = BuildConfig.VERSION_NAME;//todo stitchCounterV2
                        val textVersion = row.findViewById<View>(R.id.version) as TextView
                        val textVersionName = row.findViewById<View>(R.id.version_name) as TextView
                        textVersion.setText(R.string.version)
                        textVersionName.text = "2.0.0" //todo stitchCounterV2
                    }

                    else -> {
                        row = inflater.inflate(R.layout.list_item_theme, parent, false)

                        val textView1 = row.findViewById<View>(R.id.theme_title) as TextView
                        val color1View = row.findViewById<View>(R.id.color_1_view)
                        val color2View = row.findViewById<View>(R.id.color_2_view)
                        val color3View = row.findViewById<View>(R.id.color_3_view)

                        val title = data[0][childPosition]!!["NAME"].toString()
                        val theme = when (title) {
                            "_default" -> _default
                            "_defaultDark" -> _defaultDark
                            "cottonCandy" -> cottonCandy
                            "cottonCandyDark" -> cottonCandyDark
                            "robinsEggBlue" -> robinsEggBlue
                            "robinsEggBlueDark" -> robinsEggBlueDark
                            else -> _default
                        }
                        textView1.text = theme.title
                        color1View.setBackgroundColor(theme.color1)
                        color2View.setBackgroundColor(theme.color2)
                        color3View.setBackgroundColor(theme.color3)

                        if (childPosition % 2 == 0) {
                            if (Build.VERSION.SDK_INT < 23) {
                                textView1.setTextColor(res.getColor(R.color.black))
                            } else {
                                textView1.setTextColor(
                                    ContextCompat.getColor(
                                        this.context,
                                        R.color.black
                                    )
                                )
                            }
                            row.setBackgroundColor(Color.parseColor("#FFFAFAFA"))
                        } else {
                            if (Build.VERSION.SDK_INT < 23) {
                                textView1.setTextColor(res.getColor(R.color.white))
                            } else {
                                textView1.setTextColor(
                                    ContextCompat.getColor(
                                        this.context,
                                        R.color.white
                                    )
                                )
                            }
                            row.setBackgroundColor(Color.parseColor("#303030"))
                        }
                    }
                }
            }
            return row
        }
    }

    companion object {
        private const val NAME = "NAME"
    }
}
