package com.example.stitchcounterv3.feature.theme

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.example.stitchcounterv3.domain.model.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages dynamic switching of launcher icons based on the selected theme.
 * Uses activity aliases to provide different launcher icons for each theme.
 */
@Singleton
class LauncherIconManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager: PackageManager = context.packageManager
    private val packageName: String = context.packageName

    // Map themes to their corresponding activity alias component names
    private val themeToComponentName = mapOf(
        AppTheme.SEA_COTTAGE to ComponentName(packageName, "$packageName.SeaCottageLauncherAlias"),
        AppTheme.RETRO_SUMMER to ComponentName(packageName, "$packageName.RetroSummerLauncherAlias"),
        AppTheme.PURPLE to ComponentName(packageName, "$packageName.PurpleLauncherAlias")
    )

    /**
     * Updates the launcher icon to match the selected theme.
     * Disables all other theme aliases and enables the one for the given theme.
     *
     * @param theme The theme to set the launcher icon for
     */
    fun updateLauncherIcon(theme: AppTheme) {
        val targetComponent = themeToComponentName[theme]
            ?: return // Unknown theme, do nothing

        // Disable all aliases first
        themeToComponentName.values.forEach { componentName ->
            try {
                packageManager.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            } catch (e: Exception) {
                // Component might not exist or permission issue - ignore
            }
        }

        // Enable the target alias
        try {
            packageManager.setComponentEnabledSetting(
                targetComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) {
            // Component might not exist or permission issue - ignore
        }
    }

    /**
     * Gets the currently enabled launcher icon theme by checking which alias is enabled.
     *
     * @return The currently enabled theme, or SEA_COTTAGE as default if none found
     */
    fun getCurrentLauncherIconTheme(): AppTheme {
        return themeToComponentName.entries.firstOrNull { (_, componentName) ->
            try {
                val state = packageManager.getComponentEnabledSetting(componentName)
                state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                        state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
            } catch (e: Exception) {
                false
            }
        }?.key ?: AppTheme.SEA_COTTAGE
    }
}
