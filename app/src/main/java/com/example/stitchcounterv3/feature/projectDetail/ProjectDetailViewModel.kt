package com.example.stitchcounterv3.feature.projectDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stitchcounterv3.domain.model.DismissalResult
import com.example.stitchcounterv3.domain.model.Project
import com.example.stitchcounterv3.domain.model.ProjectType
import com.example.stitchcounterv3.domain.usecase.GetProject
import com.example.stitchcounterv3.domain.usecase.UpsertProject
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

data class ProjectDetailUiState(
    val project: Project? = null,
    val title: String = "",
    val projectType: ProjectType = ProjectType.SINGLE,
    val totalRows: String = "",
    val imagePath: String? = null,
    val isLoading: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val titleError: String? = null,
    val totalRowsError: String? = null
)

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val getProject: GetProject,
    private val upsertProject: UpsertProject,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProjectDetailUiState())
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()
    
    private val _dismissalResult = Channel<DismissalResult>(Channel.BUFFERED)
    val dismissalResult = _dismissalResult.receiveAsFlow()
    
    private var autoSaveJob: Job? = null
    private val autoSaveDelayMs = 1000L
    private var originalTitle: String = ""
    private var originalTotalRows: String = ""
    private var originalImagePath: String? = null

    fun loadProject(projectId: Int?, projectType: ProjectType) {
        viewModelScope.launch {
            _uiState.update { currentState -> currentState.copy(isLoading = true) }
            
            if (projectId == null || projectId == 0) {
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
                        title = "",
                        projectType = projectType,
                        totalRows = "",
                        imagePath = null,
                        isLoading = false,
                        hasUnsavedChanges = false,
                        titleError = null,
                        totalRowsError = null
                    )
                }
                originalTitle = ""
                originalTotalRows = ""
                originalImagePath = null
                return@launch
            }
            
            val project = getProject(projectId)
            if (project != null) {
                originalTitle = project.title
                originalTotalRows = if (project.totalRows > 0) project.totalRows.toString() else ""
                originalImagePath = project.imagePath
                _uiState.update { currentState ->
                    currentState.copy(
                        project = project,
                        title = project.title,
                        projectType = project.type,
                        totalRows = if (project.totalRows > 0) project.totalRows.toString() else "",
                        imagePath = project.imagePath,
                        isLoading = false,
                        hasUnsavedChanges = false,
                        titleError = null,
                        totalRowsError = null
                    )
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
                originalTitle = project.title
                originalTotalRows = if (project.totalRows > 0) project.totalRows.toString() else ""
                originalImagePath = project.imagePath
                _uiState.update { currentState ->
                    currentState.copy(
                        project = project,
                        title = project.title,
                        projectType = project.type,
                        totalRows = if (project.totalRows > 0) project.totalRows.toString() else "",
                        imagePath = project.imagePath,
                        isLoading = false,
                        hasUnsavedChanges = false,
                        titleError = null,
                        totalRowsError = null
                    )
                }
            } else {
                _uiState.update { currentState -> currentState.copy(isLoading = false) }
            }
        }
    }

    fun updateTitle(newTitle: String) {
        val currentImagePath = _uiState.value.imagePath
        val currentTotalRows = _uiState.value.totalRows
        _uiState.update { currentState ->
            currentState.copy(
                title = newTitle,
                hasUnsavedChanges = newTitle != originalTitle || currentTotalRows != originalTotalRows || currentImagePath != originalImagePath,
                titleError = if (newTitle.isBlank()) "Title is required" else null
            )
        }
        triggerAutoSave()
    }

    fun updateTotalRows(newTotalRows: String) {
        val currentTitle = _uiState.value.title
        val currentImagePath = _uiState.value.imagePath
        val state = _uiState.value
        val totalRowsValue = newTotalRows.toIntOrNull() ?: 0
        val isDoubleCounter = state.projectType == ProjectType.DOUBLE
        val totalRowsError = if (isDoubleCounter && totalRowsValue <= 0 && newTotalRows.isNotBlank()) {
            "Total rows must be greater than 0"
        } else if (isDoubleCounter && newTotalRows.isBlank()) {
            "Total rows is required"
        } else {
            null
        }
        _uiState.update { currentState ->
            currentState.copy(
                totalRows = newTotalRows,
                hasUnsavedChanges = currentTitle != originalTitle || newTotalRows != originalTotalRows || currentImagePath != originalImagePath,
                totalRowsError = totalRowsError
            )
        }
        triggerAutoSave()
    }

    private fun triggerAutoSave() {
        autoSaveJob?.cancel()
        val state = _uiState.value
        val isExistingProject = state.project?.id != null && state.project.id > 0
        if (state.hasUnsavedChanges && isExistingProject) {
            autoSaveJob = viewModelScope.launch {
                delay(autoSaveDelayMs)
                save()
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            val existingProject = state.project
            val totalRowsValue = state.totalRows.toIntOrNull() ?: 0
            val project = Project(
                id = existingProject?.id ?: 0,
                type = state.projectType,
                title = state.title,
                stitchCounterNumber = existingProject?.stitchCounterNumber ?: 0,
                stitchAdjustment = existingProject?.stitchAdjustment ?: 1,
                rowCounterNumber = existingProject?.rowCounterNumber ?: 0,
                rowAdjustment = existingProject?.rowAdjustment ?: 1,
                totalRows = totalRowsValue,
                imagePath = state.imagePath
            )
            val newId = upsertProject(project).toInt()
            if (state.project?.id == 0 && newId > 0) {
                val updatedProject = project.copy(id = newId)
                originalTitle = state.title
                originalTotalRows = state.totalRows
                originalImagePath = updatedProject.imagePath
                _uiState.update { currentState ->
                    currentState.copy(
                        project = updatedProject,
                        imagePath = updatedProject.imagePath,
                        hasUnsavedChanges = false
                    )
                }
            } else if (state.project?.id != null && state.project.id > 0) {
                val updatedProject = project.copy(id = state.project.id)
                originalTitle = state.title
                originalTotalRows = state.totalRows
                originalImagePath = updatedProject.imagePath
                _uiState.update { currentState ->
                    currentState.copy(
                        project = updatedProject,
                        imagePath = updatedProject.imagePath,
                        hasUnsavedChanges = false
                    )
                }
            }
        }
    }
    
    fun attemptDismissal() {
        viewModelScope.launch {
            autoSaveJob?.cancel()
            val state = _uiState.value
            
            if (state.title.isBlank()) {
                _uiState.update { currentState ->
                    currentState.copy(titleError = "Title is required")
                }
                _dismissalResult.send(DismissalResult.ShowDiscardDialog)
            } else if (state.hasUnsavedChanges) {
                _dismissalResult.send(DismissalResult.ShowDiscardDialog)
            } else {
                save()
                _dismissalResult.send(DismissalResult.Allowed)
            }
        }
    }

    fun discardChanges() {
        _uiState.update { currentState ->
            currentState.copy(
                title = originalTitle,
                totalRows = originalTotalRows,
                imagePath = originalImagePath,
                hasUnsavedChanges = false,
                titleError = null,
                totalRowsError = null
            )
        }
    }

    fun updateImagePath(imagePath: String?) {
        val currentTitle = _uiState.value.title
        val currentTotalRows = _uiState.value.totalRows
        _uiState.update { currentState ->
            currentState.copy(
                imagePath = imagePath,
                hasUnsavedChanges = currentTitle != originalTitle || currentTotalRows != originalTotalRows || imagePath != originalImagePath
            )
        }
        triggerAutoSave()
    }

    fun createProject() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.title.isBlank()) {
                _uiState.update { currentState ->
                    currentState.copy(titleError = "Title is required")
                }
                return@launch
            }
            
            val isDoubleCounter = state.projectType == ProjectType.DOUBLE
            val totalRowsValue = state.totalRows.toIntOrNull() ?: 0
            if (isDoubleCounter && totalRowsValue <= 0) {
                _uiState.update { currentState ->
                    currentState.copy(totalRowsError = "Total rows is required and must be greater than 0")
                }
                return@launch
            }
            
            val existingProject = state.project
            val project = Project(
                id = 0,
                type = state.projectType,
                title = state.title,
                stitchCounterNumber = existingProject?.stitchCounterNumber ?: 0,
                stitchAdjustment = existingProject?.stitchAdjustment ?: 1,
                rowCounterNumber = existingProject?.rowCounterNumber ?: 0,
                rowAdjustment = existingProject?.rowAdjustment ?: 1,
                totalRows = totalRowsValue,
                imagePath = state.imagePath
            )
            val newId = upsertProject(project).toInt()
            if (newId > 0) {
                val updatedProject = project.copy(id = newId)
                originalTitle = state.title
                originalTotalRows = state.totalRows
                originalImagePath = updatedProject.imagePath
                _uiState.update { currentState ->
                    currentState.copy(
                        project = updatedProject,
                        imagePath = updatedProject.imagePath,
                        hasUnsavedChanges = false,
                        titleError = null,
                        totalRowsError = null
                    )
                }
            }
        }
    }
}






