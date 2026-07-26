package dev.harrisonsoftware.stitchCounter.feature.rowandrepeat

interface RowAndRepeatActions {
    fun incrementRow()
    fun decrementRow()
    fun resetRow()
    fun incrementRepeat()
    fun decrementRepeat()
    fun resetAll()

    companion object {
        val NoOp = object : RowAndRepeatActions {
            override fun incrementRow() {}
            override fun decrementRow() {}
            override fun resetRow() {}
            override fun incrementRepeat() {}
            override fun decrementRepeat() {}
            override fun resetAll() {}
        }
    }
}
