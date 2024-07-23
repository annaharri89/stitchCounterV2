package io.github.annaharri89.stitchcounter.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ImportExport
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import io.github.annaharri89.stitchcounter.R
import io.github.annaharri89.stitchcounter.appCurrentDestinationAsState
import io.github.annaharri89.stitchcounter.destinations.Destination
import io.github.annaharri89.stitchcounter.destinations.LibraryScreenDestination
import io.github.annaharri89.stitchcounter.destinations.PortScreenDestination
import io.github.annaharri89.stitchcounter.destinations.SettingsScreenDestination
import io.github.annaharri89.stitchcounter.theme.STTheme


enum class BottomBarDestination(
    val direction: DirectionDestinationSpec,
    val icon: ImageVector,
    @StringRes val label: Int
) {
    Library(LibraryScreenDestination, Icons.Outlined.LocalLibrary, R.string.action_library),
    Port(PortScreenDestination, Icons.Outlined.ImportExport, R.string.action_port),
    Settings(SettingsScreenDestination, Icons.Outlined.Settings, R.string.action_settings),
}

@Composable
fun BottomBar(
    navController: NavController
) {
    val currentDestination: Destination? = navController.appCurrentDestinationAsState().value
        //?: StitchTrackerNavGraph//todo

    BottomNavigation(modifier = Modifier.height(75.dp), backgroundColor = STTheme.colors.primary) {
        BottomBarDestination.entries.forEach { destination ->
            BottomNavigationItem(
                selected = currentDestination == destination.direction,
                onClick = {
                    navController.navigate(destination.direction, fun NavOptionsBuilder.() {
                        launchSingleTop = true
                    })
                },
                selectedContentColor = STTheme.colors.accentDark,
                unselectedContentColor = STTheme.colors.background,
                alwaysShowLabel = false,
                icon = { Icon(destination.icon, modifier = Modifier.size(40.dp), contentDescription = stringResource(destination.label))},
                label = { Text(stringResource(destination.label)) },
            )
        }
    }
}