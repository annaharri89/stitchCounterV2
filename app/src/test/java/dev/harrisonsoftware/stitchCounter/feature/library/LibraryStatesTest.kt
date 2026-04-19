package dev.harrisonsoftware.stitchCounter.feature.library

import dev.harrisonsoftware.stitchCounter.R
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryStatesTest {

    @Test
    fun `deleteDialogTitleRes returns singular title for one project`() {
        assertEquals(R.string.delete_project_title, deleteDialogTitleRes(projectCount = 1))
    }

    @Test
    fun `deleteDialogTitleRes returns plural title for multiple projects`() {
        assertEquals(R.string.delete_projects_title, deleteDialogTitleRes(projectCount = 3))
    }

    @Test
    fun `deleteDialogMessageRes returns singular message for one project`() {
        assertEquals(R.string.delete_project_message, deleteDialogMessageRes(projectCount = 1))
    }

    @Test
    fun `deleteDialogMessageRes returns plural message for multiple projects`() {
        assertEquals(R.string.delete_projects_message, deleteDialogMessageRes(projectCount = 2))
    }

    @Test
    fun `loading and empty state accessibility resources are stable`() {
        assertEquals(R.string.cd_loading, loadingIndicatorDescriptionRes())
        assertEquals(R.string.cd_empty_library, emptyLibraryDescriptionRes())
        assertEquals(R.string.library_empty_title, emptyLibraryTitleRes())
        assertEquals(R.string.library_empty_message, emptyLibraryMessageRes())
    }
}
