package oqk.ananke.clepsydrae.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HourglassFull
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.HourglassDisabled
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass
import oqk.ananke.clepsydrae.calendar.presentation.DayScreen
import oqk.ananke.clepsydrae.calendar.presentation.MonthScreen
import oqk.ananke.clepsydrae.calendar.presentation.WeekScreen
import oqk.ananke.clepsydrae.calendar.presentation.YearScreen
import oqk.ananke.clepsydrae.core.LocalSettings
import oqk.ananke.clepsydrae.core.LocalSizeInfo
import oqk.ananke.clepsydrae.core.ScreenScope
import oqk.ananke.clepsydrae.core.iPhi
import oqk.ananke.clepsydrae.habits.presentation.HabitsScreen
import oqk.ananke.clepsydrae.journal.presentation.JournalScreen
import oqk.ananke.clepsydrae.settings.presentation.SettingsScreen
import oqk.ananke.clepsydrae.statistics.presentation.StatisticsScreen

interface NavigationScope : ScreenScope<Unit, Unit>

/** Routes on which the global bottom nav should be visible */
private val mainRoutes = setOf(
    Screen.DAY.name,
    Screen.WEEK.name,
    Screen.MONTH.name,
    Screen.YEAR.name
)

@Composable
fun ClepsydraeNavigation(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val ws = LocalSizeInfo.current.sizeClass
    val sz = LocalSizeInfo.current.sizes
    val scale = LocalSettings.current.uiScale

    val scope = remember(ws, sz, scale) {
        object : NavigationScope {
            override val st = Unit
            override val onAction: (Unit) -> Unit = {}
            override val wsc: WindowSizeClass = ws
            override val sizes: DpSize = sz
            override val uiScale: Float = scale
            override val navController: NavController = navController
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Content
        NavHost(
            navController = navController,
            startDestination = Screen.DAY.name,
            modifier = Modifier.fillMaxSize()
        ) {
            for (screen in Screen.entries) {
                composable(screen.name) {
                    ScreenFactory(screen, navController)
                }
            }
        }

        // Global Adaptive Navigation Overlay
        if (currentRoute in mainRoutes) {
            with(scope) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(bottom = 16.dp, end = 16.dp, start = 16.dp)
                ) {
                    if (!isNarrow) {
                        ClepsydraeGlobalNavigationBar(
                            currentRoute = currentRoute,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxHeight(iPhi)
                        )
                    } else {
                        ClepsydraeGlobalNavigationBar(
                            currentRoute = currentRoute,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenFactory(screen: Screen, navController: NavController) {
    return when (screen) {
        Screen.DAY -> DayScreen(navController)
        Screen.JOURNAL -> JournalScreen(navController)
        Screen.HABITS -> HabitsScreen(navController)
        Screen.WEEK -> WeekScreen(navController)
        Screen.MONTH -> MonthScreen(navController)
        Screen.YEAR -> YearScreen(navController)
        Screen.SETTINGS -> SettingsScreen(navController)
        Screen.STATS -> StatisticsScreen(navController)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NavigationScope.ClepsydraeGlobalNavigationBar(currentRoute: String?, modifier: Modifier = Modifier) {
    data class NavItem(
        val screen: Screen,
        val label: String,
        val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
        val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    )

    val items = listOf(
        NavItem(Screen.DAY,   "Day",   Icons.Filled.HourglassFull, Icons.Outlined.HourglassDisabled),
        NavItem(Screen.WEEK,  "Week",  Icons.Filled.ViewWeek,      Icons.Outlined.ViewWeek),
        NavItem(Screen.MONTH, "Month", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        NavItem(Screen.YEAR,  "Year",  Icons.Filled.DateRange,     Icons.Outlined.DateRange),
    )

    val haptic = LocalHapticFeedback.current

    val itemColors = NavigationBarItemColors(
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = iPhi),
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = iPhi),
        selectedIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
        disabledIconColor = MaterialTheme.colorScheme.error,
        disabledTextColor = MaterialTheme.colorScheme.error
    )

    Card(
        shape = RoundedCornerShape(60),
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        if (isNarrow) {
            NavigationBar(
                containerColor = NavigationBarDefaults.containerColor.copy(alpha = iPhi),
                modifier = Modifier.height(64.dp).widthIn(max = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp * 2 / 3)
            ) {
                items.fastForEachIndexed { i, item ->
                    val selected = currentRoute == item.screen.name
                    NavigationBarItem(
                        selected = selected,
                        label = { Text(item.label, style = MaterialTheme.typography.labelMediumEmphasized, maxLines = 1) },
                        onClick = {
                            if (!selected) {
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                navController.navigate(item.screen.name) {
                                    popUpTo(Screen.DAY.name) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                modifier = Modifier.size(if (isShort) 14.dp else 24.dp)
                            )
                        },
                        colors = itemColors,
                        alwaysShowLabel = !(isNarrow && isShort)
                    )
                }
            }
        } else {
            NavigationRail(
                containerColor = NavigationBarDefaults.containerColor.copy(alpha = iPhi),
                modifier = Modifier.width(64.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    items.fastForEachIndexed { i, item ->
                        val selected = currentRoute == item.screen.name
                        NavigationRailItem(
                            modifier = Modifier.weight(1f),
                            selected = selected,
                            label = {
                                Text(
                                    item.label,
                                    style = MaterialTheme.typography.labelSmallEmphasized,
                                    maxLines = 1
                                )
                            },
                            onClick = {
                                if (!selected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    navController.navigate(item.screen.name) {
                                        popUpTo(Screen.DAY.name) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            colors = NavigationRailItemColors(
                                selectedIconColor = itemColors.selectedIconColor,
                                unselectedIconColor = itemColors.unselectedIconColor,
                                selectedTextColor = itemColors.selectedTextColor,
                                unselectedTextColor = itemColors.unselectedTextColor,
                                selectedIndicatorColor = itemColors.selectedIndicatorColor,
                                disabledIconColor = itemColors.disabledIconColor,
                                disabledTextColor = itemColors.disabledTextColor
                            )
                        )
                    }
                }
            }
        }
    }
}