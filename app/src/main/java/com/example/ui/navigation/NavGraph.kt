package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ui.screens.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToWorkouts = { navController.navigate(Screen.Workouts.route) },
                onNavigateToNutrition = { navController.navigate(Screen.Nutrition.route) },
                onNavigateToHealth = { navController.navigate(Screen.HealthSleep.route) }
            )
        }

        composable(Screen.Workouts.route) {
            WorkoutsScreen(
                viewModel = viewModel,
                onOpenLogger = { navController.navigate(Screen.WorkoutLogger.route) }
            )
        }

        composable(Screen.WorkoutLogger.route) {
            WorkoutLoggerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Nutrition.route) {
            NutritionScreen(
                viewModel = viewModel,
                onOpenOcrScanner = { navController.navigate(Screen.OcrScanner.route) }
            )
        }

        composable(Screen.OcrScanner.route) {
            OcrScannerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.HealthSleep.route) {
            HealthSleepScreen(viewModel = viewModel)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = viewModel)
        }
    }
}
