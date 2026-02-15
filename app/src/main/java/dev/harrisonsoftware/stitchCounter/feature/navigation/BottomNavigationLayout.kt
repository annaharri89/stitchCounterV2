package dev.harrisonsoftware.stitchCounter.feature.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController


@Composable
fun BottomNavigationLayout(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    navController: NavHostController
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = AnimationConstants.NAVIGATION_ANIMATION_DURATION)
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = AnimationConstants.NAVIGATION_ANIMATION_DURATION)
        )
    ) {
        BottomNavigationBar(
            selectedTab = selectedTab,
            navController = navController,
            onTabSelected = onTabSelected
        )
    }
}

@Composable
private fun BottomNavigationBar(
    selectedTab: BottomNavTab,
    navController: NavHostController,
    onTabSelected: (BottomNavTab) -> Unit
) {
    NavigationBar {
        BottomNavTab.entries.forEach { tab ->
            val tabTitle = stringResource(tab.titleResId)
            NavigationBarItem(
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
                colors = NavigationBarItemColors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    selectedIndicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledIconColor = Color.Gray,
                    disabledTextColor = Color.Gray
                )
            )
        }
    }
}
