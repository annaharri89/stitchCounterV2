package com.example.stitchcounterv3.feature.projectDetail

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stitchcounterv3.domain.model.ProjectType
import com.example.stitchcounterv3.feature.navigation.RootNavGraph
import com.example.stitchcounterv3.feature.sharedComposables.RowProgressIndicator
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
        uri?.let {
            val savedImagePath = saveImageToInternalStorage(context, it, uiState.project?.id ?: 0)
            savedImagePath?.let { path ->
                viewModel.updateImagePath(path)
            }
        }
    }

    val scrollState = rememberScrollState()
    val isNewProject = uiState.project?.id == null || uiState.project.id == 0
    val isDoubleCounter = uiState.projectType == ProjectType.DOUBLE
    val keyboardController = LocalSoftwareKeyboardController.current
    val totalRowsFocusRequester = remember { FocusRequester() }
    val projectNotCreated = onCreateProject != null && isNewProject

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
                onBackClick = if (!isNewProject && uiState.project?.id != null && onNavigateBack != null) {
                    { onNavigateBack(uiState.project.id) }
                } else null
            )

            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Project Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Enter project title") },
                isError = uiState.titleError != null,
                supportingText = uiState.titleError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    imeAction = if (isDoubleCounter) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (isDoubleCounter) {
                            totalRowsFocusRequester.requestFocus()
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
                    label = { Text("Total Rows") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(totalRowsFocusRequester),
                    singleLine = true,
                    placeholder = { Text("Enter total rows") },
                    isError = uiState.totalRowsError != null,
                    supportingText = uiState.totalRowsError?.let { { Text(it) } },
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
                
                val rowProgress: Float? = uiState.project?.let { project ->
                    val totalRowsValue = project.totalRows
                    if (totalRowsValue > 0) {
                        (project.rowCounterNumber.toFloat() / totalRowsValue.toFloat()).coerceIn(0f, 1f)
                    } else {
                        null
                    }
                }
                
                RowProgressIndicator(
                    progress = rowProgress,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            ProjectImageSelector(
                imagePath = uiState.imagePath,
                onImageClick = { imagePickerLauncher.launch("image/*") },
                onRemoveImage = { viewModel.updateImagePath(null) },
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            }
        }

        if (projectNotCreated) {
            val isFormValid = uiState.title.isNotBlank() && 
                (!isDoubleCounter || (uiState.totalRows.toIntOrNull() ?: 0) > 0)
            Button(
                onClick = onCreateProject,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                enabled = isFormValid
            ) {
                Text("Create Project")
            }
        }
    }

    if (showDiscardDialog) {
        val isTitleEmpty = uiState.title.isBlank()
        AlertDialog(
            onDismissRequest = onDismissDiscardDialog,
            title = {
                Text(if (isTitleEmpty) "Title Required" else "Discard Changes?")
            },
            text = {
                Text(
                    if (isTitleEmpty) {
                        "Project title is required. Do you want to discard this project?"
                    } else {
                        "You have unsaved changes. Are you sure you want to discard them?"
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
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDiscardDialog) {
                    Text("Cancel")
                }
            }
        )
    }
}

@RootNavGraph
@Destination
@Composable
fun ProjectDetailScreen(
    projectId: Int,
    viewModel: ProjectDetailViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(projectId) {
        if (uiState.project == null || uiState.project?.id != projectId) {
            viewModel.loadProjectById(projectId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dismissalResult.collect { result ->
            when (result) {
                is com.example.stitchcounterv3.domain.model.DismissalResult.Allowed -> {
                    navigator.popBackStack()
                }
                is com.example.stitchcounterv3.domain.model.DismissalResult.Blocked -> {
                }
                is com.example.stitchcounterv3.domain.model.DismissalResult.ShowDiscardDialog -> {
                    showDiscardDialog = true
                }
            }
        }
    }

    BackHandler {
        viewModel.attemptDismissal()
    }

    ProjectDetailContent(
        uiState = uiState,
        viewModel = viewModel,
        context = context,
        showDiscardDialog = showDiscardDialog,
        onDismissDiscardDialog = { showDiscardDialog = false },
        onDiscard = {
            viewModel.discardChanges()
            showDiscardDialog = false
            navigator.popBackStack()
        },
        onCreateProject = null,
        onNavigateBack = null
    )
}
