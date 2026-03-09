package dev.harrisonsoftware.stitchCounter.feature.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
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
    val navController = rememberNavController()
    val currentSheetScreen by viewModel.currentSheet.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedTab = getTabForRoute(navBackStackEntry?.destination?.route)

    Scaffold(
        bottomBar = {
            if (!isWideLayout) {
                BottomNavigationLayout(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        if (tab != selectedTab) {
                            navigateToDestination(navController, getDestinationForTab(tab))
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Row(modifier = Modifier.padding(innerPadding)) {
            if (isWideLayout) {
                NavigationRailLayout(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        if (tab != selectedTab) {
                            navigateToDestination(navController, getDestinationForTab(tab))
                        }
                    }
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