package com.example.stitchcounterv3.feature.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stitchcounterv3.domain.model.DismissalResult
import com.example.stitchcounterv3.domain.model.ProjectType
import com.example.stitchcounterv3.feature.doublecounter.DoubleCounterScreen
import com.example.stitchcounterv3.feature.projectDetail.ProjectDetailScreenContent
import com.example.stitchcounterv3.feature.singleCounter.SingleCounterScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomSheetManager(
    currentSheetScreen: SheetScreen?,
    viewModel: RootNavigationViewModel,
    onDismissalResult: (DismissalResult) -> Unit
) {
    val isDismissalAllowedState = remember { mutableStateOf(false) }
    val isValidationPending = remember { mutableStateOf(false) }
    val isSheetVisible = currentSheetScreen != null
    val density = LocalDensity.current

    var screenHeight by remember { mutableStateOf(0.dp) }
    var shouldRenderSheet by remember { mutableStateOf(false) }
    val topOffset = 48.dp
    val dragOffset = remember { mutableStateOf(0.dp) }
    val isDragging = remember { mutableStateOf(false) }

    LaunchedEffect(isSheetVisible) {
        if (isSheetVisible) {
            shouldRenderSheet = true
            dragOffset.value = 0.dp
            isDragging.value = false
        } else {
            kotlinx.coroutines.delay(AnimationConstants.NAVIGATION_ANIMATION_DURATION.toLong())
            shouldRenderSheet = false
            dragOffset.value = 0.dp
            isDragging.value = false
        }
    }

    val baseOffset = if (isSheetVisible) topOffset else screenHeight
    val targetOffset =
        if (isDragging.value && isSheetVisible) (topOffset + dragOffset.value) else baseOffset

    val sheetOffset = animateDpAsState(
        targetValue = targetOffset,
        animationSpec = if (isDragging.value) tween(durationMillis = 0) else tween(durationMillis = AnimationConstants.NAVIGATION_ANIMATION_DURATION),
        label = "bottom_sheet_offset"
    )

    val dismissThreshold = remember(screenHeight) {
        if (screenHeight > 0.dp) (screenHeight * 0.3f) else 200.dp
    }

    fun handleDismissalResult(result: DismissalResult) {
        isValidationPending.value = false
        when (result) {
            is DismissalResult.Allowed -> {
                isDismissalAllowedState.value = true
                viewModel.showBottomSheet(null)
            }

            is DismissalResult.Blocked -> {
                isDismissalAllowedState.value = false
                dragOffset.value = 0.dp
                isDragging.value = false
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

    val onDismissRequestHandler: () -> Unit = {
        if (!isDismissalAllowedState.value) {
            isValidationPending.value = true
        }
    }

    if (shouldRenderSheet) {
        Box(modifier = Modifier.fillMaxSize()) {
            val backdropAlpha = animateFloatAsState(
                targetValue = if (isSheetVisible) 0.4f else 0f,
                animationSpec = tween(durationMillis = AnimationConstants.NAVIGATION_ANIMATION_DURATION),
                label = "backdrop_alpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = backdropAlpha.value))
                    .clickable(enabled = isSheetVisible) {
                        onDismissRequestHandler()
                    }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates ->
                        screenHeight = with(density) { coordinates.size.height.toDp() }
                    }
                    .offset(y = sheetOffset.value)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .pointerInput(isSheetVisible, isDismissalAllowedState.value, screenHeight) {
                        if (isSheetVisible && screenHeight > 0.dp) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    if (dragOffset.value > dismissThreshold) {
                                        if (isDismissalAllowedState.value) {
                                            viewModel.showBottomSheet(null)
                                        } else {
                                            onDismissRequestHandler()
                                        }
                                    } else {
                                        dragOffset.value = 0.dp
                                    }
                                    isDragging.value = false
                                }
                            ) { _, dragAmount ->
                                if (dragAmount > 0) {
                                    isDragging.value = true
                                    val dragAmountDp = with(density) { dragAmount.toDp() }
                                    dragOffset.value =
                                        (dragOffset.value + dragAmountDp).coerceAtLeast(0.dp)
                                }
                            }
                        }
                    }
            ) {
                currentSheetScreen?.let { screen ->
                    val singleCounterViewModel =
                        hiltViewModel<com.example.stitchcounterv3.feature.singleCounter.SingleCounterViewModel>()
                    val doubleCounterViewModel =
                        hiltViewModel<com.example.stitchcounterv3.feature.doublecounter.DoubleCounterViewModel>()
                    val projectDetailViewModel =
                        hiltViewModel<com.example.stitchcounterv3.feature.projectDetail.ProjectDetailViewModel>()
                    val projectDetailUiState by projectDetailViewModel.uiState.collectAsStateWithLifecycle()

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
                            }

                            SheetDismissalHandler(
                                screen = screen,
                                onAttemptDismissal = { projectDetailViewModel.attemptDismissal() }
                            )

                            val hasNavigatedToCounter = remember(screen) { mutableStateOf(false) }
                            val lastObservedProjectId =
                                remember(screen) { mutableStateOf<Int?>(null) }
                            var initialProjectIdWhenCreatingNew by remember(screen) {
                                mutableStateOf<Int?>(
                                    null
                                )
                            }

                            LaunchedEffect(screen.projectId) {
                                if (screen.projectId == null) {
                                    hasNavigatedToCounter.value = false
                                    lastObservedProjectId.value = null
                                    initialProjectIdWhenCreatingNew =
                                        projectDetailUiState.project?.id
                                }
                            }

                            LaunchedEffect(projectDetailUiState.project?.id) {
                                val currentProjectId = projectDetailUiState.project?.id

                                val wasNewProject =
                                    lastObservedProjectId == null || lastObservedProjectId.value == 0
                                val isNowSaved = currentProjectId != null && currentProjectId > 0
                                val isNewProjectScreen = screen.projectId == null
                                val isProjectIdChanged =
                                    lastObservedProjectId.value != currentProjectId
                                val isNotStaleProjectId = initialProjectIdWhenCreatingNew == null ||
                                        currentProjectId == null ||
                                        currentProjectId == 0 ||
                                        currentProjectId != initialProjectIdWhenCreatingNew

                                if (isNewProjectScreen && wasNewProject && isNowSaved && isProjectIdChanged && isNotStaleProjectId && !hasNavigatedToCounter.value) {
                                    hasNavigatedToCounter.value = true
                                    viewModel.showBottomSheet(
                                        createSheetScreenForProjectType(
                                            screen.projectType,
                                            currentProjectId
                                        )
                                    )
                                }

                                lastObservedProjectId.value = currentProjectId
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AnimatedContent(
                            targetState = screen,
                            transitionSpec = {
                                val isGoingToDetail =
                                    targetState is SheetScreen.ProjectDetail && initialState !is SheetScreen.ProjectDetail

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
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = topOffset)
                                    .imePadding()
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(32.dp)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            )
                                    )
                                }
                                
                                Box(modifier = Modifier.fillMaxSize()) {
                                    when (currentScreen) {
                                    is SheetScreen.SingleCounter -> {
                                        Box(modifier = Modifier.fillMaxSize()) {
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
                                    }

                                    is SheetScreen.DoubleCounter -> {
                                        Box(modifier = Modifier.fillMaxSize()) {
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
                                    }

                                    is SheetScreen.ProjectDetail -> {
                                        ProjectDetailScreenContent(
                                            projectId = currentScreen.projectId,
                                            projectType = currentScreen.projectType,
                                            viewModel = projectDetailViewModel,
                                            onNavigateBack = { projectId ->
                                                projectDetailUiState.project?.type?.let { projectType ->
                                                    viewModel.showBottomSheet(
                                                        createSheetScreenForProjectType(
                                                            projectType,
                                                            projectId
                                                        )
                                                    )
                                                }
                                            },
                                            onDismiss = {
                                                isDismissalAllowedState.value = true
                                                viewModel.showBottomSheet(null)
                                            },
                                            onCreateProject = {
                                                projectDetailViewModel.createProject()
                                            }
                                        )
                                    }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LaunchedEffect(currentSheetScreen) {
                if (currentSheetScreen != null) {
                    isDismissalAllowedState.value = false
                }
            }
        }
    }
}