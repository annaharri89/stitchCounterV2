package com.example.stitchcounterv3.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stitchcounterv3.domain.model.ProjectType
import com.example.stitchcounterv3.feature.destinations.ProjectDetailScreenDestination
import com.example.stitchcounterv3.feature.navigation.RootNavGraph
import com.example.stitchcounterv3.feature.navigation.RootNavigationViewModel
import com.example.stitchcounterv3.feature.navigation.SheetScreen
import com.example.stitchcounterv3.feature.navigation.createSheetScreenForProjectType
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator


@RootNavGraph
@Destination
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    rootNavigationViewModel: RootNavigationViewModel,
    navigator: DestinationsNavigator
) {
    val projects by viewModel.projects.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

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
                                        navigator.navigate(ProjectDetailScreenDestination(projectId = project.id))
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
}

