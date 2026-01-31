package com.example.stitchcounterv3.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.stitchcounterv3.domain.model.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

/**
 * Repository for persisting and retrieving theme preferences using DataStore.
 * 
 * Flow: User selects theme → setTheme() → DataStore saves → selectedTheme Flow emits → UI updates
 */
@Singleton
class ThemePreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val themeKey = stringPreferencesKey("selected_theme")
    private val shouldNavigateToSettingsKey = booleanPreferencesKey("should_navigate_to_settings")

    /**
     * Reactive stream of selected theme. Emits current theme and updates when changed.
     */
    val selectedTheme: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        val themeName = preferences[themeKey] ?: AppTheme.SEA_COTTAGE.name
        AppTheme.valueOf(themeName)
    }

    /**
     * Saves selected theme to persistent storage
     */
    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = theme.name
        }
    }

    /**
     * Sets the flag indicating that the app should navigate to settings on next launch
     */
    suspend fun setShouldNavigateToSettings(shouldNavigate: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[shouldNavigateToSettingsKey] = shouldNavigate
        }
    }

    /**
     * Checks if the app should navigate to settings and clears the flag
     */
    suspend fun checkAndClearShouldNavigateToSettings(): Boolean {
        val shouldNavigate = context.dataStore.data.first()[shouldNavigateToSettingsKey] ?: false
        if (shouldNavigate) {
            context.dataStore.edit { preferences ->
                preferences.remove(shouldNavigateToSettingsKey)
            }
        }
        return shouldNavigate
    }
}