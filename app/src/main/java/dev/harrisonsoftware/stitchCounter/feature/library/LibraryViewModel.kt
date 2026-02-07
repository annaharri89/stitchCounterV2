package dev.harrisonsoftware.stitchCounter.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.domain.usecase.DeleteProject
import dev.harrisonsoftware.stitchCounter.domain.usecase.DeleteProjects
import dev.harrisonsoftware.stitchCounter.domain.usecase.ObserveProjects
import dev.harrisonsoftware.stitchCounter.feature.destinations.DoubleCounterScreenDestination
import dev.harrisonsoftware.stitchCounter.feature.destinations.SingleCounterScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class LibraryUiState(
    val isMultiSelectMode: Boolean = false,
    val selectedProjectIds: Set<Int> = emptySet(),
    val showDeleteConfirmation: Boolean = false,
    val projectsToDelete: List<Project> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    observeProjects: ObserveProjects,
    private val deleteProject: DeleteProject,
    private val deleteProjects: DeleteProjects,
) : ViewModel() {
    val projects: StateFlow<List<Project>> = observeProjects()
        .onEach {
            if (_uiState.value.isLoading) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    fun toggleMultiSelectMode() {
        _uiState.value = _uiState.value.copy(
            isMultiSelectMode = !_uiState.value.isMultiSelectMode,
            selectedProjectIds = emptySet()
        )
    }

    fun toggleProjectSelection(projectId: Int) {
        val currentSelected = _uiState.value.selectedProjectIds
        _uiState.value = _uiState.value.copy(
            selectedProjectIds = if (currentSelected.contains(projectId)) {
                currentSelected - projectId
            } else {
                currentSelected + projectId
            }
        )
    }

    fun selectAllProjects() {
        _uiState.value = _uiState.value.copy(
            selectedProjectIds = projects.value.map { it.id }.toSet()
        )
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedProjectIds = emptySet())
    }

    fun requestDelete(project: Project) {
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmation = true,
            projectsToDelete = listOf(project)
        )
    }

    fun requestBulkDelete() {
        val selectedIds = _uiState.value.selectedProjectIds
        val projectsToDelete = projects.value.filter { it.id in selectedIds }
        if (projectsToDelete.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                showDeleteConfirmation = true,
                projectsToDelete = projectsToDelete
            )
        }
    }

    fun confirmDelete() {
        val projectsToDelete = _uiState.value.projectsToDelete
        if (projectsToDelete.size == 1) {
            viewModelScope.launch { 
                deleteProject(projectsToDelete.first())
            }
        } else {
            viewModelScope.launch { 
                deleteProjects(projectsToDelete)
            }
        }
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmation = false,
            projectsToDelete = emptyList(),
            selectedProjectIds = emptySet(),
            isMultiSelectMode = false
        )
    }

    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmation = false,
            projectsToDelete = emptyList()
        )
    }

    fun navigateToEditProject(project: Project, navigator: DestinationsNavigator) {
        val destination = when (project.type) {
            ProjectType.SINGLE -> SingleCounterScreenDestination(projectId = project.id)
            ProjectType.DOUBLE -> DoubleCounterScreenDestination(projectId = project.id)
        }
        navigator.navigate(destination)
    }
}

