package oqk.ananke.clepsydrae.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import oqk.ananke.clepsydrae.calendar.presentation.MonthScreen
import oqk.ananke.clepsydrae.calendar.presentation.DayScreen
import oqk.ananke.clepsydrae.settings.presentation.SettingsScreen
import oqk.ananke.clepsydrae.statistics.presentation.StatisticsScreen

@Composable
fun ClepsydraeNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.DAY.name) {
        for (screen in Screen.entries) {
            composable(screen.name) {
                ScreenFactory(screen, navController)
            }
        }
    }
}

@Composable
fun ScreenFactory(screen: Screen, navController: NavHostController) {
    return when (screen) {
        Screen.DAY ->  DayScreen(navController)
        Screen.WEEK -> WeekScreen(navController)
        Screen.MONTH -> MonthScreen(navController)
        Screen.YEAR -> YearScreen(navController)
        Screen.MEMORY -> MemoryScreen(navController)
        Screen.PLAN -> PlanScreen(navController)
        Screen.SETTINGS -> SettingsScreen(navController)
        Screen.STATS -> StatisticsScreen(navController)
    }
}