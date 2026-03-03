package dev.harrisonsoftware.stitchCounter.feature.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import dev.harrisonsoftware.stitchCounter.feature.NavGraphs
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.dependency


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialNavigationApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun RootNavigationScreen(
    viewModel: RootNavigationViewModel,
    isWideLayout: Boolean
) {
    val selectedTab = viewModel.selectedTab.collectAsStateWithLifecycle().value
    val navController = rememberNavController()
    val currentSheetScreen by viewModel.currentSheet.collectAsStateWithLifecycle()

    val previousTab = remember { mutableStateOf<BottomNavTab?>(null) }
    LaunchedEffect(selectedTab) {
        if (previousTab.value != null && selectedTab != previousTab.value) {
            navigateToDestination(navController, getDestinationForTab(selectedTab))
        }
        previousTab.value = selectedTab
    }

    Scaffold(
        bottomBar = {
            if (!isWideLayout) {
                BottomNavigationLayout(
                    selectedTab = selectedTab,
                    onTabSelected = viewModel::selectTab
                )
            }
        }
    ) { innerPadding ->
        Row(modifier = Modifier.padding(innerPadding)) {
            if (isWideLayout) {
                NavigationRailLayout(
                    selectedTab = selectedTab,
                    onTabSelected = viewModel::selectTab
                )
            }

            DestinationsNavHost(
                navController = navController,
                navGraph = NavGraphs.root,
                engine = rememberAnimatedNavHostEngine(),
                dependenciesContainerBuilder = {
                    dependency(NavGraphs.root) { viewModel }
                    dependency(NavGraphs.root) { isWideLayout }
                }
            )
        }
    }

    BottomSheetManager(
        currentSheetScreen = currentSheetScreen,
        viewModel = viewModel,
        isWideLayout = isWideLayout,
        onDismissalResult = { }
    )
}