package com.example.stitchcounterv3.feature.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.stitchcounterv3.domain.model.DismissalResult
import com.example.stitchcounterv3.feature.NavGraphs
import com.example.stitchcounterv3.feature.destinations.SettingsScreenDestination
import com.example.stitchcounterv3.feature.doublecounter.DoubleCounterScreen
import com.example.stitchcounterv3.feature.singleCounter.SingleCounterScreen
import com.example.stitchcounterv3.feature.projectDetail.ProjectDetailContent
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navigate
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialNavigationApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun RootNavigationScreen(viewModel: RootNavigationViewModel) {

    val selectedTab = viewModel.selectedTab.collectAsStateWithLifecycle().value
    val configuration = LocalConfiguration.current
    val isCompact = configuration.screenWidthDp < 600

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val currentSheetScreen by viewModel.currentSheet.collectAsStateWithLifecycle()
    
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

    var previousTab by remember { mutableStateOf<BottomNavTab?>(null) }
    LaunchedEffect(selectedTab) {
        if (selectedTab == BottomNavTab.SETTINGS && previousTab != BottomNavTab.SETTINGS) {
            navController.navigate(SettingsScreenDestination) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
        previousTab = selectedTab
    }

    Scaffold(
        bottomBar = {
            if (isCompact) {
                BottomNavigationLayout(
                    selectedTab = selectedTab,
                    onTabSelected = viewModel::selectTab,
                    navController = navController
                )
            }
        }
    ) { innerPadding ->
        Row(modifier = Modifier.padding(innerPadding)) {
            if (!isCompact) {
                NavigationRailLayout(
                    selectedTab = selectedTab,
                    onTabSelected = viewModel::selectTab,
                    navController = navController
                )
            }

            DestinationsNavHost(
                navController = navController,
                navGraph = NavGraphs.root,
                engine = rememberAnimatedNavHostEngine(),
                dependenciesContainerBuilder = {
                    dependency(NavGraphs.root) { viewModel }
                }
            )
        }
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
        var showDiscardDialog by remember { mutableStateOf(false) }
        
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
                                showDiscardDialog = true
                            }
                        }
                    }
                }
                
                var hasNavigatedToCounter by remember(screen) { mutableStateOf(false) }
                var lastObservedProjectId by remember(screen) { mutableStateOf<Int?>(null) }
                var initialProjectIdWhenCreatingNew by remember(screen) { mutableStateOf<Int?>(null) }
                
                LaunchedEffect(screen.projectId) {
                    if (screen.projectId == null) {
                        hasNavigatedToCounter = false
                        lastObservedProjectId = null
                        initialProjectIdWhenCreatingNew = projectDetailUiState.project?.id
                    }
                }
                
                LaunchedEffect(projectDetailUiState.project?.id) {
                    val currentProjectId = projectDetailUiState.project?.id
                    
                    val wasNewProject = lastObservedProjectId == null || lastObservedProjectId == 0
                    val isNowSaved = currentProjectId != null && currentProjectId > 0
                    val isNewProjectScreen = screen.projectId == null
                    val isProjectIdChanged = lastObservedProjectId != currentProjectId
                    val isNotStaleProjectId = initialProjectIdWhenCreatingNew == null || 
                        currentProjectId == null || 
                        currentProjectId == 0 || 
                        currentProjectId != initialProjectIdWhenCreatingNew
                    
                    if (isNewProjectScreen && wasNewProject && isNowSaved && isProjectIdChanged && isNotStaleProjectId && !hasNavigatedToCounter) {
                        hasNavigatedToCounter = true
                        
                        when (screen.projectType) {
                            com.example.stitchcounterv3.domain.model.ProjectType.SINGLE -> {
                                viewModel.showBottomSheet(SheetScreen.SingleCounter(currentProjectId))
                            }
                            com.example.stitchcounterv3.domain.model.ProjectType.DOUBLE -> {
                                viewModel.showBottomSheet(SheetScreen.DoubleCounter(currentProjectId))
                            }
                        }
                    }
                    
                    lastObservedProjectId = currentProjectId
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
                    val isGoingFromDetail = initialState is SheetScreen.ProjectDetail && targetState !is SheetScreen.ProjectDetail
                    
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
                                        projectType = com.example.stitchcounterv3.domain.model.ProjectType.SINGLE
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
                                        projectType = com.example.stitchcounterv3.domain.model.ProjectType.DOUBLE
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
                            showDiscardDialog = showDiscardDialog,
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
                                when (projectDetailUiState.project?.type) {
                                    com.example.stitchcounterv3.domain.model.ProjectType.SINGLE -> {
                                        viewModel.showBottomSheet(SheetScreen.SingleCounter(projectId))
                                    }
                                    com.example.stitchcounterv3.domain.model.ProjectType.DOUBLE -> {
                                        viewModel.showBottomSheet(SheetScreen.DoubleCounter(projectId))
                                    }
                                    null -> {}
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
                } catch (e: Exception) {
                }
            }
        }
    }
}

sealed class SheetScreen {
    data class SingleCounter(val projectId: Int? = null) : SheetScreen()
    data class DoubleCounter(val projectId: Int? = null) : SheetScreen()
    data class ProjectDetail(val projectId: Int? = null, val projectType: com.example.stitchcounterv3.domain.model.ProjectType) : SheetScreen()
}