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
package io.github.annaharri89.stitchcounter.dataObjects

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import io.github.annaharri89.stitchcounter.R
import io.github.annaharri89.stitchcounter.db.WriteToDb
import io.github.annaharri89.stitchcounter.utilities.Utils

/**
 * Created by ETASpare on 6/8/2017.
 */
class OldCounter: Parcelable {
    @JvmField
    var ID: Int = 0
    @JvmField
    var counterNumber: Int = 0
    @JvmField
    var adjustment: Int = 1
    @JvmField
    var totalRows: Int = 0
    protected var progressPercent: Double = 0.0
    private val COUNTER_MIN = 0
    private val COUNTER_MAX = 9999
    private var textCounter: TextView? = null
    private var textProgress: TextView? = null
    private var strResCounter: String? = null
    var strResProgress: String? = null
    @JvmField
    var projectName: String? = null
    private var btnAdjustment1: Button? = null
    private var btnAdjustment5: Button? = null
    private var btnAdjustment10: Button? = null
    private var progressBar: ProgressBar? = null
    private var res: Resources? = null
    private lateinit var context: Context

    /* Defines the kind of object that will be parcelled */
    override fun describeContents(): Int {
        return 0
    }

    /*
     Actual object serialization happens here, Write object content
     to parcel, reading should be done according to this write order
    */
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(ID)
        dest.writeInt(counterNumber)
        dest.writeInt(adjustment)
        dest.writeInt(totalRows)
        dest.writeString(projectName)
    }

    /*
    Parcelable Counter's constructor. Used to instantiate counters that need saved when navigation
    to the library occurs.
    */
    constructor(`in`: Parcel) {
        this.ID = `in`.readInt()
        this.counterNumber = `in`.readInt()
        this.adjustment = `in`.readInt()
        this.totalRows = `in`.readInt()
        this.projectName = `in`.readString()
    }

    /*
    Double Counter's constructor, instantiates new instance of counter class, setting all instance variables and context.
    Handles progress related instance variables.
    */
    constructor(
        context: Context,
        textCounter: TextView?,
        textProgress: TextView?,
        strResCounterID: Int,
        adjustment1: Button?,
        adjustment5: Button?,
        adjustment10: Button?,
        progress: ProgressBar?
    ) {
        this.res = context.resources
        this.context = context
        this.textCounter = textCounter
        this.textProgress = textProgress
        this.strResCounter = res?.getString(strResCounterID)
        this.strResProgress = res?.getString(R.string.counter_progress)
        this.btnAdjustment1 = adjustment1
        this.btnAdjustment5 = adjustment5
        this.btnAdjustment10 = adjustment10
        this.progressBar = progress
        this.setCounter()
    }

    /*
    Single Counter's constructor, instantiates new instance of counter class, setting all instance variables and context.
    Doesn't have progress related instance variables.
    */
    constructor(
        context: Context,
        textCounter: TextView?,
        strResCounterID: Int,
        adjustment1: Button?,
        adjustment5: Button?,
        adjustment10: Button?
    ) {
        this.res = context.resources
        this.context = context
        this.textCounter = textCounter
        this.strResCounter = res?.getString(strResCounterID)
        this.btnAdjustment1 = adjustment1
        this.btnAdjustment5 = adjustment5
        this.btnAdjustment10 = adjustment10
        this.setCounter()
    }

    /*
    Formats the counter string with counter number. Sets the
    counter TextView with formattedCounterNumber.
     */
    fun setCounter() {
        val formattedCounterNumber = String.format(strResCounter!!, this.counterNumber)
        textCounter!!.text = formattedCounterNumber
    }

    fun setProjectName(name: String?) {
        this.projectName = name
    }

    /*
    Takes counterNumber as a double and divides it by totalrows (which results in a fraction),
    multiplies that by 100 to get percent. Rounds the percent up.
    */
    private fun findPercent(): Double {
        this.progressPercent =
            Math.round(counterNumber.toDouble() / this.totalRows * 100).toDouble()
        return this.progressPercent
    }

    /*
    Set's the counter's totalRows to passed num and sets the passed progress bar's max num to totalRows.
    Sets the progress.
    */
    fun setProgressBarMax(num: Int) {
        this.totalRows = num
        progressBar!!.max = this.totalRows
        setProgress()
    }

    /*
    If the progress bar exists and totalRows is greater than 0, set its progress
    (counterNumber / totalRows)
     */
    fun setProgress() {
        if (this.progressBar != null && this.totalRows > 0) {
            progressBar!!.progress = this.counterNumber
            val formattedProgressNumber = String.format(strResProgress!!, this.findPercent())
            textProgress!!.text = formattedProgressNumber
        }
    }

    /*
    If counter number is less than max, increase it by adjustment. Then calls setCounter to get
    the number to the appropriate TextView. If counterNumber goes above COUNTER_MAX (ex: at 9995,
    increase by 10 to go to 10005), set counterNumber to COUNTER_MAX (9999). Sets the progress.
    */
    fun incrementCounter() {
        if (this.counterNumber < this.COUNTER_MAX) this.counterNumber =
            this.counterNumber + this.adjustment
        if (this.counterNumber > this.COUNTER_MAX) this.counterNumber = this.COUNTER_MAX
        setCounter()
        setProgress()
    }

    /*
    If counter number is greater than min, decrease it by 1. Then calls setCounter to get the
    number to the appropriate TextView. If counterNumber drops below COUNTER_MIN (ex: at 5,
    decrease by 10 to go to -5), set counterNumber to COUNTER_MIN (0). Sets the progress.
    */
    fun decrementCounter() {
        if (this.counterNumber > this.COUNTER_MIN) this.counterNumber =
            this.counterNumber - this.adjustment
        if (this.counterNumber < this.COUNTER_MIN) this.counterNumber = this.COUNTER_MIN
        setCounter()
        setProgress()
    }

    /*
    Resets the appropriate counter to 0 and sets the progress bar's progress
    */
    fun resetCounter() {
        this.counterNumber = this.COUNTER_MIN
        setCounter()
        setProgress()
    }

    /*
    Creates a dialog that asks the user if they're sure they want to reset the counter and either
    resets the counter or does nothing, depending on if the user presses yes or no.
    */
    fun resetCounterCheck(counterType: String?) {
        val builder = AlertDialog.Builder(
            context
        )
        val formattedDialogMessage =
            String.format(res!!.getString(R.string.dialog_reset_message), counterType)
        builder.setMessage(formattedDialogMessage)
            .setPositiveButton(R.string.button_yes) { dialog, id -> resetCounter() }
            .setNegativeButton(R.string.button_no) { dialog, id ->
                // User cancelled the dialog
            }

        val dialog = builder.create()
        dialog.show()
        val dialogTextView = dialog.findViewById<View>(android.R.id.message) as TextView?
        if (dialogTextView != null) {
            dialogTextView.textSize = 20f
        }
    }

    /*
    Set's the counter's adjustment number to passed num.
    */
    fun changeAdjustmentNum(num: Int) {
        this.adjustment = num
    }

    /*
    Sets the counter's adjustment variable to 1, 5, or 10 and calls setAdjustmentButtonColor
    depending on which adjustment button has been pressed.
    */
    fun changeAdjustment(adjustmentNum: Int) {
        when (adjustmentNum) {
            1 -> {
                this.changeAdjustmentNum(1)
                this.setActiveAdjustmentButtonColor(this.btnAdjustment1)
                this.setInActiveAdjustmentButtonColor(this.btnAdjustment5, this.btnAdjustment10)
            }

            5 -> {
                this.changeAdjustmentNum(5)
                this.setActiveAdjustmentButtonColor(this.btnAdjustment5)
                this.setInActiveAdjustmentButtonColor(this.btnAdjustment1, this.btnAdjustment10)
            }

            10 -> {
                this.changeAdjustmentNum(10)
                this.setActiveAdjustmentButtonColor(this.btnAdjustment10)
                this.setInActiveAdjustmentButtonColor(this.btnAdjustment1, this.btnAdjustment5)
            }

            else -> throw RuntimeException("Unknown button")
        }
    }

    /*
    Sets the Active adjustment button's background and font colors. Has compatibility 'if' logic
    to handle getResources().getColor deprecation in Android Support Library 23.
    */
    private fun setActiveAdjustmentButtonColor(activeButton: Button?) {
        val utils = Utils(
            context
        )
        if (Build.VERSION.SDK_INT < 23) {
            /* Android Support Library 22 and earlier compatible */
            /* Sets Button Color */
            (activeButton!!.background as GradientDrawable).setColor(
                res!!.getColor(utils.determineActiveCapsuleButtonColor())
            )
            /* Sets Font Color */
            activeButton.setTextColor(res!!.getColor(utils.determineActiveCapsuleButtonTextColor()))
        } else {
            /* Android Support Library 23 compatible */
            /* Sets Button Color */
            (activeButton!!.background as GradientDrawable).setColor(
                ContextCompat.getColor(
                    context, utils.determineActiveCapsuleButtonColor()
                )
            )
            /* Sets Font Color */
            activeButton.setTextColor(
                ContextCompat.getColor(
                    context,
                    utils.determineActiveCapsuleButtonTextColor()
                )
            )
        }
    }

    /*
    Sets the Inactive adjustment buttons' background and font colors. Has compatibility 'if' logic
    to handle getResources().getColor deprecation in Android Support Library 23.
    */
    private fun setInActiveAdjustmentButtonColor(
        inactiveButton1: Button?,
        inactiveButton2: Button?
    ) {
        val utils = Utils(
            context
        )

        if (Build.VERSION.SDK_INT < 23) {
            /* Android Support Library 22 and earlier compatible */
            /* Sets Font Color */
            inactiveButton1!!.setTextColor(res!!.getColor(utils.determineInActiveCapsuleButtonTextColor()))
            inactiveButton2!!.setTextColor(res!!.getColor(utils.determineInActiveCapsuleButtonTextColor()))
            /* Sets Button Color */
            (inactiveButton1.background as GradientDrawable).setColor(
                res!!.getColor(utils.determineInActiveCapsuleButtonColor())
            )
            (inactiveButton2.background as GradientDrawable).setColor(
                res!!.getColor(utils.determineInActiveCapsuleButtonColor())
            )
        } else {
            /* Android Support Library 23 compatible */
            /* Sets Font Color */
            inactiveButton1!!.setTextColor(
                ContextCompat.getColor(
                    context!!,
                    utils.determineInActiveCapsuleButtonTextColor()
                )
            )
            inactiveButton2!!.setTextColor(
                ContextCompat.getColor(
                    context!!,
                    utils.determineInActiveCapsuleButtonTextColor()
                )
            )
            /* Sets Button Color */
            (inactiveButton1.background as GradientDrawable).setColor(
                ContextCompat.getColor(
                    context!!, utils.determineInActiveCapsuleButtonColor()
                )
            )
            (inactiveButton2.background as GradientDrawable).setColor(
                ContextCompat.getColor(
                    context!!, utils.determineInActiveCapsuleButtonColor()
                )
            )
        }
    }

    /* Save Counter */
    fun saveCounter(oldCounter1: OldCounter?, oldCounter2: OldCounter?) {
        val writeToDb = WriteToDb(this.context)
        if (oldCounter2 != null) {
            writeToDb.execute(oldCounter1, oldCounter2)
        } else {
            writeToDb.execute(oldCounter1)
        }
    }

    companion object {
        /*
    This field is needed for Android to be able to
    create new objects, individually or as arrays
    */
        @JvmField
        val CREATOR: Parcelable.Creator<OldCounter> = object : Parcelable.Creator<OldCounter> {
            override fun createFromParcel(`in`: Parcel): OldCounter {
                return OldCounter(`in`)
            }

            override fun newArray(size: Int) =  arrayOfNulls<OldCounter>(size)
        }
    }
}

