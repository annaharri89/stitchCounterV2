package dev.harrisonsoftware.stitchCounter.feature.navigation

import dev.harrisonsoftware.stitchCounter.feature.destinations.LibraryScreenDestination
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
}
