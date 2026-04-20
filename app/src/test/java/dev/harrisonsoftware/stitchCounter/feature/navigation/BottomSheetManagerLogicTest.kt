package dev.harrisonsoftware.stitchCounter.feature.navigation

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomSheetManagerLogicTest {

    @Test
    fun `shouldRenderAfterVisibilityChange mirrors sheet visibility`() {
        assertTrue(shouldRenderAfterVisibilityChange(isSheetVisible = true))
        assertFalse(shouldRenderAfterVisibilityChange(isSheetVisible = false))
    }

    @Test
    fun `shouldTriggerDismissValidation returns true only when dismissal is not allowed`() {
        assertTrue(shouldTriggerDismissValidation(isDismissalAllowed = false))
        assertFalse(shouldTriggerDismissValidation(isDismissalAllowed = true))
    }

    @Test
    fun `shouldRunDismissalAttempt requires pending validation and matching screen`() {
        val targetScreen = SheetScreen.SingleCounter(projectId = 10)

        assertTrue(
            shouldRunDismissalAttempt(
                isValidationPending = true,
                currentSheetScreen = targetScreen,
                targetScreen = targetScreen
            )
        )
        assertFalse(
            shouldRunDismissalAttempt(
                isValidationPending = false,
                currentSheetScreen = targetScreen,
                targetScreen = targetScreen
            )
        )
        assertFalse(
            shouldRunDismissalAttempt(
                isValidationPending = true,
                currentSheetScreen = SheetScreen.DoubleCounter(projectId = 10),
                targetScreen = targetScreen
            )
        )
    }

    @Test
    fun `dragEndAction returns reset when drag does not exceed threshold`() {
        val action = dragEndAction(
            dragOffset = 80.dp,
            dismissThreshold = 100.dp,
            isDismissalAllowed = false
        )

        assertEquals(DragEndAction.ResetDragOffset, action)
    }

    @Test
    fun `dragEndAction returns reset when drag offset equals dismiss threshold`() {
        val action = dragEndAction(
            dragOffset = 100.dp,
            dismissThreshold = 100.dp,
            isDismissalAllowed = true
        )

        assertEquals(DragEndAction.ResetDragOffset, action)
    }

    @Test
    fun `dragEndAction returns request validation when drag exceeds threshold and dismissal not allowed`() {
        val action = dragEndAction(
            dragOffset = 140.dp,
            dismissThreshold = 100.dp,
            isDismissalAllowed = false
        )

        assertEquals(DragEndAction.RequestValidation, action)
    }

    @Test
    fun `dragEndAction returns dismiss when drag exceeds threshold and dismissal is allowed`() {
        val action = dragEndAction(
            dragOffset = 140.dp,
            dismissThreshold = 100.dp,
            isDismissalAllowed = true
        )

        assertEquals(DragEndAction.DismissSheet, action)
    }

    @Test
    fun `shouldAutoNavigateFromNewProject returns true for first save transition on new project screen`() {
        assertTrue(
            shouldAutoNavigateFromNewProject(
                screenProjectId = null,
                lastObservedProjectId = null,
                currentProjectId = 42,
                initialProjectIdWhenCreatingNew = null,
                hasNavigatedToCounter = false
            )
        )
    }

    @Test
    fun `shouldAutoNavigateFromNewProject returns false when already navigated`() {
        assertFalse(
            shouldAutoNavigateFromNewProject(
                screenProjectId = null,
                lastObservedProjectId = null,
                currentProjectId = 42,
                initialProjectIdWhenCreatingNew = null,
                hasNavigatedToCounter = true
            )
        )
    }

    @Test
    fun `shouldAutoNavigateFromNewProject returns false for existing project screen`() {
        assertFalse(
            shouldAutoNavigateFromNewProject(
                screenProjectId = 5,
                lastObservedProjectId = 5,
                currentProjectId = 5,
                initialProjectIdWhenCreatingNew = null,
                hasNavigatedToCounter = false
            )
        )
    }

    @Test
    fun `shouldAutoNavigateFromNewProject returns false when project id is stale initial value`() {
        assertFalse(
            shouldAutoNavigateFromNewProject(
                screenProjectId = null,
                lastObservedProjectId = null,
                currentProjectId = 11,
                initialProjectIdWhenCreatingNew = 11,
                hasNavigatedToCounter = false
            )
        )
    }

    @Test
    fun `shouldAutoNavigateFromNewProject treats last observed id zero as unsaved placeholder`() {
        assertTrue(
            shouldAutoNavigateFromNewProject(
                screenProjectId = null,
                lastObservedProjectId = 0,
                currentProjectId = 9,
                initialProjectIdWhenCreatingNew = null,
                hasNavigatedToCounter = false
            )
        )
    }

    @Test
    fun `shouldAutoNavigateFromNewProject returns true when saved id replaces initial placeholder`() {
        assertTrue(
            shouldAutoNavigateFromNewProject(
                screenProjectId = null,
                lastObservedProjectId = null,
                currentProjectId = 42,
                initialProjectIdWhenCreatingNew = 5,
                hasNavigatedToCounter = false
            )
        )
    }
}
