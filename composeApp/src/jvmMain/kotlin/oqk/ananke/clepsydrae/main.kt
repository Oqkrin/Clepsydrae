package oqk.ananke.clepsydrae

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowDpSize
import androidx.compose.runtime.*
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.*
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScreenAction
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScreenViewModel
import oqk.ananke.clepsydrae.core.LocalSizeInfo
import oqk.ananke.clepsydrae.core.SizeInfo
import oqk.ananke.clepsydrae.core.isShort
import oqk.ananke.clepsydrae.core.minSquared
import oqk.ananke.clepsydrae.core.phi
import oqk.ananke.clepsydrae.navigation.ClepsydraeNavigation
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun main() = application {

    val windowState = rememberWindowState()
    val clepsydraeWindowState = rememberClepsydraeWindowState(windowState)

    Window(
        onCloseRequest = ::exitApplication,
        title = "Clepsydrae",
        state = windowState,
        undecorated = true,
        alwaysOnTop = clepsydraeWindowState.isAlwaysOnTop
    ) {

        val monitorSize by mutableStateOf(getMonitorSize())
        clepsydraeWindowState.compactSize = remember(monitorSize) { monitorSize.minSquared() / 2 * phi }

        ClepsydraeApp {
            MainContent(
                windowSizeClass = LocalSizeInfo.current.sizeClass,
                clepsydraeWindowState = clepsydraeWindowState,
                onClose = ::exitApplication
            )
        }
    }
}

@Composable
fun FrameWindowScope.MainContent(
    windowSizeClass: WindowSizeClass,
    clepsydraeWindowState: ClepsydraeWindowState,
    onClose: () -> Unit
) {
    val viewModel: ClepsydraScreenViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val navController = rememberNavController()

    // Determine UI mode based on window height

    Column(modifier = Modifier.fillMaxSize()) {
        ClepsydraWindowTitleBar(
            title = if (windowSizeClass.isShort()) state.dateText else "Clepsydrae",
            isAlwaysOnTop = clepsydraeWindowState.isAlwaysOnTop,
            isCompact = windowSizeClass.isShort(),
            onToggleAlwaysOnTop = clepsydraeWindowState::toggleCompactMode,
            onMinimize = clepsydraeWindowState::minimize,
            onClose = onClose,
            onNavigateCalendar = { navController.navigate("Calendar") },
            onPreviousDay = { viewModel.onAction(ClepsydraScreenAction.OnPreviousDay) },
            onNextDay = { viewModel.onAction(ClepsydraScreenAction.OnNextDay) }
        )

        // The Navigation Host takes up the remaining space
        ClepsydraeNavigation(navController)
    }
}

@Composable
fun FrameWindowScope.getMonitorSize(): DpSize {
    val bounds = window.graphicsConfiguration.bounds
    return with(LocalDensity.current) {
        DpSize(bounds.width.toDp(), bounds.height.toDp())
    }
}

