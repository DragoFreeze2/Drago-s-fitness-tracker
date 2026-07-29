package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.BottomNavigationBar
import com.example.ui.navigation.NavGraph
import com.example.ui.navigation.Screen
import com.example.ui.theme.HealthTrackerTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HealthTrackerTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val userMessage by viewModel.userMessage.collectAsState()

                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry.value?.destination?.route

                // Hide bottom bar on secondary full-screen flows (WorkoutLogger and OcrScanner)
                val showBottomBar = currentRoute in listOf(
                    Screen.Dashboard.route,
                    Screen.Workouts.route,
                    Screen.Nutrition.route,
                    Screen.HealthSleep.route,
                    Screen.Settings.route
                )

                LaunchedEffect(userMessage) {
                    userMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearUserMessage()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(navController = navController)
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
