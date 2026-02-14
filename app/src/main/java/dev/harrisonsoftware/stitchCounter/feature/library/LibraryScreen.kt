package dev.harrisonsoftware.stitchCounter.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.feature.navigation.RootNavGraph
import dev.harrisonsoftware.stitchCounter.feature.navigation.RootNavigationViewModel
import dev.harrisonsoftware.stitchCounter.feature.navigation.SheetScreen
import dev.harrisonsoftware.stitchCounter.feature.navigation.createSheetScreenForProjectType
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator


@RootNavGraph(start = true)
@Destination
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    rootNavigationViewModel: RootNavigationViewModel,
    navigator: DestinationsNavigator
) {
    val projects by viewModel.projects.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showNewProjectDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (uiState.isMultiSelectMode) {
                MultiSelectTopBar(
                    selectedCount = uiState.selectedProjectIds.size,
                    totalCount = projects.size,
                    onSelectAll = { viewModel.selectAllProjects() },
                    onClearSelection = { viewModel.clearSelection() },
                    onDelete = { viewModel.requestBulkDelete() },
                    onCancel = { viewModel.toggleMultiSelectMode() }
                )
            } else {
                LibraryTopBar(
                    onEnterMultiSelect = { viewModel.toggleMultiSelectMode() },
                    hasProjects = projects.isNotEmpty()
                )
            }
        },
        floatingActionButton = {
            if (!uiState.isMultiSelectMode) {
                val fabDescription = stringResource(R.string.cd_create_new_project)
                FloatingActionButton(
                    onClick = { showNewProjectDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics {
                        contentDescription = fabDescription
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
            }
        }
    ) { paddingValues ->
        Surface(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    LoadingState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
                projects.isEmpty() -> {
                    EmptyLibraryState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = paddingValues.calculateTopPadding(),
                            bottom = 80.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(projects) { project ->
                            SwipeableProjectRow(
                                project = project,
                                isSelected = uiState.selectedProjectIds.contains(project.id),
                                isMultiSelectMode = uiState.isMultiSelectMode,
                                onOpen = { 
                                    if (!uiState.isMultiSelectMode) {
                                        rootNavigationViewModel.showBottomSheet(
                                            createSheetScreenForProjectType(project.type, project.id)
                                        )
                                    }
                                },
                                onSelect = { viewModel.toggleProjectSelection(project.id) },
                                onDelete = { viewModel.requestDelete(project) },
                                onToggleMultiSelect = { viewModel.toggleMultiSelectMode() },
                                onInfoClick = {
                                    if (!uiState.isMultiSelectMode && project.id > 0) {
                                        rootNavigationViewModel.showBottomSheet(
                                            SheetScreen.ProjectDetail(
                                                projectId = project.id,
                                                projectType = project.type
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showDeleteConfirmation) {
        DeleteConfirmationDialog(
            projectCount = uiState.projectsToDelete.size,
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.cancelDelete() }
        )
    }

    if (showNewProjectDialog) {
        NewProjectDialog(
            onSelectSingleCounter = {
                showNewProjectDialog = false
                rootNavigationViewModel.showBottomSheet(
                    SheetScreen.ProjectDetail(projectId = null, projectType = ProjectType.SINGLE)
                )
            },
            onSelectDoubleCounter = {
                showNewProjectDialog = false
                rootNavigationViewModel.showBottomSheet(
                    SheetScreen.ProjectDetail(projectId = null, projectType = ProjectType.DOUBLE)
                )
            },
            onDismiss = { showNewProjectDialog = false }
        )
    }
}

@Composable
fun NewProjectDialog(
    onSelectSingleCounter: () -> Unit,
    onSelectDoubleCounter: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.new_project_dialog_title),
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSelectSingleCounter,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.new_project_single_counter))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onSelectDoubleCounter,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(stringResource(R.string.new_project_double_counter))
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.action_cancel),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}