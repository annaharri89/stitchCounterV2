package dev.harrisonsoftware.stitchCounter.feature.library

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.feature.navigation.SheetScreen

@Composable
internal fun LibraryProjectCollection(
    projects: List<Project>,
    selectedProjectIds: Set<Int>,
    isMultiSelectMode: Boolean,
    currentSheet: SheetScreen?,
    contentPadding: PaddingValues,
    onOpen: (Project) -> Unit,
    onSelect: (Project) -> Unit,
    onDelete: (Project) -> Unit,
    onToggleMultiSelect: () -> Unit,
    onInfoClick: (Project) -> Unit
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val density = LocalDensity.current
    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyGridState()
    var tallestProjectHeightPx by remember(projects, isMultiSelectMode) { mutableIntStateOf(0) }
    var projectCountWhenSheetOpened by remember { mutableIntStateOf(-1) }
    var sheetWasOpenedDuringSession by remember { mutableStateOf(false) }

    LaunchedEffect(currentSheet) {
        if (!sheetWasOpenedDuringSession && currentSheet != null) {
            sheetWasOpenedDuringSession = true
            projectCountWhenSheetOpened = projects.size
        }
        if (sheetWasOpenedDuringSession && currentSheet == null) {
            sheetWasOpenedDuringSession = false
        }
    }

    val isScrollInProgress = if (isLandscape) {
        lazyGridState.isScrollInProgress
    } else {
        lazyListState.isScrollInProgress
    }
    LaunchedEffect(currentSheet, projects.size, isScrollInProgress, isLandscape) {
        if (
            currentSheet == null &&
            !isScrollInProgress &&
            projectCountWhenSheetOpened >= 0 &&
            projects.size > projectCountWhenSheetOpened
        ) {
            if (isLandscape) {
                lazyGridState.animateScrollToItem(projects.lastIndex)
            } else {
                lazyListState.animateScrollToItem(projects.lastIndex)
            }
            projectCountWhenSheetOpened = -1
        }
    }

    val equalHeightModifier = Modifier
        .heightIn(min = with(density) { tallestProjectHeightPx.toDp() })
        .onSizeChanged { size ->
            if (size.height > tallestProjectHeightPx) {
                tallestProjectHeightPx = size.height
            }
        }

    if (isLandscape) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(LANDSCAPE_GRID_COLUMN_COUNT),
            state = lazyGridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(PROJECT_SPACING),
            verticalArrangement = Arrangement.spacedBy(PROJECT_SPACING)
        ) {
            items(projects, key = { it.id }) { project ->
                LibraryProjectItem(
                    modifier = equalHeightModifier,
                    project = project,
                    selectedProjectIds = selectedProjectIds,
                    isMultiSelectMode = isMultiSelectMode,
                    onOpen = onOpen,
                    onSelect = onSelect,
                    onDelete = onDelete,
                    onToggleMultiSelect = onToggleMultiSelect,
                    onInfoClick = onInfoClick
                )
            }
        }
    } else {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(PROJECT_SPACING)
        ) {
            items(projects, key = { it.id }) { project ->
                LibraryProjectItem(
                    modifier = equalHeightModifier,
                    project = project,
                    selectedProjectIds = selectedProjectIds,
                    isMultiSelectMode = isMultiSelectMode,
                    onOpen = onOpen,
                    onSelect = onSelect,
                    onDelete = onDelete,
                    onToggleMultiSelect = onToggleMultiSelect,
                    onInfoClick = onInfoClick
                )
            }
        }
    }
}

@Composable
private fun LibraryProjectItem(
    modifier: Modifier = Modifier,
    project: Project,
    selectedProjectIds: Set<Int>,
    isMultiSelectMode: Boolean,
    onOpen: (Project) -> Unit,
    onSelect: (Project) -> Unit,
    onDelete: (Project) -> Unit,
    onToggleMultiSelect: () -> Unit,
    onInfoClick: (Project) -> Unit
) {
    SwipeableProjectRow(
        modifier = modifier,
        project = project,
        isSelected = selectedProjectIds.contains(project.id),
        isMultiSelectMode = isMultiSelectMode,
        onOpen = { onOpen(project) },
        onSelect = { onSelect(project) },
        onDelete = { onDelete(project) },
        onToggleMultiSelect = onToggleMultiSelect,
        onInfoClick = { onInfoClick(project) }
    )
}

private val PROJECT_SPACING = 12.dp
private const val LANDSCAPE_GRID_COLUMN_COUNT = 2
