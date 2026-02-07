package dev.harrisonsoftware.stitchCounter.feature.navigation

import androidx.navigation.NavHostController
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.feature.destinations.LibraryScreenDestination
import dev.harrisonsoftware.stitchCounter.feature.destinations.MainScreenDestination
import dev.harrisonsoftware.stitchCounter.feature.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.Direction

fun getDestinationForTab(tab: BottomNavTab): Direction = when(tab) {
    BottomNavTab.HOME -> MainScreenDestination
    BottomNavTab.LIBRARY -> LibraryScreenDestination
    BottomNavTab.SETTINGS -> SettingsScreenDestination
}

inline fun <reified T : Direction> navigateToDestination(
    navController: NavHostController,
    destination: T
) {
    navController.navigate(destination) {
        popUpTo(navController.graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun createSheetScreenForProjectType(projectType: ProjectType, projectId: Int?): SheetScreen = when (projectType) {
    ProjectType.SINGLE -> SheetScreen.SingleCounter(projectId)
    ProjectType.DOUBLE -> SheetScreen.DoubleCounter(projectId)
}
