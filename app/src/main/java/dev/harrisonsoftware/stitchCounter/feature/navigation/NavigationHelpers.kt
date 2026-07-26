package dev.harrisonsoftware.stitchCounter.feature.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavHostController
import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.feature.destinations.LibraryScreenDestination
import dev.harrisonsoftware.stitchCounter.feature.destinations.NotesScreenDestination
import dev.harrisonsoftware.stitchCounter.feature.destinations.SettingsScreenDestination
import dev.harrisonsoftware.stitchCounter.feature.destinations.StatsScreenDestination
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.Direction

fun getDestinationForTab(tab: BottomNavTab): Direction = when(tab) {
    BottomNavTab.LIBRARY -> LibraryScreenDestination
    BottomNavTab.NOTES -> NotesScreenDestination
    BottomNavTab.STATS -> StatsScreenDestination
    BottomNavTab.SETTINGS -> SettingsScreenDestination
}

fun getTabForRoute(route: String?): BottomNavTab = when {
    route == null -> BottomNavTab.LIBRARY
    route.startsWith(LibraryScreenDestination.route) -> BottomNavTab.LIBRARY
    route.startsWith(NotesScreenDestination.route) -> BottomNavTab.NOTES
    route.startsWith(StatsScreenDestination.route) -> BottomNavTab.STATS
    route.startsWith(SettingsScreenDestination.route) -> BottomNavTab.SETTINGS
    else -> BottomNavTab.LIBRARY
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
    ProjectType.ROW_AND_REPEAT -> SheetScreen.RowAndRepeat(projectId)
    ProjectType.UNKNOWN -> SheetScreen.ProjectDetail(projectId = projectId, projectType = ProjectType.UNKNOWN)
}

fun isCreateNoteSheet(sheet: SheetScreen?): Boolean =
    sheet is SheetScreen.CreateNote && sheet.noteId == null

fun createNoteSheetForNoteId(noteId: Int?): SheetScreen.CreateNote =
    SheetScreen.CreateNote(noteId = noteId)

fun rootNavigationContentPadding(
    isWideLayout: Boolean,
    innerPadding: PaddingValues
): PaddingValues {
    return if (isWideLayout) {
        PaddingValues(
            bottom = innerPadding.calculateBottomPadding(),
            start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
            end = innerPadding.calculateRightPadding(LayoutDirection.Ltr),
        )
    } else {
        innerPadding
    }
}
