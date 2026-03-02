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
        val themeName = preferences[themeKey] ?: AppTheme.DUSTY_ROSE.name
        try {
            AppTheme.valueOf(themeName)
        } catch (_: IllegalArgumentException) {
            AppTheme.DUSTY_ROSE
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        context.appDataStore.edit { preferences ->
            preferences[themeKey] = theme.name
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
