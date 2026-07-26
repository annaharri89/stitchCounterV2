package dev.harrisonsoftware.stitchCounter.feature.projectDetail

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.feature.navigation.RootNavGraph
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.LoadFailureContent
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.RowProgressWithLabel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
fun ProjectDetailContent(
    uiState: ProjectDetailUiState,
    viewModel: ProjectDetailViewModel,
    context: Context,
    showDiscardDialog: Boolean,
    onDismissDiscardDialog: () -> Unit,
    onDiscard: () -> Unit,
    onCreateProject: (() -> Unit)?,
    onNavigateBack: ((Int) -> Unit)?
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.saveAndAddImage(context, it) }
    }

    var showImagePreview by rememberSaveable { mutableStateOf(false) }
    var imagePreviewStartIndex by rememberSaveable { mutableIntStateOf(0) }

    BackHandler(enabled = showImagePreview) {
        showImagePreview = false
    }

    val scrollState = rememberScrollState()
    val existingProjectId = uiState.project?.id?.takeIf { it > 0 }
    val isNewProject = existingProjectId == null
    val isDoubleCounter = uiState.projectType == ProjectType.DOUBLE
    val isRowAndRepeat = uiState.projectType == ProjectType.ROW_AND_REPEAT
    val keyboardController = LocalSoftwareKeyboardController.current
    val totalRowsFocusRequester = remember { FocusRequester() }
    val rowsPerRepeatFocusRequester = remember { FocusRequester() }
    val repeatGoalFocusRequester = remember { FocusRequester() }
    val projectNotCreated = onCreateProject != null && isNewProject

    val loadError = uiState.loadError

    if (loadError != null) {
        LoadFailureContent(
            messageResId = loadError,
            onClose = { viewModel.attemptDismissal() },
        )
    } else {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProjectDetailTopBar(
                isNewProject = isNewProject,
                onCloseClick = if (isNewProject) { { viewModel.attemptDismissal() } } else null,
                onBackClick = if (existingProjectId != null && onNavigateBack != null) {
                    { onNavigateBack(existingProjectId) }
                } else null
            )

            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text(stringResource(R.string.label_project_title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.placeholder_project_title)) },
                isError = uiState.titleError != null,
                supportingText = uiState.titleError?.let { errorResId -> { Text(stringResource(errorResId)) } },
                keyboardOptions = KeyboardOptions(
                    imeAction = if (isDoubleCounter || isRowAndRepeat) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        when {
                            isDoubleCounter -> totalRowsFocusRequester.requestFocus()
                            isRowAndRepeat -> rowsPerRepeatFocusRequester.requestFocus()
                        }
                    },
                    onDone = {
                        keyboardController?.hide()
                    }
                )
            )

            if (isDoubleCounter) {
                OutlinedTextField(
                    value = uiState.totalRows,
                    onValueChange = { viewModel.updateTotalRows(it) },
                    label = { Text(stringResource(R.string.label_total_rows)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(totalRowsFocusRequester),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.placeholder_total_rows)) },
                    isError = uiState.totalRowsError != null,
                    supportingText = uiState.totalRowsError?.let { errorResId -> { Text(stringResource(errorResId)) } },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                )
            }

            if (isRowAndRepeat) {
                ProjectDetailRowAndRepeatFields(
                    rowsPerRepeat = uiState.rowsPerRepeat,
                    repeatGoal = uiState.totalRows,
                    rowsPerRepeatError = uiState.rowsPerRepeatError,
                    repeatGoalError = uiState.totalRowsError,
                    rowsPerRepeatFocusRequester = rowsPerRepeatFocusRequester,
                    repeatGoalFocusRequester = repeatGoalFocusRequester,
                    keyboardController = keyboardController,
                    onRowsPerRepeatChange = viewModel::updateRowsPerRepeat,
                    onRepeatGoalChange = viewModel::updateTotalRows,
                )
            }

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text(stringResource(R.string.label_project_notes)) },
                placeholder = { Text(stringResource(R.string.placeholder_project_notes)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = context.getString(R.string.label_project_notes)
                    },
                minLines = 4
            )

            ProjectImageSelector(
                imagePaths = uiState.imagePaths,
                onImageClick = { imagePickerLauncher.launch("image/*") },
                onRemoveImage = { imagePath -> viewModel.removeImagePath(imagePath) },
                onTapImage = { index ->
                    imagePreviewStartIndex = index
                    showImagePreview = true
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (!isNewProject) {
                if (isDoubleCounter) {
                    uiState.project?.let { project ->
                        RowProgressWithLabel(
                            currentRowCount = project.rowCounterNumber,
                            totalRows = project.totalRows,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = uiState.isCompleted,
                            onValueChange = { viewModel.toggleCompleted(it) },
                            role = Role.Switch
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.label_project_completed),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = uiState.isCompleted,
                        onCheckedChange = null
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            }
        }

        if (projectNotCreated) {
            val isFormValid = uiState.title.isNotBlank() && when {
                isDoubleCounter -> (uiState.totalRows.toIntOrNull() ?: 0) > 0
                isRowAndRepeat -> {
                    (uiState.rowsPerRepeat.toIntOrNull() ?: 0) > 0 &&
                        (uiState.totalRows.toIntOrNull() ?: 0) > 0
                }
                else -> true
            }
            Button(
                onClick = onCreateProject,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                enabled = isFormValid
            ) {
                Text(stringResource(R.string.action_create_project))
            }
        }
    }
    }

    if (showDiscardDialog) {
        val isTitleEmpty = uiState.title.isBlank()
        AlertDialog(
            onDismissRequest = onDismissDiscardDialog,
            title = {
                Text(
                    if (isTitleEmpty) stringResource(R.string.dialog_title_required)
                    else stringResource(R.string.dialog_discard_changes)
                )
            },
            text = {
                Text(
                    if (isTitleEmpty) {
                        stringResource(R.string.dialog_title_required_message)
                    } else {
                        stringResource(R.string.dialog_discard_changes_message)
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDiscard()
                    },
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

    if (showImagePreview && uiState.imagePaths.isNotEmpty()) {
        ImagePreviewBottomSheet(
            imagePaths = uiState.imagePaths,
            initialPageIndex = imagePreviewStartIndex,
            onDismiss = { showImagePreview = false }
        )
    }
}

@Composable
fun ProjectDetailScreenContent(
    projectId: Int? = null,
    projectType: ProjectType? = null,
    viewModel: ProjectDetailViewModel = hiltViewModel(),
    onNavigateBack: ((Int) -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    onCreateProject: (() -> Unit)? = null,
    isDiscardDialogManagedBySheet: Boolean = false,
    showDiscardDialog: Boolean = false,
    onDismissDiscardDialog: () -> Unit = {},
    onConfirmDiscard: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val internalShowDiscardDialog = remember { mutableStateOf(false) }
    val resolvedShowDiscardDialog = if (isDiscardDialogManagedBySheet) {
        showDiscardDialog
    } else {
        internalShowDiscardDialog.value
    }

    LaunchedEffect(projectId, projectType) {
        if (projectId != null && projectId > 0) {
            viewModel.loadProjectById(projectId)
        } else if (projectType != null) {
            viewModel.loadProject(null, projectType)
        }
    }

    if (!isDiscardDialogManagedBySheet) {
        LaunchedEffect(Unit) {
            viewModel.dismissalResult.collect { result ->
                when (result) {
                    is dev.harrisonsoftware.stitchCounter.domain.model.DismissalResult.Allowed -> {
                        onDismiss?.invoke()
                    }
                    is dev.harrisonsoftware.stitchCounter.domain.model.DismissalResult.ShowDiscardDialog -> {
                        internalShowDiscardDialog.value = true
                    }
                }
            }
        }
    }

    if (onDismiss != null || isDiscardDialogManagedBySheet) {
        BackHandler {
            viewModel.attemptDismissal()
        }
    }

    ProjectDetailContent(
        uiState = uiState,
        viewModel = viewModel,
        context = context,
        showDiscardDialog = resolvedShowDiscardDialog,
        onDismissDiscardDialog = if (isDiscardDialogManagedBySheet) {
            onDismissDiscardDialog
        } else {
            { internalShowDiscardDialog.value = false }
        },
        onDiscard = if (isDiscardDialogManagedBySheet) {
            onConfirmDiscard
        } else {
            {
                viewModel.discardChanges()
                internalShowDiscardDialog.value = false
                onDismiss?.invoke()
                Unit
            }
        },
        onCreateProject = onCreateProject,
        onNavigateBack = onNavigateBack
    )
}

@RootNavGraph
@Destination
@Composable
fun ProjectDetailScreen(
    projectId: Int,
    viewModel: ProjectDetailViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    ProjectDetailScreenContent(
        projectId = projectId,
        projectType = null,
        viewModel = viewModel,
        onNavigateBack = null,
        onDismiss = { navigator.popBackStack() },
        onCreateProject = null
    )
}
