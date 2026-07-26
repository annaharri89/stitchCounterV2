package dev.harrisonsoftware.stitchCounter.feature.notes

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.harrisonsoftware.stitchCounter.Constants
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.domain.model.DismissalResult
import dev.harrisonsoftware.stitchCounter.domain.usecase.CreateNote
import dev.harrisonsoftware.stitchCounter.domain.usecase.CreateNoteResult
import dev.harrisonsoftware.stitchCounter.domain.usecase.GetNote
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpdateNote
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpdateNoteResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class CreateNoteUiState(
    val noteId: Int? = null,
    val title: String = "",
    val body: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    @StringRes val titleError: Int? = null,
    @StringRes val loadError: Int? = null,
) {
    val isEditMode: Boolean get() = noteId != null
}

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val createNote: CreateNote,
    private val getNote: GetNote,
    private val updateNote: UpdateNote,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateNoteUiState())
    val uiState: StateFlow<CreateNoteUiState> = _uiState.asStateFlow()

    private val _dismissalResult = Channel<DismissalResult>(Channel.BUFFERED)
    val dismissalResult = _dismissalResult.receiveAsFlow()

    private var originalTitle: String = ""
    private var originalBody: String = ""

    fun loadNote(noteId: Int?) {
        viewModelScope.launch {
            if (noteId == null) {
                resetToCreateMode()
                return@launch
            }
            _uiState.update { currentState ->
                currentState.copy(isLoading = true, titleError = null, loadError = null)
            }
            val note = getNote(noteId)
            if (note == null) {
                Timber.tag(Constants.LOG_TAG_CREATE_NOTE_VIEW_MODEL)
                    .w("event=note_load_failed noteId=$noteId reason=not_found")
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        loadError = R.string.error_note_not_found,
                    )
                }
                return@launch
            }
            originalTitle = note.title
            originalBody = note.body
            _uiState.value = CreateNoteUiState(
                noteId = note.id,
                title = note.title,
                body = note.body,
            )
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { currentState ->
            currentState.copy(title = title, titleError = null)
        }
    }

    fun updateBody(body: String) {
        _uiState.update { currentState -> currentState.copy(body = body) }
    }

    fun saveNote() {
        val currentState = _uiState.value
        if (currentState.isSaving || currentState.isSaved || currentState.isLoading) {
            return
        }
        _uiState.update { state -> state.copy(isSaving = true, titleError = null) }
        viewModelScope.launch {
            if (currentState.isEditMode) {
                saveEditedNote(currentState)
            } else {
                saveNewNote(currentState)
            }
        }
    }

    fun attemptDismissal() {
        val currentState = _uiState.value
        if (currentState.loadError != null) {
            viewModelScope.launch { _dismissalResult.send(DismissalResult.Allowed) }
            return
        }
        if (currentState.isSaved) {
            viewModelScope.launch { _dismissalResult.send(DismissalResult.Allowed) }
            return
        }
        val hasUnsavedChanges = hasUnsavedChanges(currentState)
        viewModelScope.launch {
            if (hasUnsavedChanges) {
                _dismissalResult.send(DismissalResult.ShowDiscardDialog)
            } else {
                _dismissalResult.send(DismissalResult.Allowed)
            }
        }
    }

    fun confirmDiscard() {
        viewModelScope.launch { _dismissalResult.send(DismissalResult.Allowed) }
    }

    private suspend fun saveNewNote(currentState: CreateNoteUiState) {
        when (val result = createNote(currentState.title, currentState.body, System.currentTimeMillis())) {
            is CreateNoteResult.Success -> {
                Timber.tag(Constants.LOG_TAG_CREATE_NOTE_VIEW_MODEL)
                    .i("event=note_created noteId=${result.noteId}")
                _uiState.update { state ->
                    state.copy(isSaving = false, isSaved = true)
                }
                _dismissalResult.send(DismissalResult.Allowed)
            }
            CreateNoteResult.InvalidTitle -> {
                Timber.tag(Constants.LOG_TAG_CREATE_NOTE_VIEW_MODEL)
                    .i("event=note_create_failed reason=invalid_title")
                _uiState.update { state ->
                    state.copy(isSaving = false, titleError = R.string.error_title_required)
                }
            }
        }
    }

    private suspend fun saveEditedNote(currentState: CreateNoteUiState) {
        val noteId = currentState.noteId ?: return
        when (
            val result = updateNote(
                noteId = noteId,
                title = currentState.title,
                body = currentState.body,
                timestampMillis = System.currentTimeMillis(),
            )
        ) {
            UpdateNoteResult.Success -> {
                Timber.tag(Constants.LOG_TAG_CREATE_NOTE_VIEW_MODEL)
                    .i("event=note_updated noteId=$noteId")
                originalTitle = currentState.title
                originalBody = currentState.body
                _uiState.update { state ->
                    state.copy(isSaving = false, isSaved = true)
                }
                _dismissalResult.send(DismissalResult.Allowed)
            }
            UpdateNoteResult.InvalidTitle -> {
                Timber.tag(Constants.LOG_TAG_CREATE_NOTE_VIEW_MODEL)
                    .i("event=note_update_failed noteId=$noteId reason=invalid_title")
                _uiState.update { state ->
                    state.copy(isSaving = false, titleError = R.string.error_title_required)
                }
            }
            UpdateNoteResult.NotFound -> {
                Timber.tag(Constants.LOG_TAG_CREATE_NOTE_VIEW_MODEL)
                    .w("event=note_update_failed noteId=$noteId reason=not_found")
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        loadError = R.string.error_note_not_found,
                    )
                }
            }
        }
    }

    private fun resetToCreateMode() {
        originalTitle = ""
        originalBody = ""
        _uiState.value = CreateNoteUiState()
    }

    private fun hasUnsavedChanges(currentState: CreateNoteUiState): Boolean {
        if (currentState.isEditMode) {
            return currentState.title != originalTitle || currentState.body != originalBody
        }
        return currentState.title.isNotBlank() || currentState.body.isNotBlank()
    }
}
