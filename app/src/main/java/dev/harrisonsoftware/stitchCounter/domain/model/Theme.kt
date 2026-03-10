package dev.harrisonsoftware.stitchCounter.domain.model

import androidx.annotation.StringRes
import dev.harrisonsoftware.stitchCounter.R

enum class AppTheme(@param:StringRes val displayName: Int) {
    FOREST_FIBER(R.string.theme_name_forest_fiber),
    SEA_COTTAGE(R.string.theme_name_sea_cottage),
    GOLDEN_HEARTH(R.string.theme_name_golden_hearth),
    DUSTY_ROSE(R.string.theme_name_dusty_rose),
    CLOUD_SOFT(R.string.theme_name_cloud_soft),
    YARN_CANDY(R.string.theme_name_yarn_candy)
}