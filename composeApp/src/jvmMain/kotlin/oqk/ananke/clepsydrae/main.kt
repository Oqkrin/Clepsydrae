package oqk.ananke.clepsydrae

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.*
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScreenAction
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScreenViewModel
import oqk.ananke.clepsydrae.core.JvmNotificationManager
import oqk.ananke.clepsydrae.core.LocalSizeInfo
import oqk.ananke.clepsydrae.core.isShort
import oqk.ananke.clepsydrae.core.minSquared
import oqk.ananke.clepsydrae.core.phi
import oqk.ananke.clepsydrae.navigation.ClepsydraeNavigation
import org.koin.compose.viewmodel.koinViewModel

fun main() = application {

    val trayState = rememberTrayState()
    val clepsydraeWindow = rememberClepsydraeWindow(rememberWindowState())

    Window(
        onCloseRequest = ::exitApplication,
        title = "Clepsydrae",
        state = clepsydraeWindow.state,
        undecorated = true,
        alwaysOnTop = clepsydraeWindow.isAlwaysOnTop
    ) {

        val monitorSize by mutableStateOf(getMonitorSize())
        clepsydraeWindow.compactSize = remember(monitorSize) { monitorSize.minSquared() / 2 * phi }

        ClepsydraeApp(JvmNotificationManager(trayState)) {
            MainContent(
                windowSizeClass = LocalSizeInfo.current.sizeClass,
                clepsydraeWindow = clepsydraeWindow,
                onClose = ::exitApplication
            )

            Tray(
                state = trayState,
                icon = ColorPainter(MaterialTheme.colorScheme.primary),
                menu = {
                    Item("Exit", onClick = ::exitApplication)
                }
            )
        }
    }
}

@Composable
fun FrameWindowScope.MainContent(
    windowSizeClass: WindowSizeClass,
    clepsydraeWindow: ClepsydraWindow,
    onClose: () -> Unit
) {
    val viewModel: ClepsydraScreenViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val navController = rememberNavController()


    Column(modifier = Modifier.fillMaxSize()) {
        ClepsydraWindowTitleBar(
            title = if (windowSizeClass.isShort()) state.dateText else "Clepsydrae",
            isAlwaysOnTop = clepsydraeWindow.isAlwaysOnTop,
            isCompact = windowSizeClass.isShort(),
            onToggleAlwaysOnTop = clepsydraeWindow::toggleCompactMode,
            onMinimize = clepsydraeWindow::minimize,
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

