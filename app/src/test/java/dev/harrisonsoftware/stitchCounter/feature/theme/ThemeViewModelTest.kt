package dev.harrisonsoftware.stitchCounter.feature.theme

import dev.harrisonsoftware.stitchCounter.data.repo.AppPreferencesRepository
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var appPreferencesRepository: AppPreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        appPreferencesRepository = mockk()
        every { appPreferencesRepository.selectedTheme } returns flowOf(AppTheme.FOREST_FIBER)
        every { appPreferencesRepository.forceDarkMode } returns flowOf(false)
        every { appPreferencesRepository.forceLightMode } returns flowOf(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init observes selected theme`() {
        every { appPreferencesRepository.selectedTheme } returns flowOf(AppTheme.SEA_COTTAGE)
        val viewModel = ThemeViewModel(appPreferencesRepository)

        assertEquals(AppTheme.SEA_COTTAGE, viewModel.uiState.value.selectedTheme)
    }

    @Test
    fun `init observes force dark mode`() {
        every { appPreferencesRepository.forceDarkMode } returns flowOf(true)
        val viewModel = ThemeViewModel(appPreferencesRepository)

        assertTrue(viewModel.uiState.value.forceDarkMode)
    }

    @Test
    fun `init observes force light mode`() {
        every { appPreferencesRepository.forceLightMode } returns flowOf(true)
        val viewModel = ThemeViewModel(appPreferencesRepository)

        assertTrue(viewModel.uiState.value.forceLightMode)
    }

    @Test
    fun `resolveDarkTheme uses forced dark mode when enabled`() {
        val themeUiState = ThemeUiState(forceDarkMode = true, forceLightMode = false)

        assertTrue(themeUiState.resolveDarkTheme(systemInDarkTheme = false))
    }

    @Test
    fun `resolveDarkTheme uses forced light mode when enabled`() {
        val themeUiState = ThemeUiState(forceDarkMode = false, forceLightMode = true)

        assertFalse(themeUiState.resolveDarkTheme(systemInDarkTheme = true))
    }

    @Test
    fun `resolveDarkTheme follows system when neither mode is forced`() {
        val themeUiState = ThemeUiState(forceDarkMode = false, forceLightMode = false)

        assertTrue(themeUiState.resolveDarkTheme(systemInDarkTheme = true))
        assertFalse(themeUiState.resolveDarkTheme(systemInDarkTheme = false))
    }
}
