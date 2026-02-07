package dev.harrisonsoftware.stitchCounter.feature.singleCounter

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
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
)

@HiltViewModel
open class SingleCounterViewModel @Inject constructor(
    private val getProject: GetProject,
    private val upsertProject: UpsertProject,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SingleCounterUiState())
    open val uiState: StateFlow<SingleCounterUiState> = _uiState.asStateFlow()
    
    private val _dismissalResult = Channel<DismissalResult>(Channel.BUFFERED)
    val dismissalResult = _dismissalResult.receiveAsFlow()
    
    private var autoSaveJob: Job? = null
    private val autoSaveDelayMs = 1000L // 1 second debounce

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
                _uiState.update { currentState ->
                    currentState.copy(
                        id = project.id,
                        title = project.title,
                        counterState = if (preserveCounter) {
                            currentState.counterState
                        } else {
                            CounterState(
                                count = project.stitchCounterNumber,
                                adjustment = AdjustmentAmount.entries.find { it.adjustmentAmount == project.stitchAdjustment } ?: AdjustmentAmount.ONE
                            )
                        }
                    )
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
        triggerAutoSave()
    }

    fun increment() {
        _uiState.update { currentState ->
            currentState.copy(
                counterState = currentState.counterState.increment()
            )
        }
        triggerAutoSave()
    }

    fun decrement() {
        _uiState.update { currentState ->
            currentState.copy(
                counterState = currentState.counterState.decrement()
            )
        }
        triggerAutoSave()
    }

    fun resetCount() {
        _uiState.update { currentState ->
            currentState.copy(
                counterState = currentState.counterState.reset()
            )
        }
        triggerAutoSave()
    }
    
    private fun triggerAutoSave() {
        autoSaveJob?.cancel()
        val state = _uiState.value
        if (state.id > 0) {
            autoSaveJob = viewModelScope.launch {
                delay(autoSaveDelayMs)
                save()
            }
        }
    }

    fun resetState() {
        _uiState.update { _ -> SingleCounterUiState() }
    }

    private fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            val existingProject = if (state.id > 0) getProject(state.id) else null
            val project = Project(
                id = state.id,
                type = ProjectType.SINGLE,
                title = existingProject?.title ?: "",
                stitchCounterNumber = state.counterState.count,
                stitchAdjustment = state.counterState.adjustment.adjustmentAmount,
                imagePaths = existingProject?.imagePaths ?: emptyList()
            )
            val newId = upsertProject(project).toInt()
            if (state.id == 0 && newId > 0) {
                _uiState.update { currentState -> currentState.copy(id = newId) }
            }
        }
    }
    
    fun attemptDismissal() {
        viewModelScope.launch {
            autoSaveJob?.cancel()
            save()
            _dismissalResult.send(DismissalResult.Allowed)
        }
    }

}

