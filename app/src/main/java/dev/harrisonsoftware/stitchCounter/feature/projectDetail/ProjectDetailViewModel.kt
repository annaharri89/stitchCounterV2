package dev.harrisonsoftware.stitchCounter.feature.projectDetail

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.harrisonsoftware.stitchCounter.Constants
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.domain.model.DismissalResult
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.domain.validation.ProjectValidator
import dev.harrisonsoftware.stitchCounter.domain.usecase.GetProject
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpdateProjectDetailResult
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpdateProjectDetailValues
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpsertProjectResult
import dev.harrisonsoftware.stitchCounter.domain.usecase.UpsertProject
import dev.harrisonsoftware.stitchCounter.logging.AppLogger
import android.content.Context
import android.net.Uri
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
import kotlinx.coroutines.withContext

data class ProjectDetailUiState(
    val project: Project? = null,
    val title: String = "",
    val notes: String = "",
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
    private val updateProjectDetailValues: UpdateProjectDetailValues,
    private val appLogger: AppLogger,
) : ViewModel() {

    private sealed interface SaveToRoomResult {
        data object Saved : SaveToRoomResult
        data object NotSavedValidation : SaveToRoomResult
        data object NotSavedPersistence : SaveToRoomResult
    }

    companion object {
        private const val SAVED_STATE_KEY_PROJECT_ID = "detail_project_id"
        private const val SAVED_STATE_KEY_TITLE = "detail_title"
        private const val SAVED_STATE_KEY_NOTES = "detail_notes"
        private const val SAVED_STATE_KEY_PROJECT_TYPE = "detail_project_type"
        private const val SAVED_STATE_KEY_TOTAL_ROWS = "detail_total_rows"
        private const val SAVED_STATE_KEY_IS_COMPLETED = "detail_is_completed"
        private const val SAVED_STATE_KEY_IMAGE_PATHS = "detail_image_paths"
        private const val AUTO_SAVE_DELAY_MS = 1000L
    }

    private val _uiState = MutableStateFlow(ProjectDetailUiState())
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    private val _dismissalResult = Channel<DismissalResult>(Channel.BUFFERED)
    val dismissalResult = _dismissalResult.receiveAsFlow()

    private var autoSaveJob: Job? = null
    private var originalTitle: String = ""
    private var originalNotes: String = ""
    private var originalTotalRows: String = ""
    private var originalImagePaths: List<String> = emptyList()
    private var originalIsCompleted: Boolean = false

    fun loadProject(projectId: Int?, projectType: ProjectType) {
        viewModelScope.launch {
            _uiState.update { currentState -> currentState.copy(isLoading = true) }

            if (projectId == null || projectId == 0) {
                appLogger.info(
                    tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                    message = "event=project_load mode=new projectType=$projectType"
                )
                val savedProjectId = savedStateHandle.get<Int>(SAVED_STATE_KEY_PROJECT_ID)
                val previousProjectAlreadyCreated = savedProjectId != null && savedProjectId > 0
                if (previousProjectAlreadyCreated) {
                    clearSavedState()
                }
                val restoredTitle = savedStateHandle.get<String>(SAVED_STATE_KEY_TITLE)
                val restoredNotes = savedStateHandle.get<String>(SAVED_STATE_KEY_NOTES)
                val restoredTotalRows = savedStateHandle.get<String>(SAVED_STATE_KEY_TOTAL_ROWS)
                val restoredIsCompleted = savedStateHandle.get<Boolean>(SAVED_STATE_KEY_IS_COMPLETED)
                val restoredImagePaths = savedStateHandle.get<ArrayList<String>>(SAVED_STATE_KEY_IMAGE_PATHS)
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
                        notes = if (hasSavedNewProjectState) restoredNotes ?: "" else "",
                        projectType = projectType,
                        totalRows = if (hasSavedNewProjectState) restoredTotalRows ?: "" else "",
                        imagePaths = if (hasSavedNewProjectState) restoredImagePaths?.toList() ?: emptyList() else emptyList(),
                        isCompleted = if (hasSavedNewProjectState) restoredIsCompleted ?: false else false,
                        isLoading = false,
                        hasUnsavedChanges = hasSavedNewProjectState,
                        titleError = null,
                        totalRowsError = null
                    )
                }
                originalTitle = ""
                originalNotes = ""
                originalTotalRows = ""
                originalImagePaths = emptyList()
                originalIsCompleted = false
                return@launch
            }

            val project = getProject(projectId)
            if (project != null) {
                restoreExistingProjectState(project)
            } else {
                appLogger.warn(
                    tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                    message = "event=project_load_missing projectId=$projectId"
                )
                _uiState.update { currentState -> currentState.copy(isLoading = false) }
            }
        }
    }

    fun loadProjectById(projectId: Int) {
        viewModelScope.launch {
            _uiState.update { currentState -> currentState.copy(isLoading = true) }
            val project = getProject(projectId)
            if (project != null) {
                restoreExistingProjectState(project)
            } else {
                appLogger.warn(
                    tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                    message = "event=project_load_missing projectId=$projectId source=by_id"
                )
                _uiState.update { currentState -> currentState.copy(isLoading = false) }
            }
        }
    }

    private fun restoreExistingProjectState(project: Project) {
        val savedProjectId = savedStateHandle.get<Int>(SAVED_STATE_KEY_PROJECT_ID)
        val restoreFromSavedState = savedProjectId == project.id

        val restoredTitle = if (restoreFromSavedState) {
            savedStateHandle.get<String>(SAVED_STATE_KEY_TITLE) ?: project.title
        } else {
            project.title
        }
        val restoredNotes = if (restoreFromSavedState) {
            savedStateHandle.get<String>(SAVED_STATE_KEY_NOTES) ?: project.notes
        } else {
            project.notes
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
        val restoredImagePaths = if (restoreFromSavedState) {
            savedStateHandle.get<ArrayList<String>>(SAVED_STATE_KEY_IMAGE_PATHS)?.toList()
                ?: project.imagePaths
        } else {
            project.imagePaths
        }

        originalTitle = project.title
        originalNotes = project.notes
        originalTotalRows = if (project.totalRows > 0) project.totalRows.toString() else ""
        originalImagePaths = project.imagePaths
        originalIsCompleted = project.completedAt != null

        val hasChangesFromSavedState = restoreFromSavedState && (
            restoredTitle != originalTitle
                || restoredNotes != originalNotes
                || restoredTotalRows != originalTotalRows
                || restoredIsCompleted != originalIsCompleted
                || restoredImagePaths != originalImagePaths
        )

        _uiState.update { currentState ->
            currentState.copy(
                project = project,
                title = restoredTitle,
                notes = restoredNotes,
                projectType = project.type,
                totalRows = restoredTotalRows,
                imagePaths = restoredImagePaths,
                isCompleted = restoredIsCompleted,
                isLoading = false,
                hasUnsavedChanges = hasChangesFromSavedState,
                titleError = null,
                totalRowsError = null
            )
        }

        if (hasChangesFromSavedState) {
            appLogger.info(
                tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                message = "event=project_restore_saved_state projectId=${project.id}"
            )
            triggerAutoSave()
        }
    }

    private fun hasChanges(
        title: String = _uiState.value.title,
        notes: String = _uiState.value.notes,
        totalRows: String = _uiState.value.totalRows,
        imagePaths: List<String> = _uiState.value.imagePaths,
        isCompleted: Boolean = _uiState.value.isCompleted
    ): Boolean =
        title != originalTitle
                || notes != originalNotes
                || totalRows != originalTotalRows
                || imagePaths != originalImagePaths
                || isCompleted != originalIsCompleted

    fun updateTitle(newTitle: String) {
        _uiState.update { currentState ->
            currentState.copy(
                title = newTitle,
                hasUnsavedChanges = hasChanges(title = newTitle),
                titleError = if (!ProjectValidator.isTitleValid(newTitle)) R.string.error_title_required else null
            )
        }
        persistToSavedState()
        triggerAutoSave()
    }

    fun updateNotes(newNotes: String) {
        _uiState.update { currentState ->
            currentState.copy(
                notes = newNotes,
                hasUnsavedChanges = hasChanges(notes = newNotes)
            )
        }
        persistToSavedState()
        triggerAutoSave()
    }

    fun updateTotalRows(newTotalRows: String) {
        val state = _uiState.value
        val totalRowsValue = newTotalRows.toIntOrNull() ?: 0
        val isDoubleCounter = state.projectType == ProjectType.DOUBLE
        val totalRowsError = if (isDoubleCounter && newTotalRows.isNotBlank() && !ProjectValidator.areTotalRowsValidForType(totalRowsValue, state.projectType)) {
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
        val canPersist = canPersistState(state)
        if (state.hasUnsavedChanges && isExistingProject && canPersist) {
            appLogger.info(
                tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                message = "event=autosave_scheduled projectId=${state.project?.id ?: 0} delayMs=$AUTO_SAVE_DELAY_MS"
            )
            autoSaveJob = viewModelScope.launch {
                delay(AUTO_SAVE_DELAY_MS)
                appLogger.info(
                    tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                    message = "event=autosave_executed projectId=${_uiState.value.project?.id ?: 0}"
                )
                saveToRoom()
            }
        } else {
            appLogger.info(
                tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                message = "event=autosave_skipped hasUnsavedChanges=${state.hasUnsavedChanges} isExistingProject=$isExistingProject canPersistState=$canPersist"
            )
        }
    }

    fun save() {
        viewModelScope.launch {
            autoSaveJob?.cancel()
            saveToRoom()
        }
    }

    private suspend fun saveToRoom(): SaveToRoomResult {
        val state = _uiState.value
        val staleProject = state.project
        val projectId = staleProject?.id ?: 0
        val totalRowsValue = state.totalRows.toIntOrNull() ?: 0
        val now = System.currentTimeMillis()
        val completedAt = if (state.isCompleted) (staleProject?.completedAt ?: now) else null

        if (projectId > 0) {
            val updateResult = updateProjectDetailValues(
                id = projectId,
                title = state.title,
                notes = state.notes,
                totalRows = totalRowsValue,
                projectType = state.projectType,
                imagePaths = state.imagePaths,
                completedAt = completedAt,
                updatedAt = now
            )
            if (!applyUpdateProjectDetailResult(updateResult)) {
                appLogger.warn(
                    tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                    message = "event=save_blocked_validation projectId=$projectId result=$updateResult"
                )
                return SaveToRoomResult.NotSavedValidation
            }
            val freshProject = getProject(projectId)
            if (freshProject != null) {
                originalTitle = state.title
                originalNotes = state.notes
                originalTotalRows = state.totalRows
                originalImagePaths = state.imagePaths
                originalIsCompleted = state.isCompleted
                _uiState.update { currentState ->
                    currentState.copy(
                        project = freshProject,
                        imagePaths = freshProject.imagePaths,
                        hasUnsavedChanges = false,
                        titleError = null,
                        totalRowsError = null
                    )
                }
            }
            appLogger.info(
                tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                message = "event=save_success projectId=$projectId mode=update"
            )
            return SaveToRoomResult.Saved
        } else {
            val freshProject = if (projectId > 0) getProject(projectId) else null
            val baseProject = freshProject ?: staleProject
            val project = Project(
                id = 0,
                type = state.projectType,
                title = state.title,
                notes = state.notes,
                stitchCounterNumber = baseProject?.stitchCounterNumber ?: 0,
                stitchAdjustment = baseProject?.stitchAdjustment ?: 1,
                rowCounterNumber = baseProject?.rowCounterNumber ?: 0,
                rowAdjustment = baseProject?.rowAdjustment ?: 1,
                totalRows = totalRowsValue,
                imagePaths = state.imagePaths,
                createdAt = now,
                updatedAt = now,
                completedAt = completedAt,
                totalStitchesEver = 0,
            )
            val upsertResult = upsertProject(project)
            val newId = when (upsertResult) {
                is UpsertProjectResult.Success -> upsertResult.projectId.toInt()
                UpsertProjectResult.InvalidTitle -> {
                    appLogger.warn(
                        tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                        message = "event=create_blocked_validation reason=invalid_title"
                    )
                    _uiState.update { currentState ->
                        currentState.copy(titleError = R.string.error_title_required)
                    }
                    return SaveToRoomResult.NotSavedValidation
                }
            }
            if (newId > 0) {
                val updatedProject = project.copy(id = newId)
                originalTitle = state.title
                originalNotes = state.notes
                originalTotalRows = state.totalRows
                originalImagePaths = updatedProject.imagePaths
                originalIsCompleted = state.isCompleted
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
                appLogger.info(
                    tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                    message = "event=save_success projectId=$newId mode=create"
                )
                return SaveToRoomResult.Saved
            }
            appLogger.warn(
                tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                message = "event=save_failed_persistence mode=create"
            )
            return SaveToRoomResult.NotSavedPersistence
        }
    }

    suspend fun ensureSaved() {
        autoSaveJob?.cancel()
        val state = _uiState.value
        val isExistingProject = state.project?.id != null && state.project.id > 0
        if (state.hasUnsavedChanges && isExistingProject && canPersistState(state)) {
            saveToRoom()
        }
    }

    fun attemptDismissal() {
        viewModelScope.launch {
            autoSaveJob?.cancel()
            val state = _uiState.value

            if (!ProjectValidator.isTitleValid(state.title)) {
                appLogger.warn(
                    tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                    message = "event=dismissal_blocked reason=invalid_title"
                )
                _uiState.update { currentState ->
                    currentState.copy(titleError = R.string.error_title_required)
                }
                _dismissalResult.send(DismissalResult.ShowDiscardDialog)
            } else {
                when (saveToRoom()) {
                    SaveToRoomResult.Saved -> _dismissalResult.send(DismissalResult.Allowed)
                    SaveToRoomResult.NotSavedValidation,
                    SaveToRoomResult.NotSavedPersistence -> {
                        appLogger.warn(
                            tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                            message = "event=dismissal_show_discard_dialog reason=save_failed"
                        )
                        _dismissalResult.send(DismissalResult.ShowDiscardDialog)
                    }
                }
            }
        }
    }

    fun discardChanges() {
        _uiState.update { currentState ->
            currentState.copy(
                title = originalTitle,
                notes = originalNotes,
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

    fun saveAndAddImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            val savedImagePath = withContext(Dispatchers.IO) {
                saveImageToInternalStorage(context, uri)
            }
            if (savedImagePath == null) {
                appLogger.warn(
                    tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                    message = "event=image_add_failed reason=save_returned_null"
                )
            }
            savedImagePath?.let { addImagePath(it) }
        }
    }

    fun addImagePath(imagePath: String) {
        val newImagePaths = _uiState.value.imagePaths + imagePath
        _uiState.update { currentState ->
            currentState.copy(
                imagePaths = newImagePaths,
                hasUnsavedChanges = hasChanges(imagePaths = newImagePaths)
            )
        }
        persistToSavedState()
        triggerAutoSave()
    }

    fun removeImagePath(imagePath: String) {
        val imageWasPresent = _uiState.value.imagePaths.contains(imagePath)
        if (!imageWasPresent) {
            appLogger.warn(
                tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                message = "event=image_remove_missing path=$imagePath"
            )
        }
        val newImagePaths = _uiState.value.imagePaths.filter { it != imagePath }
        _uiState.update { currentState ->
            currentState.copy(
                imagePaths = newImagePaths,
                hasUnsavedChanges = hasChanges(imagePaths = newImagePaths)
            )
        }
        persistToSavedState()
        triggerAutoSave()
    }

    fun createProject() {
        viewModelScope.launch {
            val state = _uiState.value
            if (!ProjectValidator.isTitleValid(state.title)) {
                appLogger.warn(
                    tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                    message = "event=create_blocked_validation reason=invalid_title"
                )
                _uiState.update { currentState ->
                    currentState.copy(titleError = R.string.error_title_required)
                }
                return@launch
            }

            val totalRowsValue = state.totalRows.toIntOrNull() ?: 0
            if (!ProjectValidator.areTotalRowsValidForType(totalRowsValue, state.projectType)) {
                appLogger.warn(
                    tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                    message = "event=create_blocked_validation reason=invalid_total_rows projectType=${state.projectType}"
                )
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
                notes = state.notes,
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
            val upsertResult = upsertProject(project)
            val newId = when (upsertResult) {
                is UpsertProjectResult.Success -> upsertResult.projectId.toInt()
                UpsertProjectResult.InvalidTitle -> {
                    appLogger.warn(
                        tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                        message = "event=create_blocked_validation reason=invalid_title_upsert"
                    )
                    _uiState.update { currentState ->
                        currentState.copy(titleError = R.string.error_title_required)
                    }
                    return@launch
                }
            }
            if (newId > 0) {
                val updatedProject = project.copy(id = newId)
                originalTitle = state.title
                originalNotes = state.notes
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
                appLogger.info(
                    tag = Constants.LOG_TAG_PROJECT_DETAIL_VIEW_MODEL,
                    message = "event=create_success projectId=$newId"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        val state = _uiState.value
        val projectId = state.project?.id ?: 0
        if (projectId > 0 && state.hasUnsavedChanges && canPersistState(state)) {
            CoroutineScope(Dispatchers.IO + NonCancellable).launch {
                saveToRoom()
            }
        }
    }

    private fun canPersistState(state: ProjectDetailUiState): Boolean {
        val totalRowsValue = state.totalRows.toIntOrNull() ?: 0
        return ProjectValidator.isTitleValid(state.title)
                && ProjectValidator.areTotalRowsValidForType(totalRowsValue, state.projectType)
    }

    private fun applyUpdateProjectDetailResult(result: UpdateProjectDetailResult): Boolean {
        return when (result) {
            UpdateProjectDetailResult.Success -> true
            UpdateProjectDetailResult.InvalidTitle -> {
                _uiState.update { currentState ->
                    currentState.copy(titleError = R.string.error_title_required)
                }
                false
            }
            UpdateProjectDetailResult.InvalidTotalRows -> {
                _uiState.update { currentState ->
                    currentState.copy(totalRowsError = R.string.error_total_rows_required_and_greater)
                }
                false
            }
        }
    }

    private fun persistToSavedState() {
        val state = _uiState.value
        savedStateHandle[SAVED_STATE_KEY_PROJECT_ID] = state.project?.id ?: 0
        savedStateHandle[SAVED_STATE_KEY_TITLE] = state.title
        savedStateHandle[SAVED_STATE_KEY_NOTES] = state.notes
        savedStateHandle[SAVED_STATE_KEY_PROJECT_TYPE] = state.projectType.name
        savedStateHandle[SAVED_STATE_KEY_TOTAL_ROWS] = state.totalRows
        savedStateHandle[SAVED_STATE_KEY_IS_COMPLETED] = state.isCompleted
        savedStateHandle[SAVED_STATE_KEY_IMAGE_PATHS] = ArrayList(state.imagePaths)
    }

    private fun clearSavedState() {
        savedStateHandle.remove<Int>(SAVED_STATE_KEY_PROJECT_ID)
        savedStateHandle.remove<String>(SAVED_STATE_KEY_TITLE)
        savedStateHandle.remove<String>(SAVED_STATE_KEY_NOTES)
        savedStateHandle.remove<String>(SAVED_STATE_KEY_PROJECT_TYPE)
        savedStateHandle.remove<String>(SAVED_STATE_KEY_TOTAL_ROWS)
        savedStateHandle.remove<Boolean>(SAVED_STATE_KEY_IS_COMPLETED)
        savedStateHandle.remove<ArrayList<String>>(SAVED_STATE_KEY_IMAGE_PATHS)
    }
}
