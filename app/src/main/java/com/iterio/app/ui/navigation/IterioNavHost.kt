package com.iterio.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.iterio.app.ui.screens.calendar.CalendarScreen
import com.iterio.app.ui.screens.home.HomeScreen
import com.iterio.app.ui.screens.backup.BackupScreen
import com.iterio.app.ui.screens.premium.PremiumScreen
import com.iterio.app.ui.screens.settings.SettingsScreen
import com.iterio.app.ui.screens.settings.allowedapps.AllowedAppsScreen
import com.iterio.app.ui.screens.stats.StatsScreen
import com.iterio.app.ui.screens.tasks.TasksScreen
import com.iterio.app.ui.screens.timer.TimerScreen

@Composable
fun IterioNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTimer = { taskId ->
                    navController.navigate(Screen.Timer.createRoute(taskId))
                },
                onNavigateToTasks = {
                    navController.navigate(Screen.Tasks.route)
                }
            )
        }

        composable(Screen.Tasks.route) {
            TasksScreen(
                onStartTimer = { task ->
                    navController.navigate(Screen.Timer.createRoute(task.id))
                }
            )
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(
                onStartTimer = { taskId ->
                    navController.navigate(Screen.Timer.createRoute(taskId))
                }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToPremium = {
                    navController.navigate(Screen.Premium.route)
                },
                onNavigateToBackup = {
                    navController.navigate(Screen.Backup.route)
                },
                onNavigateToAllowedApps = {
                    navController.navigate(Screen.AllowedApps.route)
                }
            )
        }

        composable(Screen.AllowedApps.route) {
            AllowedAppsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Premium.route) {
            PremiumScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Backup.route) {
            BackupScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPremium = {
                    navController.navigate(Screen.Premium.route)
                }
            )
        }

        composable(
            route = Screen.Timer.route,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
            TimerScreen(
                taskId = taskId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPremium = {
                    navController.navigate(Screen.Premium.route)
                }
            )
        }
    }
}
