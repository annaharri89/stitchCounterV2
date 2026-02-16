package dev.harrisonsoftware.stitchCounter.feature.projectDetail

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.harrisonsoftware.stitchCounter.R
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProjectDetailUiState(
    val project: Project? = null,
    val title: String = "",
    val projectType: ProjectType = ProjectType.SINGLE,
    val totalRows: String = "",
    val imagePaths: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    @StringRes val titleError: Int? = null,
    @StringRes val totalRowsError: Int? = null
)

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getProject: GetProject,
    private val upsertProject: UpsertProject,
) : ViewModel() {

    companion object {
        private const val SAVED_STATE_KEY_PROJECT_ID = "detail_project_id"
        private const val SAVED_STATE_KEY_TITLE = "detail_title"
        private const val SAVED_STATE_KEY_PROJECT_TYPE = "detail_project_type"
        private const val SAVED_STATE_KEY_TOTAL_ROWS = "detail_total_rows"
        private const val SAVED_STATE_KEY_IS_COMPLETED = "detail_is_completed"
        private const val AUTO_SAVE_DELAY_MS = 1000L
    }

    private val _uiState = MutableStateFlow(ProjectDetailUiState())
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    private val _dismissalResult = Channel<DismissalResult>(Channel.BUFFERED)
    val dismissalResult = _dismissalResult.receiveAsFlow()

    private var autoSaveJob: Job? = null
    private var originalTitle: String = ""
    private var originalTotalRows: String = ""
    private var originalImagePaths: List<String> = emptyList()
    private var originalIsCompleted: Boolean = false

    fun loadProject(projectId: Int?, projectType: ProjectType) {
        viewModelScope.launch {
            _uiState.update { currentState -> currentState.copy(isLoading = true) }

            if (projectId == null || projectId == 0) {
                val restoredTitle = savedStateHandle.get<String>(SAVED_STATE_KEY_TITLE)
                val restoredTotalRows = savedStateHandle.get<String>(SAVED_STATE_KEY_TOTAL_ROWS)
                val restoredIsCompleted = savedStateHandle.get<Boolean>(SAVED_STATE_KEY_IS_COMPLETED)
                val hasSavedNewProjectState = restoredTitle != null

                val newProject = Project(
                    id = 0,
                    type = projectType,
                    title = "",
                    stitchCounterNumber = 0,
                    stitchAdjustment = 1,
                    rowCounterNumber = 0,
                    rowAdjustment = 1,
                    totalRows = 0
                )
                _uiState.update { currentState ->
                    currentState.copy(
                        project = newProject,
                        title = if (hasSavedNewProjectState) restoredTitle ?: "" else "",
                        projectType = projectType,
                        totalRows = if (hasSavedNewProjectState) restoredTotalRows ?: "" else "",
                        imagePaths = emptyList(),
                        isCompleted = if (hasSavedNewProjectState) restoredIsCompleted ?: false else false,
                        isLoading = false,
                        hasUnsavedChanges = hasSavedNewProjectState,
                        titleError = null,
                        totalRowsError = null
                    )
                }
                originalTitle = ""
                originalTotalRows = ""
                originalImagePaths = emptyList()
                originalIsCompleted = false
                return@launch
            }

            val project = getProject(projectId)
            if (project != null) {
                val savedProjectId = savedStateHandle.get<Int>(SAVED_STATE_KEY_PROJECT_ID)
                val restoreFromSavedState = savedProjectId == project.id

                val restoredTitle = if (restoreFromSavedState) {
                    savedStateHandle.get<String>(SAVED_STATE_KEY_TITLE) ?: project.title
                } else {
                    project.title
                }
                val restoredTotalRows = if (restoreFromSavedState) {
                    savedStateHandle.get<String>(SAVED_STATE_KEY_TOTAL_ROWS)
                        ?: if (project.totalRows > 0) project.totalRows.toString() else ""
                } else {
                    if (project.totalRows > 0) project.totalRows.toString() else ""
                }
                val restoredIsCompleted = if (restoreFromSavedState) {
                    savedStateHandle.get<Boolean>(SAVED_STATE_KEY_IS_COMPLETED) ?: (project.completedAt != null)
                } else {
                    project.completedAt != null
                }

                originalTitle = project.title
                originalTotalRows = if (project.totalRows > 0) project.totalRows.toString() else ""
                originalImagePaths = project.imagePaths
                originalIsCompleted = project.completedAt != null

                val hasChangesFromSavedState = restoreFromSavedState && (
                    restoredTitle != originalTitle
                        || restoredTotalRows != originalTotalRows
                        || restoredIsCompleted != originalIsCompleted
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        project = project,
                        title = restoredTitle,
                        projectType = project.type,
                        totalRows = restoredTotalRows,
                        imagePaths = project.imagePaths,
                        isCompleted = restoredIsCompleted,
                        isLoading = false,
                        hasUnsavedChanges = hasChangesFromSavedState,
                        titleError = null,
                        totalRowsError = null
                    )
                }

                if (hasChangesFromSavedState) {
                    triggerAutoSave()
                }
            } else {
                _uiState.update { currentState -> currentState.copy(isLoading = false) }
            }
        }
    }

    fun loadProjectById(projectId: Int) {
        viewModelScope.launch {
            _uiState.update { currentState -> currentState.copy(isLoading = true) }
            val project = getProject(projectId)
            if (project != null) {
                val savedProjectId = savedStateHandle.get<Int>(SAVED_STATE_KEY_PROJECT_ID)
                val restoreFromSavedState = savedProjectId == project.id

                val restoredTitle = if (restoreFromSavedState) {
                    savedStateHandle.get<String>(SAVED_STATE_KEY_TITLE) ?: project.title
                } else {
                    project.title
                }
                val restoredTotalRows = if (restoreFromSavedState) {
                    savedStateHandle.get<String>(SAVED_STATE_KEY_TOTAL_ROWS)
                        ?: if (project.totalRows > 0) project.totalRows.toString() else ""
                } else {
                    if (project.totalRows > 0) project.totalRows.toString() else ""
                }
                val restoredIsCompleted = if (restoreFromSavedState) {
                    savedStateHandle.get<Boolean>(SAVED_STATE_KEY_IS_COMPLETED) ?: (project.completedAt != null)
                } else {
                    project.completedAt != null
                }

                originalTitle = project.title
                originalTotalRows = if (project.totalRows > 0) project.totalRows.toString() else ""
                originalImagePaths = project.imagePaths
                originalIsCompleted = project.completedAt != null

                val hasChangesFromSavedState = restoreFromSavedState && (
                    restoredTitle != originalTitle
                        || restoredTotalRows != originalTotalRows
                        || restoredIsCompleted != originalIsCompleted
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        project = project,
                        title = restoredTitle,
                        projectType = project.type,
                        totalRows = restoredTotalRows,
                        imagePaths = project.imagePaths,
                        isCompleted = restoredIsCompleted,
                        isLoading = false,
                        hasUnsavedChanges = hasChangesFromSavedState,
                        titleError = null,
                        totalRowsError = null
                    )
                }

                if (hasChangesFromSavedState) {
                    triggerAutoSave()
                }
            } else {
                _uiState.update { currentState -> currentState.copy(isLoading = false) }
            }
        }
    }

    private fun hasChanges(
        title: String = _uiState.value.title,
        totalRows: String = _uiState.value.totalRows,
        imagePaths: List<String> = _uiState.value.imagePaths,
        isCompleted: Boolean = _uiState.value.isCompleted
    ): Boolean =
        title != originalTitle
                || totalRows != originalTotalRows
                || imagePaths != originalImagePaths
                || isCompleted != originalIsCompleted

    fun updateTitle(newTitle: String) {
        _uiState.update { currentState ->
            currentState.copy(
                title = newTitle,
                hasUnsavedChanges = hasChanges(title = newTitle),
                titleError = if (newTitle.isBlank()) R.string.error_title_required else null
            )
        }
        persistToSavedState()
        triggerAutoSave()
    }

    fun updateTotalRows(newTotalRows: String) {
        val state = _uiState.value
        val totalRowsValue = newTotalRows.toIntOrNull() ?: 0
        val isDoubleCounter = state.projectType == ProjectType.DOUBLE
        val totalRowsError = if (isDoubleCounter && totalRowsValue <= 0 && newTotalRows.isNotBlank()) {
            R.string.error_total_rows_greater_than_zero
        } else if (isDoubleCounter && newTotalRows.isBlank()) {
            R.string.error_total_rows_required
        } else {
            null
        }
        _uiState.update { currentState ->
            currentState.copy(
                totalRows = newTotalRows,
                hasUnsavedChanges = hasChanges(totalRows = newTotalRows),
                totalRowsError = totalRowsError
            )
        }
        persistToSavedState()
        triggerAutoSave()
    }

    fun toggleCompleted(isCompleted: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isCompleted = isCompleted,
                hasUnsavedChanges = hasChanges(isCompleted = isCompleted)
            )
        }
        persistToSavedState()
        triggerAutoSave()
    }

    private fun triggerAutoSave() {
        autoSaveJob?.cancel()
        val state = _uiState.value
        val isExistingProject = state.project?.id != null && state.project.id > 0
        if (state.hasUnsavedChanges && isExistingProject) {
            autoSaveJob = viewModelScope.launch {
                delay(AUTO_SAVE_DELAY_MS)
                saveToRoom()
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            autoSaveJob?.cancel()
            saveToRoom()
        }
    }

    private suspend fun saveToRoom() {
        val state = _uiState.value
        val staleProject = state.project
        val projectId = staleProject?.id ?: 0
        val totalRowsValue = state.totalRows.toIntOrNull() ?: 0

        val freshProject = if (projectId > 0) getProject(projectId) else null
        val baseProject = freshProject ?: staleProject

        val project = Project(
            id = projectId,
            type = state.projectType,
            title = state.title,
            stitchCounterNumber = baseProject?.stitchCounterNumber ?: 0,
            stitchAdjustment = baseProject?.stitchAdjustment ?: 1,
            rowCounterNumber = baseProject?.rowCounterNumber ?: 0,
            rowAdjustment = baseProject?.rowAdjustment ?: 1,
            totalRows = totalRowsValue,
            imagePaths = state.imagePaths,
            createdAt = baseProject?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            completedAt = if (state.isCompleted) (baseProject?.completedAt ?: System.currentTimeMillis()) else null,
            totalStitchesEver = baseProject?.totalStitchesEver ?: 0,
        )
        val newId = upsertProject(project).toInt()
        if (projectId == 0 && newId > 0) {
            val updatedProject = project.copy(id = newId)
            originalTitle = state.title
            originalTotalRows = state.totalRows
            originalImagePaths = updatedProject.imagePaths
            originalIsCompleted = state.isCompleted
            _uiState.update { currentState ->
                currentState.copy(
                    project = updatedProject,
                    imagePaths = updatedProject.imagePaths,
                    hasUnsavedChanges = false
                )
            }
            persistToSavedState()
        } else if (projectId > 0) {
            val updatedProject = project.copy(id = projectId)
            originalTitle = state.title
            originalTotalRows = state.totalRows
            originalImagePaths = updatedProject.imagePaths
            originalIsCompleted = state.isCompleted
            _uiState.update { currentState ->
                currentState.copy(
                    project = updatedProject,
                    imagePaths = updatedProject.imagePaths,
                    hasUnsavedChanges = false
                )
            }
        }
    }

    fun attemptDismissal() {
        viewModelScope.launch {
            autoSaveJob?.cancel()
            val state = _uiState.value

            if (state.title.isBlank()) {
                _uiState.update { currentState ->
                    currentState.copy(titleError = R.string.error_title_required)
                }
                _dismissalResult.send(DismissalResult.ShowDiscardDialog)
            } else {
                saveToRoom()
                _dismissalResult.send(DismissalResult.Allowed)
            }
        }
    }

    fun discardChanges() {
        _uiState.update { currentState ->
            currentState.copy(
                title = originalTitle,
                totalRows = originalTotalRows,
                imagePaths = originalImagePaths,
                isCompleted = originalIsCompleted,
                hasUnsavedChanges = false,
                titleError = null,
                totalRowsError = null
            )
        }
        clearSavedState()
    }

    fun addImagePath(imagePath: String) {
        val newImagePaths = _uiState.value.imagePaths + imagePath
        _uiState.update { currentState ->
            currentState.copy(
                imagePaths = newImagePaths,
                hasUnsavedChanges = hasChanges(imagePaths = newImagePaths)
            )
        }
        triggerAutoSave()
    }

    fun removeImagePath(imagePath: String) {
        val newImagePaths = _uiState.value.imagePaths.filter { it != imagePath }
        _uiState.update { currentState ->
            currentState.copy(
                imagePaths = newImagePaths,
                hasUnsavedChanges = hasChanges(imagePaths = newImagePaths)
            )
        }
        triggerAutoSave()
    }

    fun createProject() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.title.isBlank()) {
                _uiState.update { currentState ->
                    currentState.copy(titleError = R.string.error_title_required)
                }
                return@launch
            }

            val isDoubleCounter = state.projectType == ProjectType.DOUBLE
            val totalRowsValue = state.totalRows.toIntOrNull() ?: 0
            if (isDoubleCounter && totalRowsValue <= 0) {
                _uiState.update { currentState ->
                    currentState.copy(totalRowsError = R.string.error_total_rows_required_and_greater)
                }
                return@launch
            }

            val existingProject = state.project
            val now = System.currentTimeMillis()
            val project = Project(
                id = 0,
                type = state.projectType,
                title = state.title,
                stitchCounterNumber = existingProject?.stitchCounterNumber ?: 0,
                stitchAdjustment = existingProject?.stitchAdjustment ?: 1,
                rowCounterNumber = existingProject?.rowCounterNumber ?: 0,
                rowAdjustment = existingProject?.rowAdjustment ?: 1,
                totalRows = totalRowsValue,
                imagePaths = state.imagePaths,
                createdAt = now,
                updatedAt = now,
                completedAt = null,
                totalStitchesEver = 0,
            )
            val newId = upsertProject(project).toInt()
            if (newId > 0) {
                val updatedProject = project.copy(id = newId)
                originalTitle = state.title
                originalTotalRows = state.totalRows
                originalImagePaths = updatedProject.imagePaths
                _uiState.update { currentState ->
                    currentState.copy(
                        project = updatedProject,
                        imagePaths = updatedProject.imagePaths,
                        hasUnsavedChanges = false,
                        titleError = null,
                        totalRowsError = null
                    )
                }
                persistToSavedState()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        val state = _uiState.value
        val projectId = state.project?.id ?: 0
        if (projectId > 0 && state.hasUnsavedChanges) {
            CoroutineScope(Dispatchers.IO + NonCancellable).launch {
                saveToRoom()
            }
        }
    }

    private fun persistToSavedState() {
        val state = _uiState.value
        savedStateHandle[SAVED_STATE_KEY_PROJECT_ID] = state.project?.id ?: 0
        savedStateHandle[SAVED_STATE_KEY_TITLE] = state.title
        savedStateHandle[SAVED_STATE_KEY_PROJECT_TYPE] = state.projectType.name
        savedStateHandle[SAVED_STATE_KEY_TOTAL_ROWS] = state.totalRows
        savedStateHandle[SAVED_STATE_KEY_IS_COMPLETED] = state.isCompleted
    }

    private fun clearSavedState() {
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_PROJECT_ID)
        savedStateHandle.remove<String>(SAVED_STATE_KEY_TITLE)
        savedStateHandle.remove<String>(SAVED_STATE_KEY_PROJECT_TYPE)
        savedStateHandle.remove<String>(SAVED_STATE_KEY_TOTAL_ROWS)
        savedStateHandle.remove<Boolean>(SAVED_STATE_KEY_IS_COMPLETED)
    }
}
