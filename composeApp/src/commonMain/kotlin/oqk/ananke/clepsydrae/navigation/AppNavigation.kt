package oqk.ananke.clepsydrae.navigation

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import oqk.ananke.clepsydrae.calendar.presentation.CalendarScreen
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScreen
import oqk.ananke.clepsydrae.core.LocalWindowSizeClass
import oqk.ananke.clepsydrae.settings.presentation.SettingsScreen
import oqk.ananke.clepsydrae.statistics.presentation.StatisticsScreen

@Composable
fun AppNavigation() {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val navController = rememberNavController()
    
    CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
        NavHost(navController = navController, startDestination = Screen.Clepsydra.route) {
            composable(Screen.Clepsydra.route) { ClepsydraScreen(navController) }
            composable(Screen.Calendar.route) { CalendarScreen(navController) }
            composable(Screen.Statistics.route) { StatisticsScreen(navController) }
            composable(Screen.Settings.route) { SettingsScreen(navController) }
        }
    }
}
