package dev.harrisonsoftware.stitchCounter.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.supportDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "support_preferences"
)

@Singleton
class SupportAppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val hasSupportedKey = booleanPreferencesKey("has_supported_app")

    val hasSupported: Flow<Boolean> = context.supportDataStore.data.map { preferences ->
        preferences[hasSupportedKey] ?: false
    }

    suspend fun setHasSupported(supported: Boolean) {
        context.supportDataStore.edit { preferences ->
            preferences[hasSupportedKey] = supported
        }
    }
}
