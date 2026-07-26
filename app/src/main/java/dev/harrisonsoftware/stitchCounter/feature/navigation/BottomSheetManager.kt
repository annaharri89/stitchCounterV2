package dev.harrisonsoftware.stitchCounter.feature.navigation

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.harrisonsoftware.stitchCounter.R
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.harrisonsoftware.stitchCounter.domain.model.DismissalResult
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.feature.doublecounter.DoubleCounterScreen
import dev.harrisonsoftware.stitchCounter.feature.doublecounter.DoubleCounterViewModel
import dev.harrisonsoftware.stitchCounter.feature.projectDetail.ProjectDetailScreenContent
import dev.harrisonsoftware.stitchCounter.feature.projectDetail.ProjectDetailViewModel
import dev.harrisonsoftware.stitchCounter.feature.rowandrepeat.RowAndRepeat
import dev.harrisonsoftware.stitchCounter.feature.rowandrepeat.RowAndRepeatViewModel
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.sheetHeaderInsetPadding
import dev.harrisonsoftware.stitchCounter.feature.singleCounter.SingleCounterScreen
import dev.harrisonsoftware.stitchCounter.feature.singleCounter.SingleCounterViewModel

internal enum class DragEndAction {
    DismissSheet,
    RequestValidation,
    ResetDragOffset,
}

internal fun shouldRenderAfterVisibilityChange(isSheetVisible: Boolean): Boolean = isSheetVisible

internal fun shouldTriggerDismissValidation(isDismissalAllowed: Boolean): Boolean = !isDismissalAllowed

internal fun shouldRunDismissalAttempt(
    isValidationPending: Boolean,
    currentSheetScreen: SheetScreen?,
    targetScreen: SheetScreen,
): Boolean = isValidationPending && currentSheetScreen == targetScreen

internal fun dragEndAction(
    dragOffset: androidx.compose.ui.unit.Dp,
    dismissThreshold: androidx.compose.ui.unit.Dp,
    isDismissalAllowed: Boolean,
): DragEndAction {
    if (dragOffset <= dismissThreshold) {
        return DragEndAction.ResetDragOffset
    }
    return if (isDismissalAllowed) DragEndAction.DismissSheet else DragEndAction.RequestValidation
}

internal fun validationPendingAfterHandlingDismissalResult(): Boolean = false

internal fun shouldShowDiscardDialogForDismissalResult(result: DismissalResult): Boolean =
    result is DismissalResult.ShowDiscardDialog

internal fun shouldAutoNavigateFromNewProject(
    screenProjectId: Int?,
    lastObservedProjectId: Int?,
    currentProjectId: Int?,
    initialProjectIdWhenCreatingNew: Int?,
    hasNavigatedToCounter: Boolean,
): Boolean {
    val wasNewProject = lastObservedProjectId == null || lastObservedProjectId == 0
    val isNowSaved = currentProjectId != null && currentProjectId > 0
    val isNewProjectScreen = screenProjectId == null
    val isProjectIdChanged = lastObservedProjectId != currentProjectId
    val isNotStaleProjectId =
        initialProjectIdWhenCreatingNew == null ||
            currentProjectId == null ||
            currentProjectId == 0 ||
            currentProjectId != initialProjectIdWhenCreatingNew
    return isNewProjectScreen &&
        wasNewProject &&
        isNowSaved &&
        isProjectIdChanged &&
        isNotStaleProjectId &&
        !hasNavigatedToCounter
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomSheetManager(
    currentSheetScreen: SheetScreen?,
    viewModel: RootNavigationViewModel,
    isWideLayout: Boolean,
) {
    val isDismissalAllowedState = remember { mutableStateOf(false) }
    val isValidationPending = remember { mutableStateOf(false) }
    val showDiscardDialog = remember { mutableStateOf(false) }
    val isSheetVisible = currentSheetScreen != null
    val density = LocalDensity.current

    var screenHeight by remember { mutableStateOf(0.dp) }
    var shouldRenderSheet by remember { mutableStateOf(false) }
    val topOffset = if (isWideLayout) 24.dp else 48.dp
    val dragHandleTopPadding = if (isWideLayout) 4.dp else 8.dp
    val dragHandleBottomPadding = if (isWideLayout) 2.dp else 4.dp
    val dragOffset = remember { mutableStateOf(0.dp) }
    val isDragging = remember { mutableStateOf(false) }

    LaunchedEffect(isSheetVisible) {
        if (isSheetVisible) {
            shouldRenderSheet = shouldRenderAfterVisibilityChange(isSheetVisible = true)
            dragOffset.value = 0.dp
            isDragging.value = false
        } else {
            kotlinx.coroutines.delay(AnimationConstants.NAVIGATION_ANIMATION_DURATION.toLong())
            shouldRenderSheet = shouldRenderAfterVisibilityChange(isSheetVisible = false)
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
        isValidationPending.value = validationPendingAfterHandlingDismissalResult()
        when (result) {
            is DismissalResult.Allowed -> {
                showDiscardDialog.value = false
                isDismissalAllowedState.value = true
                viewModel.showBottomSheet(null)
            }

            is DismissalResult.ShowDiscardDialog -> {
                showDiscardDialog.value = shouldShowDiscardDialogForDismissalResult(result)
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
            showDiscardDialog.value = false
        }

        LaunchedEffect(isValidationPending.value, currentSheetScreen) {
            if (
                shouldRunDismissalAttempt(
                    isValidationPending = isValidationPending.value,
                    currentSheetScreen = currentSheetScreen,
                    targetScreen = screen
                )
            ) {
                onAttemptDismissal()
            }
        }
    }

    val onDismissRequestHandler: () -> Unit = {
        if (shouldTriggerDismissValidation(isDismissalAllowed = isDismissalAllowedState.value)) {
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

            val dismissSheetDescription = stringResource(R.string.cd_dismiss_sheet)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = backdropAlpha.value))
                    .semantics { contentDescription = dismissSheetDescription }
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
                    .sheetHeaderInsetPadding(isWideLayout = isWideLayout)
                    .pointerInput(isSheetVisible, isDismissalAllowedState.value, screenHeight) {
                        if (isSheetVisible && screenHeight > 0.dp) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    when (
                                        dragEndAction(
                                            dragOffset = dragOffset.value,
                                            dismissThreshold = dismissThreshold,
                                            isDismissalAllowed = isDismissalAllowedState.value
                                        )
                                    ) {
                                        DragEndAction.DismissSheet -> viewModel.showBottomSheet(null)
                                        DragEndAction.RequestValidation -> onDismissRequestHandler()
                                        DragEndAction.ResetDragOffset -> dragOffset.value = 0.dp
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
                    when (screen) {
                        is SheetScreen.SingleCounter -> {
                            val singleCounterViewModel =
                                hiltViewModel<SingleCounterViewModel>()

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
                            val doubleCounterViewModel =
                                hiltViewModel<DoubleCounterViewModel>()

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

                        is SheetScreen.RowAndRepeat -> {
                            val rowAndRepeatViewModel =
                                hiltViewModel<RowAndRepeatViewModel>()

                            LaunchedEffect(screen.projectId) {
                                rowAndRepeatViewModel.loadProject(screen.projectId)
                            }

                            SheetDismissalHandler(
                                screen = screen,
                                onAttemptDismissal = { rowAndRepeatViewModel.attemptDismissal() }
                            )

                            LaunchedEffect(screen) {
                                rowAndRepeatViewModel.dismissalResult.collect { result ->
                                    handleDismissalResult(result)
                                }
                            }
                        }

                        is SheetScreen.ProjectDetail -> {
                            val projectDetailViewModel =
                                hiltViewModel<ProjectDetailViewModel>()
                            val projectDetailUiState by projectDetailViewModel.uiState.collectAsStateWithLifecycle()

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
                                if (
                                    shouldAutoNavigateFromNewProject(
                                        screenProjectId = screen.projectId,
                                        lastObservedProjectId = lastObservedProjectId.value,
                                        currentProjectId = currentProjectId,
                                        initialProjectIdWhenCreatingNew = initialProjectIdWhenCreatingNew,
                                        hasNavigatedToCounter = hasNavigatedToCounter.value
                                    )
                                ) {
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

                            LaunchedEffect(screen) {
                                projectDetailViewModel.dismissalResult.collect { result ->
                                    handleDismissalResult(result)
                                }
                            }
                        }

                            SheetDismissalHandler(
                                screen = screen,
                                onAttemptDismissal = { createNoteViewModel.attemptDismissal() }
                            )

                            LaunchedEffect(screen) {
                                createNoteViewModel.dismissalResult.collect { result ->
                                    handleDismissalResult(result)
                                }
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
                                val dragHandleDescription = stringResource(R.string.cd_drag_handle)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            top = dragHandleTopPadding,
                                            bottom = dragHandleBottomPadding
                                        ),
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
                                            .semantics { contentDescription = dragHandleDescription }
                                    )
                                }
                                
                                Box(modifier = Modifier.fillMaxSize()) {
                                    when (currentScreen) {
                                    is SheetScreen.SingleCounter -> {
                                        val singleCounterViewModel =
                                            hiltViewModel<SingleCounterViewModel>()
                                        val saveBeforeNavigateScope = rememberCoroutineScope()
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            SingleCounterScreen(
                                                projectId = currentScreen.projectId,
                                                viewModel = singleCounterViewModel,
                                                isWideLayout = isWideLayout,
                                                onNavigateToDetail = { projectId ->
                                                    saveBeforeNavigateScope.launch {
                                                        singleCounterViewModel.ensureSaved()
                                                        viewModel.showBottomSheet(
                                                            SheetScreen.ProjectDetail(
                                                                projectId = projectId,
                                                                projectType = ProjectType.SINGLE
                                                            )
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }

                                    is SheetScreen.DoubleCounter -> {
                                        val doubleCounterViewModel =
                                            hiltViewModel<DoubleCounterViewModel>()
                                        val saveBeforeNavigateScope = rememberCoroutineScope()
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            DoubleCounterScreen(
                                                projectId = currentScreen.projectId,
                                                viewModel = doubleCounterViewModel,
                                                isWideLayout = isWideLayout,
                                                onNavigateToDetail = { projectId ->
                                                    saveBeforeNavigateScope.launch {
                                                        doubleCounterViewModel.ensureSaved()
                                                        viewModel.showBottomSheet(
                                                            SheetScreen.ProjectDetail(
                                                                projectId = projectId,
                                                                projectType = ProjectType.DOUBLE
                                                            )
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }

                                    is SheetScreen.RowAndRepeat -> {
                                        val rowAndRepeatViewModel =
                                            hiltViewModel<RowAndRepeatViewModel>()
                                        val saveBeforeNavigateScope = rememberCoroutineScope()
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            RowAndRepeat(
                                                projectId = currentScreen.projectId,
                                                viewModel = rowAndRepeatViewModel,
                                                isWideLayout = isWideLayout,
                                                onNavigateToDetail = { projectId ->
                                                    saveBeforeNavigateScope.launch {
                                                        rowAndRepeatViewModel.ensureSaved()
                                                        viewModel.showBottomSheet(
                                                            SheetScreen.ProjectDetail(
                                                                projectId = projectId,
                                                                projectType = ProjectType.ROW_AND_REPEAT
                                                            )
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }

                                    is SheetScreen.ProjectDetail -> {
                                        val projectDetailViewModel =
                                            hiltViewModel<ProjectDetailViewModel>()
                                        val projectDetailUiState by projectDetailViewModel.uiState.collectAsStateWithLifecycle()
                                        val saveBeforeNavigateScope = rememberCoroutineScope()
                                        ProjectDetailScreenContent(
                                            projectId = currentScreen.projectId,
                                            projectType = currentScreen.projectType,
                                            viewModel = projectDetailViewModel,
                                            isDiscardDialogManagedBySheet = true,
                                            showDiscardDialog = showDiscardDialog.value,
                                            onDismissDiscardDialog = { showDiscardDialog.value = false },
                                            onConfirmDiscard = {
                                                showDiscardDialog.value = false
                                                projectDetailViewModel.discardChanges()
                                                handleDismissalResult(DismissalResult.Allowed)
                                            },
                                            onNavigateBack = { projectId ->
                                                saveBeforeNavigateScope.launch {
                                                    if (projectDetailUiState.loadError == null) {
                                                        projectDetailViewModel.ensureSaved()
                                                    }
                                                    viewModel.showBottomSheet(
                                                        createSheetScreenForProjectType(
                                                            currentScreen.projectType,
                                                            projectId
                                                        )
                                                    )
                                                }
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