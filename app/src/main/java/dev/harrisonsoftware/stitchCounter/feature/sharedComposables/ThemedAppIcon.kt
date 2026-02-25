package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme

@Composable
fun ThemedAppIcon(
    theme: AppTheme,
    modifier: Modifier = Modifier,
    iconCornerRadius: Dp = 12.dp,
    isSelected: Boolean = false
) {
    val backgroundResourceId = theme.launcherBackgroundRes()
    val foregroundResourceId = theme.foregroundIconRes()
    val themeIconDescription = stringResource(R.string.cd_theme_icon, stringResource(theme.displayName))
    val iconShape = RoundedCornerShape(iconCornerRadius)

    Box(
        modifier = modifier
            .then(
                if (isSelected) Modifier.shadow(
                    elevation = 12.dp,
                    shape = iconShape,
                    ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ) else Modifier
            )
            .clip(iconShape)
            .semantics { contentDescription = themeIconDescription }
    ) {
        Image(
            painter = painterResource(id = backgroundResourceId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Image(
            painter = painterResource(id = foregroundResourceId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@DrawableRes
private fun AppTheme.launcherBackgroundRes(): Int = when (this) {
    AppTheme.SEA_COTTAGE -> R.drawable.ic_launcher_background_sea_cottage
    AppTheme.GOLDEN_HEARTH -> R.drawable.ic_launcher_background_golden_hearth
    AppTheme.FOREST_FIBER -> R.drawable.ic_launcher_background_forest_fiber
    AppTheme.CLOUD_SOFT -> R.drawable.ic_launcher_background_cloud_soft
    AppTheme.YARN_CANDY -> R.drawable.ic_launcher_background_yarn_candy
    AppTheme.DUSTY_ROSE -> R.drawable.ic_launcher_background_dusty_rose
}

@DrawableRes
private fun AppTheme.foregroundIconRes(): Int = when (this) {
    AppTheme.SEA_COTTAGE -> R.drawable.ic_yarn_sea_cottage
    AppTheme.GOLDEN_HEARTH -> R.drawable.ic_yarn_golden_hearth
    AppTheme.FOREST_FIBER -> R.drawable.ic_yarn_forest_fiber
    AppTheme.CLOUD_SOFT -> R.drawable.ic_yarn_cloud_soft
    AppTheme.YARN_CANDY -> R.drawable.ic_yarn_yarn_candy
    AppTheme.DUSTY_ROSE -> R.drawable.ic_yarn_dusty_rose
}
