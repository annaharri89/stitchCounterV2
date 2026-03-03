package dev.harrisonsoftware.stitchCounter.feature.doublecounter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.harrisonsoftware.stitchCounter.data.repo.AppPreferencesRepository
import dev.harrisonsoftware.stitchCounter.domain.model.AdjustmentAmount
import dev.harrisonsoftware.stitchCounter.domain.model.CounterState
import dev.harrisonsoftware.stitchCounter.domain.model.DismissalResult
import dev.harrisonsoftware.stitchCounter.domain.usecase.GetProject
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpdateDoubleCounterValues
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

data class DoubleCounterUiState(
    val id: Int = 0,
    val title: String = "",
    val stitchCounterState: CounterState = CounterState(),
    val rowCounterState: CounterState = CounterState(),
    val totalRows: Int = 0,
    val totalStitchesEver: Int = 0,
    val shouldShowCustomAdjustmentTip: Boolean = false,
) {
    val rowProgress: Float? = if (totalRows > 0) {
        (rowCounterState.count.toFloat() / totalRows.toFloat()).coerceIn(0f, 1f)
    } else {
        null
    }
}

enum class CounterType {
    STITCH,
    ROW
}

@HiltViewModel
open class DoubleCounterViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getProject: GetProject,
    private val updateDoubleCounterValues: UpdateDoubleCounterValues,
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    companion object {
        private const val SAVED_STATE_KEY_PROJECT_ID = "double_project_id"
        private const val SAVED_STATE_KEY_STITCH_COUNT = "double_stitch_count"
        private const val SAVED_STATE_KEY_STITCH_ADJUSTMENT = "double_stitch_adjustment"
        private const val SAVED_STATE_KEY_ROW_COUNT = "double_row_count"
        private const val SAVED_STATE_KEY_ROW_ADJUSTMENT = "double_row_adjustment"
        private const val SAVED_STATE_KEY_TOTAL_ROWS = "double_total_rows"
        private const val SAVED_STATE_KEY_TOTAL_STITCHES_EVER = "double_total_stitches_ever"
    }

    private val _uiState = MutableStateFlow(DoubleCounterUiState())
    open val uiState: StateFlow<DoubleCounterUiState> = _uiState.asStateFlow()

    private val _dismissalResult = Channel<DismissalResult>(Channel.BUFFERED)
    val dismissalResult = _dismissalResult.receiveAsFlow()

    private var persistJob: Job? = null

    init {
        viewModelScope.launch {
            if (appPreferencesRepository.consumeShouldShowCustomAdjustmentTip()) {
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
            if (_uiState.value.id != projectId) {
                _uiState.update { DoubleCounterUiState() }
            }
            val project = getProject(projectId)
            if (project != null) {
                val currentState = _uiState.value
                val preserveCounters = currentState.id == project.id && currentState.id > 0
                val savedProjectId = savedStateHandle.get<Int>(SAVED_STATE_KEY_PROJECT_ID)
                val restoreFromSavedState = !preserveCounters && savedProjectId == project.id

                val restoredStitchCount = when {
                    preserveCounters -> currentState.stitchCounterState.count
                    restoreFromSavedState -> savedStateHandle.get<Int>(SAVED_STATE_KEY_STITCH_COUNT) ?: project.stitchCounterNumber
                    else -> project.stitchCounterNumber
                }
                val restoredStitchAdjustment = when {
                    preserveCounters -> currentState.stitchCounterState.adjustment
                    restoreFromSavedState -> {
                        val saved = savedStateHandle.get<Int>(SAVED_STATE_KEY_STITCH_ADJUSTMENT)
                        AdjustmentAmount.fromPersistedAmount(
                            amount = saved ?: AdjustmentAmount.ONE.defaultAmount,
                            previousCustomAdjustmentAmount = currentState.stitchCounterState.customAdjustmentAmount
                        ).first
                    }
                    else -> AdjustmentAmount.fromPersistedAmount(
                        amount = project.stitchAdjustment,
                        previousCustomAdjustmentAmount = AdjustmentAmount.CUSTOM.defaultAmount
                    ).first
                }
                val restoredStitchCustomAdjustment = when {
                    preserveCounters -> currentState.stitchCounterState.customAdjustmentAmount
                    restoreFromSavedState -> {
                        val saved = savedStateHandle.get<Int>(SAVED_STATE_KEY_STITCH_ADJUSTMENT)
                            ?: AdjustmentAmount.ONE.defaultAmount
                        AdjustmentAmount.fromPersistedAmount(
                            amount = saved,
                            previousCustomAdjustmentAmount = currentState.stitchCounterState.customAdjustmentAmount
                        ).second
                    }
                    else -> AdjustmentAmount.fromPersistedAmount(
                        amount = project.stitchAdjustment,
                        previousCustomAdjustmentAmount = AdjustmentAmount.CUSTOM.defaultAmount
                    ).second
                }
                val restoredRowCount = when {
                    preserveCounters -> currentState.rowCounterState.count
                    restoreFromSavedState -> savedStateHandle.get<Int>(SAVED_STATE_KEY_ROW_COUNT) ?: project.rowCounterNumber
                    else -> project.rowCounterNumber
                }
                val restoredRowAdjustment = when {
                    preserveCounters -> currentState.rowCounterState.adjustment
                    restoreFromSavedState -> {
                        val saved = savedStateHandle.get<Int>(SAVED_STATE_KEY_ROW_ADJUSTMENT)
                        AdjustmentAmount.fromPersistedAmount(
                            amount = saved ?: AdjustmentAmount.ONE.defaultAmount,
                            previousCustomAdjustmentAmount = currentState.rowCounterState.customAdjustmentAmount
                        ).first
                    }
                    else -> AdjustmentAmount.fromPersistedAmount(
                        amount = project.rowAdjustment,
                        previousCustomAdjustmentAmount = AdjustmentAmount.CUSTOM.defaultAmount
                    ).first
                }
                val restoredRowCustomAdjustment = when {
                    preserveCounters -> currentState.rowCounterState.customAdjustmentAmount
                    restoreFromSavedState -> {
                        val saved = savedStateHandle.get<Int>(SAVED_STATE_KEY_ROW_ADJUSTMENT)
                            ?: AdjustmentAmount.ONE.defaultAmount
                        AdjustmentAmount.fromPersistedAmount(
                            amount = saved,
                            previousCustomAdjustmentAmount = currentState.rowCounterState.customAdjustmentAmount
                        ).second
                    }
                    else -> AdjustmentAmount.fromPersistedAmount(
                        amount = project.rowAdjustment,
                        previousCustomAdjustmentAmount = AdjustmentAmount.CUSTOM.defaultAmount
                    ).second
                }
                val restoredTotalStitchesEver = when {
                    preserveCounters -> currentState.totalStitchesEver
                    restoreFromSavedState -> savedStateHandle.get<Int>(SAVED_STATE_KEY_TOTAL_STITCHES_EVER) ?: project.totalStitchesEver
                    else -> project.totalStitchesEver
                }

                _uiState.update {
                    DoubleCounterUiState(
                        id = project.id,
                        title = project.title,
                        stitchCounterState = CounterState(
                            count = restoredStitchCount,
                            adjustment = restoredStitchAdjustment,
                            customAdjustmentAmount = restoredStitchCustomAdjustment
                        ),
                        rowCounterState = CounterState(
                            count = restoredRowCount,
                            adjustment = restoredRowAdjustment,
                            customAdjustmentAmount = restoredRowCustomAdjustment
                        ),
                        totalRows = project.totalRows,
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

    private fun updateCounter(type: CounterType, update: (CounterState) -> CounterState) {
        _uiState.update { currentState ->
            when (type) {
                CounterType.STITCH -> currentState.copy(
                    stitchCounterState = update(currentState.stitchCounterState)
                )
                CounterType.ROW -> {
                    val updatedState = update(currentState.rowCounterState)
                    val cappedCount = if (currentState.totalRows > 0) {
                        updatedState.count.coerceAtMost(currentState.totalRows)
                    } else {
                        updatedState.count
                    }
                    currentState.copy(
                        rowCounterState = updatedState.copy(count = cappedCount)
                    )
                }
            }
        }
        persistToSavedState()
        persistToRoom()
    }

    fun increment(type: CounterType) {
        if (type == CounterType.STITCH) {
            val adjustmentAmount = _uiState.value.stitchCounterState.resolvedAdjustmentAmount
            _uiState.update { it.copy(totalStitchesEver = it.totalStitchesEver + adjustmentAmount) }
        }
        updateCounter(type) { it.increment() }
    }

    fun decrement(type: CounterType) = updateCounter(type) { it.decrement() }
    fun reset(type: CounterType) = updateCounter(type) { it.reset() }
    fun changeAdjustment(type: CounterType, value: AdjustmentAmount) =
        updateCounter(type) { it.copy(adjustment = value) }
    fun setCustomAdjustmentAmount(type: CounterType, value: Int) =
        updateCounter(type) {
            it.copy(
                adjustment = AdjustmentAmount.CUSTOM,
                customAdjustmentAmount = value.coerceAtLeast(1)
            )
        }

    fun onCustomAdjustmentTipShown() {
        _uiState.update { it.copy(shouldShowCustomAdjustmentTip = false) }
    }

    fun showCustomAdjustmentTip() {
        _uiState.update { it.copy(shouldShowCustomAdjustmentTip = true) }
    }

    suspend fun ensureSaved() {
        persistJob?.cancel()
        saveToRoom()
    }

    fun attemptDismissal() {
        viewModelScope.launch {
            persistJob?.cancel()
            saveToRoom()
            _dismissalResult.send(DismissalResult.Allowed)
        }
    }

    fun resetState() {
        _uiState.update { _ -> DoubleCounterUiState() }
        clearSavedState()
    }

    fun resetAll() {
        reset(CounterType.STITCH)
        reset(CounterType.ROW)
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
            updateDoubleCounterValues(
                id = state.id,
                stitchCount = state.stitchCounterState.count,
                stitchAdjustment = state.stitchCounterState.resolvedAdjustmentAmount,
                rowCount = state.rowCounterState.count,
                rowAdjustment = state.rowCounterState.resolvedAdjustmentAmount,
                totalStitchesEver = state.totalStitchesEver,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    private fun persistToSavedState() {
        val state = _uiState.value
        savedStateHandle[SAVED_STATE_KEY_PROJECT_ID] = state.id
        savedStateHandle[SAVED_STATE_KEY_STITCH_COUNT] = state.stitchCounterState.count
        savedStateHandle[SAVED_STATE_KEY_STITCH_ADJUSTMENT] = state.stitchCounterState.resolvedAdjustmentAmount
        savedStateHandle[SAVED_STATE_KEY_ROW_COUNT] = state.rowCounterState.count
        savedStateHandle[SAVED_STATE_KEY_ROW_ADJUSTMENT] = state.rowCounterState.resolvedAdjustmentAmount
        savedStateHandle[SAVED_STATE_KEY_TOTAL_ROWS] = state.totalRows
        savedStateHandle[SAVED_STATE_KEY_TOTAL_STITCHES_EVER] = state.totalStitchesEver
    }

    private fun clearSavedState() {
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_PROJECT_ID)
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_STITCH_COUNT)
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_STITCH_ADJUSTMENT)
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_ROW_COUNT)
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_ROW_ADJUSTMENT)
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_TOTAL_ROWS)
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_TOTAL_STITCHES_EVER)
    }
}
