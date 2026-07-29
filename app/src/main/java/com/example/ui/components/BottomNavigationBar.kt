package com.example.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ui.navigation.Screen

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.Dashboard,
        Screen.Workouts,
        Screen.Nutrition,
        Screen.HealthSleep,
        Screen.Settings
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_item_${item.route}")
            )
        }
    }
}
