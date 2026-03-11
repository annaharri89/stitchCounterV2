package dev.harrisonsoftware.stitchCounter.feature.sharedComposables

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
    val previewResourceId = theme.previewDrawableRes
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
            painter = painterResource(id = previewResourceId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
