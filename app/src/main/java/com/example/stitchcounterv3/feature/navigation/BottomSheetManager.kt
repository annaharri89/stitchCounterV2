package com.example.stitchcounterv3.feature.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stitchcounterv3.domain.model.DismissalResult
import com.example.stitchcounterv3.domain.model.ProjectType
import com.example.stitchcounterv3.feature.doublecounter.DoubleCounterScreen
import com.example.stitchcounterv3.feature.projectDetail.ProjectDetailContent
import com.example.stitchcounterv3.feature.singleCounter.SingleCounterScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun BottomSheetManager(
    currentSheetScreen: SheetScreen?,
    viewModel: RootNavigationViewModel,
    onDismissalResult: (DismissalResult) -> Unit
) {
    val scope = rememberCoroutineScope()
    val isDismissalAllowedState = remember { mutableStateOf(false) }
    val isValidationPending = remember { mutableStateOf(false) }
    val currentSheetValue = remember { mutableStateOf<SheetValue?>(null) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { target ->
            val isDismissing = target == SheetValue.Hidden && currentSheetValue.value == SheetValue.Expanded
            if (isDismissing && !isDismissalAllowedState.value) {
                if (!isValidationPending.value) {
                    isValidationPending.value = true
                }
                false
            } else {
                val allowed = target != SheetValue.Hidden || isDismissalAllowedState.value
                currentSheetValue.value = target
                allowed
            }
        }
    )

    LaunchedEffect(sheetState.currentValue) {
        currentSheetValue.value = sheetState.currentValue
    }

    fun handleDismissalResult(result: DismissalResult) {
        isValidationPending.value = false
        when (result) {
            is DismissalResult.Allowed -> {
                isDismissalAllowedState.value = true
                scope.launch {
                    sheetState.hide()
                }
                viewModel.showBottomSheet(null)
            }
            is DismissalResult.Blocked -> {
                isDismissalAllowedState.value = false
                if (sheetState.currentValue != SheetValue.Expanded) {
                    scope.launch {
                        sheetState.expand()
                    }
                }
            }
            is DismissalResult.ShowDiscardDialog -> {
                onDismissalResult(result)
            }
        }
    }

    @Composable
    fun <T : SheetScreen> SheetDismissalHandler(
        screen: T,
        onAttemptDismissal: () -> Unit
    ) {
        LaunchedEffect(screen) {
            isDismissalAllowedState.value = false
            isValidationPending.value = false
        }

        LaunchedEffect(isValidationPending.value, currentSheetScreen) {
            if (isValidationPending.value && currentSheetScreen == screen) {
                onAttemptDismissal()
            }
        }
    }

    val sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val sheetModifier = Modifier.fillMaxHeight(0.98f).fillMaxWidth()

    val onDismissRequestHandler: () -> Unit = {
        if (!isDismissalAllowedState.value) {
            isValidationPending.value = true
        }
    }

    currentSheetScreen?.let { screen ->
        val singleCounterViewModel = hiltViewModel<com.example.stitchcounterv3.feature.singleCounter.SingleCounterViewModel>()
        val doubleCounterViewModel = hiltViewModel<com.example.stitchcounterv3.feature.doublecounter.DoubleCounterViewModel>()
        val projectDetailViewModel = hiltViewModel<com.example.stitchcounterv3.feature.projectDetail.ProjectDetailViewModel>()
        val projectDetailUiState by projectDetailViewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current
        var showDiscardDialog = remember { mutableStateOf(false) }

        when (screen) {
            is SheetScreen.SingleCounter -> {
                LaunchedEffect(screen.projectId) {
                    singleCounterViewModel.loadProject(screen.projectId)
                }

                SheetDismissalHandler(
                    screen = screen,
                    onAttemptDismissal = { singleCounterViewModel.attemptDismissal() }
                )

                LaunchedEffect(screen) {
                    singleCounterViewModel.dismissalResult.collect { result ->
                        handleDismissalResult(result)
                    }
                }
            }
            is SheetScreen.DoubleCounter -> {
                LaunchedEffect(screen.projectId) {
                    doubleCounterViewModel.loadProject(screen.projectId)
                }

                SheetDismissalHandler(
                    screen = screen,
                    onAttemptDismissal = { doubleCounterViewModel.attemptDismissal() }
                )

                LaunchedEffect(screen) {
                    doubleCounterViewModel.dismissalResult.collect { result ->
                        handleDismissalResult(result)
                    }
                }
            }
            is SheetScreen.ProjectDetail -> {
                LaunchedEffect(screen) {
                    if (screen.projectId == null) {
                        singleCounterViewModel.resetState()
                        doubleCounterViewModel.resetState()
                    }
                    projectDetailViewModel.loadProject(screen.projectId, screen.projectType)
                }

                SheetDismissalHandler(
                    screen = screen,
                    onAttemptDismissal = { projectDetailViewModel.attemptDismissal() }
                )

                LaunchedEffect(screen) {
                    projectDetailViewModel.dismissalResult.collect { result ->
                        when (result) {
                            is DismissalResult.Allowed -> {
                                handleDismissalResult(result)
                            }
                            is DismissalResult.Blocked -> {
                                handleDismissalResult(result)
                            }
                            is DismissalResult.ShowDiscardDialog -> {
                                showDiscardDialog.value = true
                            }
                        }
                    }
                }

                val hasNavigatedToCounter = remember(screen) { mutableStateOf(false) }
                val lastObservedProjectId = remember(screen) { mutableStateOf<Int?>(null) }
                var initialProjectIdWhenCreatingNew by remember(screen) { mutableStateOf<Int?>(null) }

                LaunchedEffect(screen.projectId) {
                    if (screen.projectId == null) {
                        hasNavigatedToCounter.value = false
                        lastObservedProjectId.value = null
                        initialProjectIdWhenCreatingNew = projectDetailUiState.project?.id
                    }
                }

                LaunchedEffect(projectDetailUiState.project?.id) {
                    val currentProjectId = projectDetailUiState.project?.id

                    val wasNewProject = lastObservedProjectId == null || lastObservedProjectId.value == 0
                    val isNowSaved = currentProjectId != null && currentProjectId > 0
                    val isNewProjectScreen = screen.projectId == null
                    val isProjectIdChanged = lastObservedProjectId.value != currentProjectId
                    val isNotStaleProjectId = initialProjectIdWhenCreatingNew == null ||
                        currentProjectId == null ||
                        currentProjectId == 0 ||
                        currentProjectId != initialProjectIdWhenCreatingNew

                    if (isNewProjectScreen && wasNewProject && isNowSaved && isProjectIdChanged && isNotStaleProjectId && !hasNavigatedToCounter.value) {
                        hasNavigatedToCounter.value = true
                        viewModel.showBottomSheet(
                            createSheetScreenForProjectType(screen.projectType, currentProjectId)
                        )
                    }

                    lastObservedProjectId.value = currentProjectId
                }
            }
        }

        ModalBottomSheet(
            onDismissRequest = onDismissRequestHandler,
            sheetState = sheetState,
            shape = sheetShape,
            modifier = sheetModifier
        ) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    val isGoingToDetail = targetState is SheetScreen.ProjectDetail && initialState !is SheetScreen.ProjectDetail

                    slideInHorizontally(
                        initialOffsetX = { fullWidth ->
                            if (isGoingToDetail) fullWidth else -fullWidth
                        },
                        animationSpec = tween(durationMillis = AnimationConstants.NAVIGATION_ANIMATION_DURATION)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { fullWidth ->
                            if (isGoingToDetail) -fullWidth else fullWidth
                        },
                        animationSpec = tween(durationMillis = AnimationConstants.NAVIGATION_ANIMATION_DURATION)
                    )
                },
                label = "bottom_sheet_content"
            ) { currentScreen ->
                when (currentScreen) {
                    is SheetScreen.SingleCounter -> {
                        SingleCounterScreen(
                            projectId = currentScreen.projectId,
                            viewModel = singleCounterViewModel,
                            onNavigateToDetail = { projectId ->
                                viewModel.showBottomSheet(
                                    SheetScreen.ProjectDetail(
                                        projectId = projectId,
                                        projectType = ProjectType.SINGLE
                                    )
                                )
                            }
                        )
                    }
                    is SheetScreen.DoubleCounter -> {
                        DoubleCounterScreen(
                            projectId = currentScreen.projectId,
                            viewModel = doubleCounterViewModel,
                            onNavigateToDetail = { projectId ->
                                viewModel.showBottomSheet(
                                    SheetScreen.ProjectDetail(
                                        projectId = projectId,
                                        projectType = ProjectType.DOUBLE
                                    )
                                )
                            }
                        )
                    }
                    is SheetScreen.ProjectDetail -> {
                        ProjectDetailContent(
                            uiState = projectDetailUiState,
                            viewModel = projectDetailViewModel,
                            context = context,
                            showDiscardDialog = showDiscardDialog.value,
                            onDismissDiscardDialog = { showDiscardDialog = false },
                            onDiscard = {
                                projectDetailViewModel.discardChanges()
                                showDiscardDialog = false
                                isDismissalAllowedState.value = true
                                scope.launch {
                                    sheetState.hide()
                                }
                                viewModel.showBottomSheet(null)
                            },
                            onCreateProject = {
                                projectDetailViewModel.createProject()
                            },
                            onNavigateBack = { projectId ->
                                projectDetailUiState.project?.type?.let { projectType ->
                                    viewModel.showBottomSheet(
                                        createSheetScreenForProjectType(projectType, projectId)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        LaunchedEffect(currentSheetScreen) {
            if (currentSheetScreen != null) {
                isDismissalAllowedState.value = false
                try {
                    sheetState.show()
                } catch (_: Exception) {
                }
            }
        }
    }
}
