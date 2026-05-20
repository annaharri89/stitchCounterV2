package dev.harrisonsoftware.stitchCounter.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.domain.model.AppTheme
import dev.harrisonsoftware.stitchCounter.feature.theme.ThemeColor

@Composable
fun ThemeCard(selectedTheme: AppTheme,
              updateTheme: (theme: AppTheme) -> Unit,
              themeColors: List<ThemeColor>
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.settings_choose_color_scheme),
            style = MaterialTheme.typography.titleMedium
        )

        AppTheme.entries.forEach { theme ->
            ThemeOptionCard(
                theme = theme,
                isSelected = selectedTheme == theme,
                themeColors = if (selectedTheme == theme) themeColors else emptyList(),
                onThemeSelected = { updateTheme(theme) }
            )
        }
    }
}