package dev.harrisonsoftware.stitchCounter.feature.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import dev.harrisonsoftware.stitchCounter.R

@Composable
internal fun SettingsCard(
    forceDarkMode: Boolean,
    onForceDarkModeChange: (Boolean) -> Unit,
    forceLightMode: Boolean,
    onForceLightModeChange: (Boolean) -> Unit,
    forceCounterScreensOn: Boolean,
    onForceCounterScreensOnChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsSwitchRow(
                titleRes = R.string.settings_dark_mode,
                subtitleRes = R.string.settings_dark_mode_subtitle,
                checked = forceDarkMode,
                onCheckedChange = onForceDarkModeChange
            )
            SettingsSwitchRow(
                titleRes = R.string.settings_light_mode,
                subtitleRes = R.string.settings_light_mode_subtitle,
                checked = forceLightMode,
                onCheckedChange = onForceLightModeChange
            )
            SettingsSwitchRow(
                titleRes = R.string.settings_force_counter_screens_on,
                subtitleRes = R.string.settings_force_counter_screens_on_subtitle,
                checked = forceCounterScreensOn,
                onCheckedChange = onForceCounterScreensOnChange
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val title = stringResource(titleRes)
    val stateDescription = stringResource(
        if (checked) {
            R.string.cd_setting_on
        } else {
            R.string.cd_setting_off
        }
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .semantics(mergeDescendants = true) {
                this.stateDescription = stateDescription
            }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(subtitleRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.clearAndSetSemantics {}
        )
    }
}
