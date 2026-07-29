package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dash", Icons.Default.Dashboard)
    object Workouts : Screen("workouts", "Workouts", Icons.Default.FitnessCenter)
    object Nutrition : Screen("nutrition", "Nutrition", Icons.Default.Restaurant)
    object HealthSleep : Screen("health", "Health", Icons.Default.Favorite)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    // Secondary routes
    object WorkoutLogger : Screen("workout_logger", "Logger", Icons.Default.FitnessCenter)
    object OcrScanner : Screen("ocr_scanner", "OCR Scan", Icons.Default.Restaurant)
}
