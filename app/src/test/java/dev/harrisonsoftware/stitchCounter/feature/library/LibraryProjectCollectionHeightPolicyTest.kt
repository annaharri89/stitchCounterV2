package dev.harrisonsoftware.stitchCounter.feature.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryProjectCollectionHeightPolicyTest {

    @Test
    fun shouldEqualizeLibraryProjectHeights_isTrue_inLandscape() {
        assertTrue(shouldEqualizeLibraryProjectHeights(isLandscape = true))
    }

    @Test
    fun shouldEqualizeLibraryProjectHeights_isFalse_inPortrait() {
        assertFalse(shouldEqualizeLibraryProjectHeights(isLandscape = false))
    }

    @Test
    fun tallestLibraryProjectHeightRememberKey_tracksOnlyMultiSelectMode() {
        assertEquals(false, tallestLibraryProjectHeightRememberKey(isMultiSelectMode = false))
        assertEquals(true, tallestLibraryProjectHeightRememberKey(isMultiSelectMode = true))
    }
}
