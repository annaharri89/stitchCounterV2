package dev.harrisonsoftware.stitchCounter.feature.projectDetail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import dev.harrisonsoftware.stitchCounter.R

@Composable
internal fun ProjectDetailRowAndRepeatFields(
    rowsPerRepeat: String,
    repeatGoal: String,
    rowsPerRepeatError: Int?,
    repeatGoalError: Int?,
    rowsPerRepeatFocusRequester: FocusRequester,
    repeatGoalFocusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    onRowsPerRepeatChange: (String) -> Unit,
    onRepeatGoalChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = rowsPerRepeat,
        onValueChange = onRowsPerRepeatChange,
        label = { Text(stringResource(R.string.row_and_repeat_rows_per_repeat_label)) },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(rowsPerRepeatFocusRequester),
        singleLine = true,
        isError = rowsPerRepeatError != null,
        supportingText = rowsPerRepeatError?.let { errorResId -> { Text(stringResource(errorResId)) } },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number,
        ),
        keyboardActions = KeyboardActions(
            onNext = { repeatGoalFocusRequester.requestFocus() },
        ),
    )

    OutlinedTextField(
        value = repeatGoal,
        onValueChange = onRepeatGoalChange,
        label = { Text(stringResource(R.string.row_and_repeat_repeat_goal_label)) },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(repeatGoalFocusRequester),
        singleLine = true,
        isError = repeatGoalError != null,
        supportingText = repeatGoalError?.let { errorResId -> { Text(stringResource(errorResId)) } },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Number,
        ),
        keyboardActions = KeyboardActions(
            onDone = { keyboardController?.hide() },
        ),
    )
}
