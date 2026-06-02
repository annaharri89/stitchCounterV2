package dev.harrisonsoftware.stitchCounter.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_preferences"
)

@Singleton
class AppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // region Theme

    private val themeKey = stringPreferencesKey("selected_theme")

    val selectedTheme: Flow<AppTheme> = context.appDataStore.data.map { preferences ->
        val themeName = preferences[themeKey] ?: AppTheme.FOREST_FIBER.name
        try {
            AppTheme.valueOf(themeName)
        } catch (_: IllegalArgumentException) {
            AppTheme.FOREST_FIBER
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        context.appDataStore.edit { preferences ->
            preferences[themeKey] = theme.name
        }
    }

    // endregion

    // region Display

    private val forceDarkModeKey = booleanPreferencesKey("force_dark_mode")
    private val forceLightModeKey = booleanPreferencesKey("force_light_mode")
    private val forceCounterScreensOnKey = booleanPreferencesKey("force_counter_screens_on")

    val forceDarkMode: Flow<Boolean> = context.appDataStore.data.map { preferences ->
        preferences[forceDarkModeKey] ?: false
    }

    val forceLightMode: Flow<Boolean> = context.appDataStore.data.map { preferences ->
        preferences[forceLightModeKey] ?: false
    }

    val forceCounterScreensOn: Flow<Boolean> = context.appDataStore.data.map { preferences ->
        preferences[forceCounterScreensOnKey] ?: false
    }

    suspend fun setForceDarkMode(enabled: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[forceDarkModeKey] = enabled
            if (enabled) {
                preferences[forceLightModeKey] = false
            }
        }
    }

    suspend fun setForceLightMode(enabled: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[forceLightModeKey] = enabled
            if (enabled) {
                preferences[forceDarkModeKey] = false
            }
        }
    }

    suspend fun setForceCounterScreensOn(enabled: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[forceCounterScreensOnKey] = enabled
        }
    }

    // endregion

    // region Support

    private val hasSupportedKey = booleanPreferencesKey("has_supported_app")

    val hasSupported: Flow<Boolean> = context.appDataStore.data.map { preferences ->
        preferences[hasSupportedKey] ?: false
    }

    suspend fun setHasSupported(supported: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[hasSupportedKey] = supported
        }
    }

    // endregion

    // region Counter Tips

    private val hasSeenCustomAdjustmentTipKey = booleanPreferencesKey("has_seen_custom_adjustment_tip")

    suspend fun consumeShouldShowCustomAdjustmentTip(): Boolean {
        var shouldShowTip = false
        context.appDataStore.edit { preferences ->
            val hasSeenTip = preferences[hasSeenCustomAdjustmentTipKey] ?: false
            shouldShowTip = !hasSeenTip
            if (!hasSeenTip) {
                preferences[hasSeenCustomAdjustmentTipKey] = true
            }
        }
        return shouldShowTip
    }

    // endregion
}
