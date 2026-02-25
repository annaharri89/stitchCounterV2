package dev.harrisonsoftware.stitchCounter.feature.theme

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme
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

    private val themeToComponentName = mapOf(
        AppTheme.SEA_COTTAGE to ComponentName(packageName, "$packageName.SeaCottageLauncherAlias"),
        AppTheme.GOLDEN_HEARTH to ComponentName(packageName, "$packageName.GoldenHearthLauncherAlias"),
        AppTheme.FOREST_FIBER to ComponentName(packageName, "$packageName.ForestFiberLauncherAlias"),
        AppTheme.CLOUD_SOFT to ComponentName(packageName, "$packageName.CloudSoftLauncherAlias"),
        AppTheme.YARN_CANDY to ComponentName(packageName, "$packageName.YarnCandyLauncherAlias"),
        AppTheme.DUSTY_ROSE to ComponentName(packageName, "$packageName.DustyRoseLauncherAlias")
    )

    @Volatile
    var pendingTheme: AppTheme? = null

    fun applyPendingIconChangeIfNeeded() {
        pendingTheme?.let { theme ->
            updateLauncherIcon(theme)
            pendingTheme = null
        }
    }

    fun updateLauncherIcon(theme: AppTheme) {
        val targetComponent = themeToComponentName[theme]
            ?: return // Unknown theme, do nothing

        // Avoid unnecessary component toggling when the right launcher alias is already active.
        if (isTargetAliasAlreadyActive(targetComponent)) {
            return
        }

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

    private fun isTargetAliasAlreadyActive(targetComponent: ComponentName): Boolean {
        return try {
            themeToComponentName.values.all { componentName ->
                val enabledState = packageManager.getComponentEnabledSetting(componentName)
                when (componentName) {
                    targetComponent -> enabledState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    else -> enabledState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                        enabledState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                }
            }
        } catch (_: Exception) {
            false
        }
    }
}
