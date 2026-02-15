package dev.harrisonsoftware.stitchCounter.feature.singleCounter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.harrisonsoftware.stitchCounter.domain.model.AdjustmentAmount
import dev.harrisonsoftware.stitchCounter.domain.model.CounterState
import dev.harrisonsoftware.stitchCounter.domain.model.DismissalResult
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.domain.usecase.GetProject
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpsertProject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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

data class SingleCounterUiState(
    val id: Int = 0,
    val title: String = "",
    val counterState: CounterState = CounterState(),
    val totalStitchesEver: Int = 0,
)

@HiltViewModel
open class SingleCounterViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getProject: GetProject,
    private val upsertProject: UpsertProject,
) : ViewModel() {

    companion object {
        private const val SAVED_STATE_KEY_PROJECT_ID = "single_project_id"
        private const val SAVED_STATE_KEY_COUNTER_COUNT = "single_counter_count"
        private const val SAVED_STATE_KEY_COUNTER_ADJUSTMENT = "single_counter_adjustment"
        private const val SAVED_STATE_KEY_TOTAL_STITCHES_EVER = "single_total_stitches_ever"
    }

    private val _uiState = MutableStateFlow(SingleCounterUiState())
    open val uiState: StateFlow<SingleCounterUiState> = _uiState.asStateFlow()

    private val _dismissalResult = Channel<DismissalResult>(Channel.BUFFERED)
    val dismissalResult = _dismissalResult.receiveAsFlow()

    private var persistJob: Job? = null

    fun loadProject(projectId: Int?) {
        viewModelScope.launch {
            if (projectId == null || projectId == 0) {
                resetState()
                return@launch
            }
            val project = getProject(projectId)
            if (project != null) {
                val currentState = _uiState.value
                val preserveCounter = currentState.id == project.id && currentState.id > 0
                val savedProjectId = savedStateHandle.get<Int>(SAVED_STATE_KEY_PROJECT_ID)
                val restoreFromSavedState = !preserveCounter && savedProjectId == project.id

                val restoredCount = when {
                    preserveCounter -> currentState.counterState.count
                    restoreFromSavedState -> savedStateHandle.get<Int>(SAVED_STATE_KEY_COUNTER_COUNT) ?: project.stitchCounterNumber
                    else -> project.stitchCounterNumber
                }
                val restoredAdjustment = when {
                    preserveCounter -> currentState.counterState.adjustment
                    restoreFromSavedState -> {
                        val savedAdjustment = savedStateHandle.get<Int>(SAVED_STATE_KEY_COUNTER_ADJUSTMENT)
                        AdjustmentAmount.entries.find { it.adjustmentAmount == savedAdjustment } ?: AdjustmentAmount.ONE
                    }
                    else -> AdjustmentAmount.entries.find { it.adjustmentAmount == project.stitchAdjustment } ?: AdjustmentAmount.ONE
                }
                val restoredTotalStitchesEver = when {
                    preserveCounter -> currentState.totalStitchesEver
                    restoreFromSavedState -> savedStateHandle.get<Int>(SAVED_STATE_KEY_TOTAL_STITCHES_EVER) ?: project.totalStitchesEver
                    else -> project.totalStitchesEver
                }

                _uiState.update {
                    SingleCounterUiState(
                        id = project.id,
                        title = project.title,
                        counterState = CounterState(
                            count = restoredCount,
                            adjustment = restoredAdjustment
                        ),
                        totalStitchesEver = restoredTotalStitchesEver
                    )
                }
                persistToSavedState()

                if (restoreFromSavedState) {
                    persistToRoom()
                }
            }
        }
    }

    fun changeAdjustment(value: AdjustmentAmount) {
        _uiState.update { currentState ->
            currentState.copy(
                counterState = currentState.counterState.copy(adjustment = value)
            )
        }
        persistToSavedState()
        persistToRoom()
    }

    fun increment() {
        _uiState.update { currentState ->
            currentState.copy(
                counterState = currentState.counterState.increment(),
                totalStitchesEver = currentState.totalStitchesEver + currentState.counterState.adjustment.adjustmentAmount
            )
        }
        persistToSavedState()
        persistToRoom()
    }

    fun decrement() {
        _uiState.update { currentState ->
            currentState.copy(
                counterState = currentState.counterState.decrement()
            )
        }
        persistToSavedState()
        persistToRoom()
    }

    fun resetCount() {
        _uiState.update { currentState ->
            currentState.copy(
                counterState = currentState.counterState.reset()
            )
        }
        persistToSavedState()
        persistToRoom()
    }

    fun resetState() {
        _uiState.update { _ -> SingleCounterUiState() }
        clearSavedState()
    }

    fun attemptDismissal() {
        viewModelScope.launch {
            persistJob?.cancel()
            saveToRoom()
            _dismissalResult.send(DismissalResult.Allowed)
        }
    }

    override fun onCleared() {
        super.onCleared()
        val state = _uiState.value
        if (state.id > 0) {
            CoroutineScope(Dispatchers.IO + NonCancellable).launch {
                saveToRoom()
            }
        }
    }

    private fun persistToRoom() {
        persistJob?.cancel()
        val state = _uiState.value
        if (state.id > 0) {
            persistJob = viewModelScope.launch { saveToRoom() }
        }
    }

    private suspend fun saveToRoom() {
        val state = _uiState.value
        val existingProject = if (state.id > 0) getProject(state.id) else null
        val project = Project(
            id = state.id,
            type = ProjectType.SINGLE,
            title = existingProject?.title ?: "",
            stitchCounterNumber = state.counterState.count,
            stitchAdjustment = state.counterState.adjustment.adjustmentAmount,
            imagePaths = existingProject?.imagePaths ?: emptyList(),
            createdAt = existingProject?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            completedAt = existingProject?.completedAt,
            totalStitchesEver = state.totalStitchesEver,
        )
        val newId = upsertProject(project).toInt()
        if (state.id == 0 && newId > 0) {
            _uiState.update { currentState -> currentState.copy(id = newId) }
            persistToSavedState()
        }
    }

    private fun persistToSavedState() {
        val state = _uiState.value
        savedStateHandle[SAVED_STATE_KEY_PROJECT_ID] = state.id
        savedStateHandle[SAVED_STATE_KEY_COUNTER_COUNT] = state.counterState.count
        savedStateHandle[SAVED_STATE_KEY_COUNTER_ADJUSTMENT] = state.counterState.adjustment.adjustmentAmount
        savedStateHandle[SAVED_STATE_KEY_TOTAL_STITCHES_EVER] = state.totalStitchesEver
    }

    private fun clearSavedState() {
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_PROJECT_ID)
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_COUNTER_COUNT)
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_COUNTER_ADJUSTMENT)
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_TOTAL_STITCHES_EVER)
    }
}
