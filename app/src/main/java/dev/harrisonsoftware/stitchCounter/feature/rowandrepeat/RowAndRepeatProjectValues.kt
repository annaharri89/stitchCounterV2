package dev.harrisonsoftware.stitchCounter.feature.rowandrepeat

internal object RowAndRepeatProjectValues {
    fun rowsPerRepeatFromStoredValue(rowAdjustment: Int): Int =
        if (rowAdjustment > 1) {
            rowAdjustment
        } else {
            RowAndRepeatUiState.DEFAULT_ROWS_PER_REPEAT
        }

    fun repeatGoalFromStoredValue(totalRows: Int): Int =
        if (totalRows > 0) {
            totalRows
        } else {
            RowAndRepeatUiState.DEFAULT_REPEAT_GOAL
        }

    fun rowsPerRepeatInputFromStoredValue(rowAdjustment: Int): String =
        rowsPerRepeatFromStoredValue(rowAdjustment).toString()

    fun repeatGoalInputFromStoredValue(totalRows: Int): String =
        if (totalRows > 0) totalRows.toString() else ""
}
