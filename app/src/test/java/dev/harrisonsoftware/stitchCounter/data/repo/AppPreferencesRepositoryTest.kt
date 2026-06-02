package dev.harrisonsoftware.stitchCounter.data.repo

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AppPreferencesRepositoryTest {

    private lateinit var repository: AppPreferencesRepository

    @Before
    fun setUp() {
        repository = AppPreferencesRepository(RuntimeEnvironment.getApplication())
    }

    @After
    fun tearDown() = runTest {
        repository.setForceDarkMode(false)
        repository.setForceLightMode(false)
        repository.setForceCounterScreensOn(false)
    }

    @Test
    fun `forceDarkMode defaults to false`() = runTest {
        assertFalse(repository.forceDarkMode.first())
    }

    @Test
    fun `forceLightMode defaults to false`() = runTest {
        assertFalse(repository.forceLightMode.first())
    }

    @Test
    fun `forceCounterScreensOn defaults to false`() = runTest {
        assertFalse(repository.forceCounterScreensOn.first())
    }

    @Test
    fun `setForceDarkMode true persists and disables force light mode`() = runTest {
        repository.setForceLightMode(true)
        repository.setForceDarkMode(true)

        assertTrue(repository.forceDarkMode.first())
        assertFalse(repository.forceLightMode.first())
    }

    @Test
    fun `setForceDarkMode false persists`() = runTest {
        repository.setForceDarkMode(true)
        repository.setForceDarkMode(false)

        assertFalse(repository.forceDarkMode.first())
    }

    @Test
    fun `setForceLightMode true persists and disables force dark mode`() = runTest {
        repository.setForceDarkMode(true)
        repository.setForceLightMode(true)

        assertTrue(repository.forceLightMode.first())
        assertFalse(repository.forceDarkMode.first())
    }

    @Test
    fun `setForceLightMode false persists`() = runTest {
        repository.setForceLightMode(true)
        repository.setForceLightMode(false)

        assertFalse(repository.forceLightMode.first())
    }

    @Test
    fun `setForceCounterScreensOn persists on and off`() = runTest {
        repository.setForceCounterScreensOn(true)
        assertTrue(repository.forceCounterScreensOn.first())

        repository.setForceCounterScreensOn(false)
        assertFalse(repository.forceCounterScreensOn.first())
    }
}
