package dev.harrisonsoftware.stitchCounter.feature.library

import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryProjectRowTapActionTest {

    @Test
    fun resolveProjectRowTapAction_returnsToggleSelection_whenMultiSelectModeEnabled() {
        val action = resolveProjectRowTapAction(
            isMultiSelectMode = true,
            isSwipeRevealed = false
        )

        assertEquals(ProjectRowTapAction.ToggleSelection, action)
    }

    @Test
    fun resolveProjectRowTapAction_returnsResetSwipeState_whenSwipeIsRevealed() {
        val action = resolveProjectRowTapAction(
            isMultiSelectMode = false,
            isSwipeRevealed = true
        )

        assertEquals(ProjectRowTapAction.ResetSwipeState, action)
    }

    @Test
    fun resolveProjectRowTapAction_returnsOpenProject_whenSingleSelectAndSwipeHidden() {
        val action = resolveProjectRowTapAction(
            isMultiSelectMode = false,
            isSwipeRevealed = false
        )

        assertEquals(ProjectRowTapAction.OpenProject, action)
    }
}
