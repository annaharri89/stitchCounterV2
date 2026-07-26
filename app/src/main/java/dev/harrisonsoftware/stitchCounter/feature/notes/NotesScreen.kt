package dev.harrisonsoftware.stitchCounter.feature.notes

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.feature.navigation.RootNavGraph
import dev.harrisonsoftware.stitchCounter.feature.navigation.RootNavigationViewModel
import dev.harrisonsoftware.stitchCounter.feature.navigation.SheetScreen
import dev.harrisonsoftware.stitchCounter.feature.navigation.createNoteSheetForNoteId

@RootNavGraph
@Destination
@Composable
fun NotesScreen(
    viewModel: NotesViewModel = hiltViewModel(),
    rootNavigationViewModel: RootNavigationViewModel,
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val currentSheet by rootNavigationViewModel.currentSheet.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { NotesTopBar() },
        floatingActionButton = {
            val fabDescription = stringResource(R.string.cd_create_new_note)
            FloatingActionButton(
                onClick = { rootNavigationViewModel.showBottomSheet(createNoteSheetForNoteId(noteId = null)) },
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
    ) { paddingValues ->
        Surface(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    NotesLoadingState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
                notes.isEmpty() -> {
                    EmptyNotesState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
                else -> {
                    NotesCollection(
                        notes = notes,
                        currentSheet = currentSheet,
                        contentPadding = PaddingValues(
                            top = paddingValues.calculateTopPadding(),
                            bottom = 80.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                        onNoteClick = { note ->
                            rootNavigationViewModel.showBottomSheet(
                                createNoteSheetForNoteId(noteId = note.id)
                            )
                        },
                        onNoteDelete = { note -> viewModel.requestDelete(note) },
                    )
                }
            }
        }
    }

    if (uiState.showDeleteConfirmation) {
        DeleteNoteConfirmationDialog(
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.cancelDelete() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesTopBar() {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.nav_notes),
                modifier = Modifier.semantics { heading() }
            )
        }
    )
}
