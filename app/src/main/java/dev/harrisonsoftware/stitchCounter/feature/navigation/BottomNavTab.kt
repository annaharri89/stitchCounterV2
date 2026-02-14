package dev.harrisonsoftware.stitchCounter.feature.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import dev.harrisonsoftware.stitchCounter.R

enum class BottomNavTab(
    @StringRes val titleResId: Int,
    val icon: ImageVector
) {
    LIBRARY(R.string.nav_library, Icons.Default.List),
    SETTINGS(R.string.nav_settings, Icons.Default.Settings)
}
