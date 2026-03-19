package dev.harrisonsoftware.stitchCounter.feature.theme

import android.content.Context
import android.content.pm.PackageManager
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme
import dev.harrisonsoftware.stitchCounter.logging.AppLogger
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LauncherIconManagerTest {

    private lateinit var packageManager: PackageManager
    private lateinit var context: Context
    private lateinit var appLogger: AppLogger

    @Before
    fun setUp() {
        packageManager = mockk(relaxed = true)
        context = mockk(relaxed = true)
        appLogger = mockk(relaxed = true)
        every { context.packageManager } returns packageManager
        every { context.packageName } returns "dev.harrisonsoftware.stitchCounter"
    }

    @Test
    fun `applyPendingIconChangeIfNeeded skips once when external launch guard is set`() {
        val launcherIconManager = LauncherIconManager(context, appLogger)
        launcherIconManager.pendingTheme = AppTheme.SEA_COTTAGE
        launcherIconManager.skipNextPendingIconApply()

        launcherIconManager.applyPendingIconChangeIfNeeded()

        verify(exactly = 0) { packageManager.setComponentEnabledSetting(any(), any(), any()) }
        assertEquals(AppTheme.SEA_COTTAGE, launcherIconManager.pendingTheme)
    }

    @Test
    fun `applyPendingIconChangeIfNeeded applies pending theme after one skipped stop`() {
        every { packageManager.getComponentEnabledSetting(any()) } returns
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT

        val launcherIconManager = LauncherIconManager(context, appLogger)
        launcherIconManager.pendingTheme = AppTheme.GOLDEN_HEARTH
        launcherIconManager.skipNextPendingIconApply()

        launcherIconManager.applyPendingIconChangeIfNeeded()
        launcherIconManager.applyPendingIconChangeIfNeeded()

        verify(atLeast = 1) { packageManager.setComponentEnabledSetting(any(), any(), any()) }
        assertNull(launcherIconManager.pendingTheme)
    }
}
