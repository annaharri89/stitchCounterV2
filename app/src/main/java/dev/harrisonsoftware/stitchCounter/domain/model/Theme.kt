package dev.harrisonsoftware.stitchCounter.domain.model

import androidx.annotation.StringRes
import dev.harrisonsoftware.stitchCounter.R

enum class AppTheme(@param:StringRes val displayName: Int) {
    DUSTY_ROSE(R.string.theme_name_dusty_rose),
    GOLDEN_HEARTH(R.string.theme_name_golden_hearth),
    SEA_COTTAGE(R.string.theme_name_sea_cottage),
    FOREST_FIBER(R.string.theme_name_forest_fiber),
    CLOUD_SOFT(R.string.theme_name_cloud_soft),
    YARN_CANDY(R.string.theme_name_yarn_candy)
}