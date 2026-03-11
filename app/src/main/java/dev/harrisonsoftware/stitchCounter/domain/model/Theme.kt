package dev.harrisonsoftware.stitchCounter.domain.model

import androidx.annotation.StringRes
import androidx.annotation.DrawableRes
import dev.harrisonsoftware.stitchCounter.R

enum class AppTheme(
    @param:StringRes val displayName: Int,
    @param:DrawableRes val previewDrawableRes: Int
) {
    FOREST_FIBER(R.string.theme_name_forest_fiber, R.drawable.theme_preview_forest_fiber),
    SEA_COTTAGE(R.string.theme_name_sea_cottage, R.drawable.theme_preview_sea_cottage),
    GOLDEN_HEARTH(R.string.theme_name_golden_hearth, R.drawable.theme_preview_golden_hearth),
    DUSTY_ROSE(R.string.theme_name_dusty_rose, R.drawable.theme_preview_dusty_rose),
    CLOUD_SOFT(R.string.theme_name_cloud_soft, R.drawable.theme_preview_cloud_soft),
    YARN_CANDY(R.string.theme_name_yarn_candy, R.drawable.theme_preview_yarn_candy)
}
