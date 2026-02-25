package dev.harrisonsoftware.stitchCounter.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
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
    /**
     * Reactive stream of selected theme. Emits current theme and updates when changed.
     */
    val selectedTheme: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        val themeName = preferences[themeKey] ?: AppTheme.DUSTY_ROSE.name
        try {
            AppTheme.valueOf(themeName)
        } catch (_: IllegalArgumentException) {
            AppTheme.DUSTY_ROSE
        }
    }

    /**
     * Saves selected theme to persistent storage
     */
    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = theme.name
        }
    }
}