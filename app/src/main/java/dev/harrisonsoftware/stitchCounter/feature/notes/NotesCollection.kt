package dev.harrisonsoftware.stitchCounter.feature.notes

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
import dev.harrisonsoftware.stitchCounter.domain.model.Note
import dev.harrisonsoftware.stitchCounter.feature.navigation.SheetScreen
import dev.harrisonsoftware.stitchCounter.feature.navigation.isCreateNoteSheet

@Composable
internal fun NotesCollection(
    notes: List<Note>,
    currentSheet: SheetScreen?,
    contentPadding: PaddingValues,
    onNoteClick: (Note) -> Unit,
    onNoteDelete: (Note) -> Unit,
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val density = LocalDensity.current
    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyGridState()
    var tallestNoteHeightPx by remember(notes) { mutableIntStateOf(0) }
    var noteCountWhenSheetOpened by remember { mutableIntStateOf(-1) }
    var sheetWasOpenedDuringSession by remember { mutableStateOf(false) }

    LaunchedEffect(currentSheet) {
        if (!sheetWasOpenedDuringSession && isCreateNoteSheet(currentSheet)) {
            sheetWasOpenedDuringSession = true
            noteCountWhenSheetOpened = notes.size
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
    LaunchedEffect(currentSheet, notes.size, isScrollInProgress, isLandscape) {
        if (
            currentSheet == null &&
            !isScrollInProgress &&
            noteCountWhenSheetOpened >= 0 &&
            notes.size > noteCountWhenSheetOpened
        ) {
            if (isLandscape) {
                lazyGridState.animateScrollToItem(notes.lastIndex)
            } else {
                lazyListState.animateScrollToItem(notes.lastIndex)
            }
            noteCountWhenSheetOpened = -1
        }
    }

    val equalHeightModifier = Modifier
        .heightIn(min = with(density) { tallestNoteHeightPx.toDp() })
        .onSizeChanged { size ->
            if (size.height > tallestNoteHeightPx) {
                tallestNoteHeightPx = size.height
            }
        }

    if (isLandscape) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(LANDSCAPE_GRID_COLUMN_COUNT),
            state = lazyGridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(NOTE_SPACING),
            verticalArrangement = Arrangement.spacedBy(NOTE_SPACING)
        ) {
            items(notes, key = { it.id }) { note ->
                SwipeableNoteRow(
                    note = note,
                    onClick = { onNoteClick(note) },
                    onDelete = { onNoteDelete(note) },
                    modifier = equalHeightModifier,
                )
            }
        }
    } else {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(NOTE_SPACING)
        ) {
            items(notes, key = { it.id }) { note ->
                SwipeableNoteRow(
                    note = note,
                    onClick = { onNoteClick(note) },
                    onDelete = { onNoteDelete(note) },
                    modifier = equalHeightModifier,
                )
            }
        }
    }
}

private val NOTE_SPACING = 12.dp
private const val LANDSCAPE_GRID_COLUMN_COUNT = 2
