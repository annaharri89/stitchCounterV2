package dev.harrisonsoftware.stitchCounter.feature.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController

@Composable
fun NavigationRailLayout(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    navController: NavHostController
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(durationMillis = AnimationConstants.NAVIGATION_ANIMATION_DURATION)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(durationMillis = AnimationConstants.NAVIGATION_ANIMATION_DURATION)
        )
    ) {
        NavigationRailComponent(
            selectedTab = selectedTab,
            navController = navController,
            onTabSelected = onTabSelected
        )
    }
}

@Composable
private fun NavigationRailComponent(
    selectedTab: BottomNavTab,
    navController: NavHostController,
    onTabSelected: (BottomNavTab) -> Unit
) {
    NavigationRail {
        BottomNavTab.entries.forEach { tab ->
            val tabTitle = stringResource(tab.titleResId)
            NavigationRailItem(
                selected = selectedTab == tab,
                onClick = {
                    onTabSelected(tab)
                    navigateToDestination(navController, getDestinationForTab(tab))
                },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tabTitle
                    )
                },
                label = {
                    Text(tabTitle)
                },
                colors = NavigationRailItemColors(
                    selectedIconColor = MaterialTheme.colorScheme.secondary,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    selectedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    disabledIconColor = Color.Gray,
                    disabledTextColor = Color.Gray
                )
            )
        }
    }
}
