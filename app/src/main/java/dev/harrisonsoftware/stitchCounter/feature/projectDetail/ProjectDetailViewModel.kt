package dev.harrisonsoftware.stitchCounter.feature.projectDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class ProjectDetailUiState(
    val project: Project? = null,
    val title: String = "",
    val projectType: ProjectType = ProjectType.SINGLE,
    val totalRows: String = "",
    val imagePaths: List<String> = emptyList(),
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
    private var originalImagePaths: List<String> = emptyList()

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
                        imagePaths = emptyList(),
                        isLoading = false,
                        hasUnsavedChanges = false,
                        titleError = null,
                        totalRowsError = null
                    )
                }
                originalTitle = ""
                originalTotalRows = ""
                originalImagePaths = emptyList()
                return@launch
            }
            
            val project = getProject(projectId)
            if (project != null) {
                originalTitle = project.title
                originalTotalRows = if (project.totalRows > 0) project.totalRows.toString() else ""
                originalImagePaths = project.imagePaths
                _uiState.update { currentState ->
                    currentState.copy(
                        project = project,
                        title = project.title,
                        projectType = project.type,
                        totalRows = if (project.totalRows > 0) project.totalRows.toString() else "",
                        imagePaths = project.imagePaths,
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
                originalImagePaths = project.imagePaths
                _uiState.update { currentState ->
                    currentState.copy(
                        project = project,
                        title = project.title,
                        projectType = project.type,
                        totalRows = if (project.totalRows > 0) project.totalRows.toString() else "",
                        imagePaths = project.imagePaths,
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
        val currentImagePaths = _uiState.value.imagePaths
        val currentTotalRows = _uiState.value.totalRows
        _uiState.update { currentState ->
            currentState.copy(
                title = newTitle,
                hasUnsavedChanges = newTitle != originalTitle || currentTotalRows != originalTotalRows || currentImagePaths != originalImagePaths,
                titleError = if (newTitle.isBlank()) "Title is required" else null
            )
        }
        triggerAutoSave()
    }

    fun updateTotalRows(newTotalRows: String) {
        val currentTitle = _uiState.value.title
        val currentImagePaths = _uiState.value.imagePaths
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
                hasUnsavedChanges = currentTitle != originalTitle || newTotalRows != originalTotalRows || currentImagePaths != originalImagePaths,
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
                imagePaths = state.imagePaths
            )
            val newId = upsertProject(project).toInt()
            if (state.project?.id == 0 && newId > 0) {
                val updatedProject = project.copy(id = newId)
                originalTitle = state.title
                originalTotalRows = state.totalRows
                originalImagePaths = updatedProject.imagePaths
                _uiState.update { currentState ->
                    currentState.copy(
                        project = updatedProject,
                        imagePaths = updatedProject.imagePaths,
                        hasUnsavedChanges = false
                    )
                }
            } else if (state.project?.id != null && state.project.id > 0) {
                val updatedProject = project.copy(id = state.project.id)
                originalTitle = state.title
                originalTotalRows = state.totalRows
                originalImagePaths = updatedProject.imagePaths
                _uiState.update { currentState ->
                    currentState.copy(
                        project = updatedProject,
                        imagePaths = updatedProject.imagePaths,
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
                imagePaths = originalImagePaths,
                hasUnsavedChanges = false,
                titleError = null,
                totalRowsError = null
            )
        }
    }

    fun addImagePath(imagePath: String) {
        val currentTitle = _uiState.value.title
        val currentTotalRows = _uiState.value.totalRows
        val currentImagePaths = _uiState.value.imagePaths
        val newImagePaths = currentImagePaths + imagePath
        _uiState.update { currentState ->
            currentState.copy(
                imagePaths = newImagePaths,
                hasUnsavedChanges = currentTitle != originalTitle || currentTotalRows != originalTotalRows || newImagePaths != originalImagePaths
            )
        }
        triggerAutoSave()
    }

    fun removeImagePath(imagePath: String) {
        val currentTitle = _uiState.value.title
        val currentTotalRows = _uiState.value.totalRows
        val currentImagePaths = _uiState.value.imagePaths
        val newImagePaths = currentImagePaths.filter { it != imagePath }
        _uiState.update { currentState ->
            currentState.copy(
                imagePaths = newImagePaths,
                hasUnsavedChanges = currentTitle != originalTitle || currentTotalRows != originalTotalRows || newImagePaths != originalImagePaths
            )
        }
        triggerAutoSave()
    }

    fun updateImagePaths(imagePaths: List<String>) {
        val currentTitle = _uiState.value.title
        val currentTotalRows = _uiState.value.totalRows
        _uiState.update { currentState ->
            currentState.copy(
                imagePaths = imagePaths,
                hasUnsavedChanges = currentTitle != originalTitle || currentTotalRows != originalTotalRows || imagePaths != originalImagePaths
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
                imagePaths = state.imagePaths
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
            }
        }
    }
}






