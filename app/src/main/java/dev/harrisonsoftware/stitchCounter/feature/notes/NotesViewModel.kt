package dev.harrisonsoftware.stitchCounter.feature.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.harrisonsoftware.stitchCounter.Constants
import dev.harrisonsoftware.stitchCounter.domain.model.Note
import dev.harrisonsoftware.stitchCounter.domain.usecase.DeleteNote
import dev.harrisonsoftware.stitchCounter.domain.usecase.ObserveNotes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

data class NotesUiState(
    val isLoading: Boolean = true,
    val showDeleteConfirmation: Boolean = false,
    val noteToDelete: Note? = null,
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    observeNotes: ObserveNotes,
    private val deleteNote: DeleteNote,
) : ViewModel() {
    val notes: StateFlow<List<Note>> = observeNotes()
        .onEach {
            if (_uiState.value.isLoading) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    fun requestDelete(note: Note) {
        Timber.tag(Constants.LOG_TAG_NOTES_VIEW_MODEL)
            .i("event=delete_requested noteId=${note.id} title=${note.title}")
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmation = true,
            noteToDelete = note,
        )
    }

    fun confirmDelete() {
        val noteToDelete = _uiState.value.noteToDelete ?: return
        Timber.tag(Constants.LOG_TAG_NOTES_VIEW_MODEL)
            .i("event=delete_confirmed noteId=${noteToDelete.id}")
        viewModelScope.launch {
            deleteNote(noteToDelete)
        }
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmation = false,
            noteToDelete = null,
        )
    }

    fun cancelDelete() {
        val noteToDelete = _uiState.value.noteToDelete
        Timber.tag(Constants.LOG_TAG_NOTES_VIEW_MODEL)
            .i("event=delete_cancelled pendingNoteId=${noteToDelete?.id}")
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmation = false,
            noteToDelete = null,
        )
    }
}
