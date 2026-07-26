package dev.harrisonsoftware.stitchCounter.feature.rowandrepeat

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.harrisonsoftware.stitchCounter.Constants
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.data.repo.AppPreferencesRepository
import dev.harrisonsoftware.stitchCounter.domain.model.DismissalResult
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.domain.usecase.GetProject
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpdateRowAndRepeatValues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class RowAndRepeatUiState(
    val id: Int = 0,
    val title: String = "",
    val rowCount: Int = ROW_COUNT_MIN,
    val rowsPerRepeat: Int = DEFAULT_ROWS_PER_REPEAT,
    val repeatCount: Int = 0,
    val repeatGoal: Int = DEFAULT_REPEAT_GOAL,
    val forceCounterScreensOn: Boolean = false,
    val counterHapticFeedbackEnabled: Boolean = true,
    @StringRes val loadError: Int? = null,
) {
    val repeatProgress: Float? = if (repeatGoal > 0) {
        (repeatCount.toFloat() / repeatGoal.toFloat()).coerceIn(0f, 1f)
    } else {
        null
    }

    companion object {
        const val DEFAULT_ROWS_PER_REPEAT = 8
        const val DEFAULT_REPEAT_GOAL = 20
        const val ROW_COUNT_MIN = 1
        const val REPEAT_COUNT_MIN = 0
    }
}

@HiltViewModel
open class RowAndRepeatViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getProject: GetProject,
    private val updateRowAndRepeatValues: UpdateRowAndRepeatValues,
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    companion object {
        private const val SAVED_STATE_KEY_PROJECT_ID = "row_repeat_project_id"
        private const val SAVED_STATE_KEY_ROW_COUNT = "row_repeat_row_count"
        private const val SAVED_STATE_KEY_REPEAT_COUNT = "row_repeat_repeat_count"
        private const val SAVED_STATE_KEY_ROWS_PER_REPEAT = "row_repeat_rows_per_repeat"
        private const val SAVED_STATE_KEY_REPEAT_GOAL = "row_repeat_repeat_goal"
    }

    private val _uiState = MutableStateFlow(RowAndRepeatUiState())
    open val uiState: StateFlow<RowAndRepeatUiState> = _uiState.asStateFlow()

    private val _dismissalResult = Channel<DismissalResult>(Channel.BUFFERED)
    val dismissalResult = _dismissalResult.receiveAsFlow()

    private var persistJob: Job? = null

    init {
        observeForceCounterScreensOn()
        observeCounterHapticFeedbackEnabled()
    }

    private fun observeForceCounterScreensOn() {
        viewModelScope.launch {
            appPreferencesRepository.forceCounterScreensOn.collect { forceCounterScreensOn ->
                _uiState.update { currentState ->
                    currentState.copy(forceCounterScreensOn = forceCounterScreensOn)
                }
            }
        }
    }

    private fun observeCounterHapticFeedbackEnabled() {
        viewModelScope.launch {
            appPreferencesRepository.counterHapticFeedbackEnabled.collect { counterHapticFeedbackEnabled ->
                _uiState.update { currentState ->
                    currentState.copy(counterHapticFeedbackEnabled = counterHapticFeedbackEnabled)
                }
            }
        }
    }

    fun loadProject(projectId: Int?) {
        viewModelScope.launch {
            if (projectId == null || projectId == 0) {
                Timber.tag(Constants.LOG_TAG_ROW_AND_REPEAT_VIEW_MODEL)
                    .i("event=project_load_reset projectId=${projectId ?: 0}")
                resetState()
                return@launch
            }
            if (_uiState.value.id != projectId) {
                _uiState.update { currentState ->
                    RowAndRepeatUiState(
                        forceCounterScreensOn = currentState.forceCounterScreensOn,
                        counterHapticFeedbackEnabled = currentState.counterHapticFeedbackEnabled,
                    )
                }
            }
            val project = getProject(projectId)
            if (project != null) {
                applyProjectToState(project, preserveCounters = _uiState.value.id == project.id && project.id > 0)
            } else {
                Timber.tag(Constants.LOG_TAG_ROW_AND_REPEAT_VIEW_MODEL)
                    .w("event=project_load_missing projectId=$projectId")
                _uiState.update { currentState ->
                    currentState.copy(loadError = R.string.error_project_not_found)
                }
            }
        }
    }

    fun incrementRow() {
        updateState(operationName = "increment_row", clearCompletedAt = false) { state ->
            var rowCount = state.rowCount + 1
            var repeatCount = state.repeatCount
            if (rowCount > state.rowsPerRepeat) {
                rowCount = RowAndRepeatUiState.ROW_COUNT_MIN
                if (repeatCount < state.repeatGoal) {
                    repeatCount++
                }
            }
            state.copy(rowCount = rowCount, repeatCount = repeatCount)
        }
    }

    fun decrementRow() {
        updateState(operationName = "decrement_row") { state ->
            state.copy(rowCount = (state.rowCount - 1).coerceAtLeast(RowAndRepeatUiState.ROW_COUNT_MIN))
        }
    }

    fun resetRow() {
        updateState(operationName = "reset_row", clearCompletedAt = true) { state ->
            state.copy(rowCount = RowAndRepeatUiState.ROW_COUNT_MIN)
        }
    }

    fun incrementRepeat() {
        updateState(operationName = "increment_repeat") { state ->
            state.copy(repeatCount = (state.repeatCount + 1).coerceAtMost(state.repeatGoal))
        }
    }

    fun decrementRepeat() {
        updateState(operationName = "decrement_repeat") { state ->
            state.copy(repeatCount = (state.repeatCount - 1).coerceAtLeast(RowAndRepeatUiState.REPEAT_COUNT_MIN))
        }
    }

    fun resetAll() {
        updateState(operationName = "reset_all", clearCompletedAt = true) { state ->
            state.copy(
                rowCount = RowAndRepeatUiState.ROW_COUNT_MIN,
                repeatCount = 0
            )
        }
    }

    suspend fun ensureSaved() {
        persistJob?.cancel()
        saveToRoom(operationName = "ensure_saved")
    }

    fun attemptDismissal() {
        viewModelScope.launch {
            persistJob?.cancel()
            saveToRoom(operationName = "attempt_dismissal")
            _dismissalResult.send(DismissalResult.Allowed)
        }
    }

    fun resetState() {
        _uiState.update { currentState ->
            RowAndRepeatUiState(
                forceCounterScreensOn = currentState.forceCounterScreensOn,
                counterHapticFeedbackEnabled = currentState.counterHapticFeedbackEnabled,
            )
        }
        clearSavedState()
    }

    override fun onCleared() {
        super.onCleared()
        if (_uiState.value.id > 0) {
            CoroutineScope(Dispatchers.IO + NonCancellable).launch {
                saveToRoom(operationName = "on_cleared")
            }
        }
    }

    private fun updateState(
        operationName: String,
        clearCompletedAt: Boolean = false,
        update: (RowAndRepeatUiState) -> RowAndRepeatUiState
    ) {
        _uiState.update(update)
        persistToSavedState()
        persistToRoom(clearCompletedAt = clearCompletedAt, operationName = operationName)
    }

    private fun applyProjectToState(project: Project, preserveCounters: Boolean) {
        val currentState = _uiState.value
        val savedProjectId = savedStateHandle.get<Int>(SAVED_STATE_KEY_PROJECT_ID)
        val restoreFromSavedState = !preserveCounters && savedProjectId == project.id

        val restoredRowCount = when {
            preserveCounters -> currentState.rowCount
            restoreFromSavedState -> savedStateHandle.get<Int>(SAVED_STATE_KEY_ROW_COUNT)
                ?: project.rowCounterNumber.coerceAtLeast(RowAndRepeatUiState.ROW_COUNT_MIN)
            else -> project.rowCounterNumber.coerceAtLeast(RowAndRepeatUiState.ROW_COUNT_MIN)
        }
        val restoredRepeatCount = when {
            preserveCounters -> currentState.repeatCount
            restoreFromSavedState -> savedStateHandle.get<Int>(SAVED_STATE_KEY_REPEAT_COUNT)
                ?: project.stitchCounterNumber
            else -> project.stitchCounterNumber
        }
        val restoredRowsPerRepeat = rowsPerRepeatFromProject(project)
        val restoredRepeatGoal = repeatGoalFromProject(project)

        _uiState.update { current ->
            RowAndRepeatUiState(
                id = project.id,
                title = project.title,
                rowCount = restoredRowCount,
                rowsPerRepeat = restoredRowsPerRepeat,
                repeatCount = restoredRepeatCount,
                repeatGoal = restoredRepeatGoal,
                forceCounterScreensOn = current.forceCounterScreensOn,
                counterHapticFeedbackEnabled = current.counterHapticFeedbackEnabled,
            )
        }
        persistToSavedState()

        if (restoreFromSavedState) {
            Timber.tag(Constants.LOG_TAG_ROW_AND_REPEAT_VIEW_MODEL)
                .i("event=project_restore_saved_state projectId=${project.id}")
            persistToRoom(operationName = "restore_saved_state")
        }
    }

    private fun rowsPerRepeatFromProject(project: Project): Int =
        RowAndRepeatProjectValues.rowsPerRepeatFromStoredValue(project.rowAdjustment)

    private fun repeatGoalFromProject(project: Project): Int =
        RowAndRepeatProjectValues.repeatGoalFromStoredValue(project.totalRows)

    private fun persistToRoom(clearCompletedAt: Boolean = false, operationName: String = "state_change") {
        persistJob?.cancel()
        if (_uiState.value.id > 0) {
            persistJob = viewModelScope.launch {
                saveToRoom(clearCompletedAt = clearCompletedAt, operationName = operationName)
            }
        }
    }

    private suspend fun saveToRoom(clearCompletedAt: Boolean = false, operationName: String = "state_change") {
        val state = _uiState.value
        if (state.id > 0) {
            runCatching {
                updateRowAndRepeatValues(
                    id = state.id,
                    repeatCount = state.repeatCount,
                    rowCount = state.rowCount,
                    rowsPerRepeat = state.rowsPerRepeat,
                    repeatGoal = state.repeatGoal,
                    clearCompletedAt = clearCompletedAt,
                    updatedAt = System.currentTimeMillis()
                )
            }.onFailure { throwable ->
                Timber.tag(Constants.LOG_TAG_ROW_AND_REPEAT_VIEW_MODEL)
                    .e(throwable, "event=counter_persist_failed operation=$operationName projectId=${state.id}")
            }
        }
    }

    private fun persistToSavedState() {
        val state = _uiState.value
        savedStateHandle[SAVED_STATE_KEY_PROJECT_ID] = state.id
        savedStateHandle[SAVED_STATE_KEY_ROW_COUNT] = state.rowCount
        savedStateHandle[SAVED_STATE_KEY_REPEAT_COUNT] = state.repeatCount
        savedStateHandle[SAVED_STATE_KEY_ROWS_PER_REPEAT] = state.rowsPerRepeat
        savedStateHandle[SAVED_STATE_KEY_REPEAT_GOAL] = state.repeatGoal
    }

    private fun clearSavedState() {
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_PROJECT_ID)
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_ROW_COUNT)
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_REPEAT_COUNT)
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_ROWS_PER_REPEAT)
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_REPEAT_GOAL)
    }
}
