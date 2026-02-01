package com.example.stitchcounterv3.feature.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.stitchcounterv3.feature.NavGraphs
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.dependency


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialNavigationApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun RootNavigationScreen(viewModel: RootNavigationViewModel) {
    val selectedTab = viewModel.selectedTab.collectAsStateWithLifecycle().value
    val configuration = LocalConfiguration.current
    val isCompact = configuration.screenWidthDp < 600
    val navController = rememberNavController()
    val currentSheetScreen by viewModel.currentSheet.collectAsStateWithLifecycle()

    var previousTab by remember { mutableStateOf<BottomNavTab?>(null) }
    LaunchedEffect(selectedTab) {
        if (selectedTab == BottomNavTab.SETTINGS && previousTab != BottomNavTab.SETTINGS) {
            navigateToDestination(navController, getDestinationForTab(selectedTab))
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

    BottomSheetManager(
        currentSheetScreen = currentSheetScreen,
        viewModel = viewModel,
        onDismissalResult = { }
    )
}