package dev.harrisonsoftware.stitchCounter.feature.singleCounter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.harrisonsoftware.stitchCounter.data.repo.CounterPreferencesRepository
import dev.harrisonsoftware.stitchCounter.domain.model.AdjustmentAmount
import dev.harrisonsoftware.stitchCounter.domain.model.CounterState
import dev.harrisonsoftware.stitchCounter.domain.model.DismissalResult
import dev.harrisonsoftware.stitchCounter.domain.usecase.GetProject
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpdateSingleCounterValues
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
    val shouldShowCustomAdjustmentTip: Boolean = false,
)

@HiltViewModel
open class SingleCounterViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getProject: GetProject,
    private val updateSingleCounterValues: UpdateSingleCounterValues,
    private val counterPreferencesRepository: CounterPreferencesRepository,
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

    init {
        viewModelScope.launch {
            if (counterPreferencesRepository.consumeShouldShowCustomAdjustmentTip()) {
                showCustomAdjustmentTip()
            }
        }
    }

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
                        AdjustmentAmount.fromPersistedAmount(
                            amount = savedAdjustment ?: AdjustmentAmount.ONE.defaultAmount,
                            previousCustomAdjustmentAmount = currentState.counterState.customAdjustmentAmount
                        ).first
                    }
                    else -> AdjustmentAmount.fromPersistedAmount(
                        amount = project.stitchAdjustment,
                        previousCustomAdjustmentAmount = currentState.counterState.customAdjustmentAmount
                    ).first
                }
                val restoredCustomAdjustmentAmount = when {
                    preserveCounter -> currentState.counterState.customAdjustmentAmount
                    restoreFromSavedState -> {
                        val savedAdjustment = savedStateHandle.get<Int>(SAVED_STATE_KEY_COUNTER_ADJUSTMENT)
                            ?: AdjustmentAmount.ONE.defaultAmount
                        AdjustmentAmount.fromPersistedAmount(
                            amount = savedAdjustment,
                            previousCustomAdjustmentAmount = currentState.counterState.customAdjustmentAmount
                        ).second
                    }
                    else -> AdjustmentAmount.fromPersistedAmount(
                        amount = project.stitchAdjustment,
                        previousCustomAdjustmentAmount = currentState.counterState.customAdjustmentAmount
                    ).second
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
                            adjustment = restoredAdjustment,
                            customAdjustmentAmount = restoredCustomAdjustmentAmount
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
                totalStitchesEver = currentState.totalStitchesEver + currentState.counterState.resolvedAdjustmentAmount
            )
        }
        persistToSavedState()
        persistToRoom()
    }

    fun setCustomAdjustmentAmount(value: Int) {
        val normalizedAmount = value.coerceAtLeast(1)
        _uiState.update { currentState ->
            currentState.copy(
                counterState = currentState.counterState.copy(
                    adjustment = AdjustmentAmount.CUSTOM,
                    customAdjustmentAmount = normalizedAmount
                )
            )
        }
        persistToSavedState()
        persistToRoom()
    }

    fun onCustomAdjustmentTipShown() {
        _uiState.update { it.copy(shouldShowCustomAdjustmentTip = false) }
    }

    fun showCustomAdjustmentTip() {
        _uiState.update { it.copy(shouldShowCustomAdjustmentTip = true) }
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
        if (state.id > 0) {
            updateSingleCounterValues(
                id = state.id,
                stitchCount = state.counterState.count,
                stitchAdjustment = state.counterState.resolvedAdjustmentAmount,
                totalStitchesEver = state.totalStitchesEver,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    private fun persistToSavedState() {
        val state = _uiState.value
        savedStateHandle[SAVED_STATE_KEY_PROJECT_ID] = state.id
        savedStateHandle[SAVED_STATE_KEY_COUNTER_COUNT] = state.counterState.count
        savedStateHandle[SAVED_STATE_KEY_COUNTER_ADJUSTMENT] = state.counterState.resolvedAdjustmentAmount
        savedStateHandle[SAVED_STATE_KEY_TOTAL_STITCHES_EVER] = state.totalStitchesEver
    }

    private fun clearSavedState() {
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_PROJECT_ID)
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_COUNTER_COUNT)
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_COUNTER_ADJUSTMENT)
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_TOTAL_STITCHES_EVER)
    }
}
