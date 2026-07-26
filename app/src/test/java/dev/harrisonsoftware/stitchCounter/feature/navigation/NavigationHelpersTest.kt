package dev.harrisonsoftware.stitchCounter.feature.navigation

import dev.harrisonsoftware.stitchCounter.domain.model.ProjectType
import dev.harrisonsoftware.stitchCounter.feature.destinations.LibraryScreenDestination
import dev.harrisonsoftware.stitchCounter.feature.destinations.NotesScreenDestination
import dev.harrisonsoftware.stitchCounter.feature.destinations.SettingsScreenDestination
import dev.harrisonsoftware.stitchCounter.feature.destinations.StatsScreenDestination
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationHelpersTest {

    @Test
    fun `getTabForRoute returns library when route is null`() {
        assertEquals(BottomNavTab.LIBRARY, getTabForRoute(null))
    }

    @Test
    fun `getTabForRoute returns library tab for library route`() {
        assertEquals(BottomNavTab.LIBRARY, getTabForRoute(LibraryScreenDestination.route))
    }

    @Test
    fun `getTabForRoute returns notes tab for notes route`() {
        assertEquals(BottomNavTab.NOTES, getTabForRoute(NotesScreenDestination.route))
    }

    @Test
    fun `getDestinationForTab returns notes destination for notes tab`() {
        assertEquals(NotesScreenDestination, getDestinationForTab(BottomNavTab.NOTES))
    }

    @Test
    fun `BottomNavTab order places notes between library and stats`() {
        val tabs = BottomNavTab.entries
        assertEquals(BottomNavTab.LIBRARY, tabs[0])
        assertEquals(BottomNavTab.NOTES, tabs[1])
        assertEquals(BottomNavTab.STATS, tabs[2])
        assertEquals(BottomNavTab.SETTINGS, tabs[3])
    }

    @Test
    fun `getTabForRoute returns stats tab for stats route`() {
        assertEquals(BottomNavTab.STATS, getTabForRoute(StatsScreenDestination.route))
    }

    @Test
    fun `getTabForRoute returns settings tab for settings route`() {
        assertEquals(BottomNavTab.SETTINGS, getTabForRoute(SettingsScreenDestination.route))
    }

    @Test
    fun `getTabForRoute returns library for unknown route`() {
        assertEquals(BottomNavTab.LIBRARY, getTabForRoute("unknown_route"))
    }

    @Test
    fun `createSheetScreenForProjectType maps single counter`() {
        val screen = createSheetScreenForProjectType(ProjectType.SINGLE, projectId = 5)
        assertEquals(SheetScreen.SingleCounter(projectId = 5), screen)
    }

    @Test
    fun `createSheetScreenForProjectType maps double counter`() {
        val screen = createSheetScreenForProjectType(ProjectType.DOUBLE, projectId = 5)
        assertEquals(SheetScreen.DoubleCounter(projectId = 5), screen)
    }

    @Test
    fun `createSheetScreenForProjectType maps row and repeat`() {
        val screen = createSheetScreenForProjectType(ProjectType.ROW_AND_REPEAT, projectId = 5)
        assertEquals(SheetScreen.RowAndRepeat(projectId = 5), screen)
    }

    @Test
    fun `createSheetScreenForProjectType maps unknown to project detail`() {
        val screen = createSheetScreenForProjectType(ProjectType.UNKNOWN, projectId = 5)
        assertEquals(SheetScreen.ProjectDetail(projectId = 5, projectType = ProjectType.UNKNOWN), screen)
    }


    @Test
    fun `isCreateNoteSheet returns true for create sheet without note id`() {
        assertEquals(true, isCreateNoteSheet(SheetScreen.CreateNote()))
    }

    @Test
    fun `isCreateNoteSheet returns false for edit sheet with note id`() {
        assertEquals(false, isCreateNoteSheet(SheetScreen.CreateNote(noteId = 4)))
    }

    @Test
    fun `createNoteSheetForNoteId builds create sheet when note id is null`() {
        assertEquals(SheetScreen.CreateNote(), createNoteSheetForNoteId(noteId = null))
    }

    @Test
    fun `createNoteSheetForNoteId builds edit sheet when note id is provided`() {
        assertEquals(SheetScreen.CreateNote(noteId = 8), createNoteSheetForNoteId(noteId = 8))
    }

}
