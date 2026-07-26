package dev.harrisonsoftware.stitchCounter.feature.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.LoadFailureContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoteScreenContent(
    viewModel: CreateNoteViewModel,
    showDiscardDialog: Boolean,
    onDismissDiscardDialog: () -> Unit,
    onConfirmDiscard: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val closeDescription = stringResource(R.string.cd_close)
    val noteBodyDescription = stringResource(R.string.cd_note_body)
    val sheetTitleResId = if (uiState.isEditMode) {
        R.string.edit_note_title
    } else {
        R.string.create_note_title
    }
    val isFormEnabled = !uiState.isLoading && !uiState.isSaving && !uiState.isSaved
    val loadError = uiState.loadError

    BackHandler {
        viewModel.attemptDismissal()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(sheetTitleResId)) },
            navigationIcon = {
                IconButton(onClick = { viewModel.attemptDismissal() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = closeDescription
                    )
                }
            }
        )

        if (loadError != null) {
            LoadFailureContent(
                messageResId = loadError,
                onClose = { viewModel.attemptDismissal() },
            )
        } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text(stringResource(R.string.label_note_title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.placeholder_note_title)) },
                isError = uiState.titleError != null,
                enabled = isFormEnabled,
                supportingText = uiState.titleError?.let { errorResId ->
                    { Text(stringResource(errorResId)) }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = uiState.body,
                onValueChange = { viewModel.updateBody(it) },
                placeholder = { Text(stringResource(R.string.placeholder_note_body)) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = noteBodyDescription
                    },
                enabled = isFormEnabled,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                keyboardActions = KeyboardActions.Default,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.surface,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.surface,
                    disabledIndicatorColor = MaterialTheme.colorScheme.surface,
                ),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.saveNote() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving && !uiState.isSaved,
            ) {
                Text(stringResource(R.string.action_save))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = onDismissDiscardDialog,
            title = { Text(stringResource(R.string.dialog_discard_changes)) },
            text = { Text(stringResource(R.string.dialog_discard_changes_message)) },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDiscard,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.action_discard))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDiscardDialog) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
