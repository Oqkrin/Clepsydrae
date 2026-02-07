package oqk.ananke.clepsydrae

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.*
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScreenAction
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScreenViewModel
import oqk.ananke.clepsydrae.core.debugBorder
import java.awt.Toolkit
import oqk.ananke.clepsydrae.navigation.ClepsydraeNavigation
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

private var alwaysOnTop = mutableStateOf(false)
private var savedState: Pair<DpSize, WindowPosition>? = null

fun main() = application {
    val windowState = rememberWindowState(width = 400.dp, height = 600.dp)
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "Clepsydrae",
        state = windowState,
        undecorated = true,
        alwaysOnTop = alwaysOnTop.value
    ) {
        AppWithTitleBar(
            onMinimize = { window.isMinimized = true },
            onClose = ::exitApplication,
            alwaysOnTop = alwaysOnTop.value,
            onToggleAlwaysOnTop = { 
                if (!alwaysOnTop.value) {
                    savedState = windowState.size to windowState.position
                    val screenSize = Toolkit.getDefaultToolkit().screenSize
                    windowState.size = DpSize(300.dp, 300.dp)
                    windowState.position = WindowPosition((screenSize.width - 300).dp, 0.dp)
                } else {
                    savedState?.let { (size, pos) ->
                        windowState.size = size
                        windowState.position = pos
                    }
                }
                alwaysOnTop.value = !alwaysOnTop.value
            }
        )
    }
}

@Composable
fun FrameWindowScope.AppWithTitleBar(
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    alwaysOnTop: Boolean,
    onToggleAlwaysOnTop: () -> Unit
) {
    val windowSizeClass by rememberUpdatedState(currentWindowAdaptiveInfo().windowSizeClass)

    ClepsydraeApp(windowSizeClass) {
        Column(modifier = Modifier.fillMaxSize()) {
            DesktopClepsydrae(onMinimize, onClose, alwaysOnTop, onToggleAlwaysOnTop)
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun FrameWindowScope.DesktopClepsydrae(
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    alwaysOnTop: Boolean,
    onToggleAlwaysOnTop: () -> Unit
) {
    val ws by koinInject<State<WindowSizeClass>>()
    val cvw: ClepsydraScreenViewModel = koinViewModel()
    val st = cvw.state.collectAsState()
    val onA = cvw::onAction
    val nv = rememberNavController()
    val isTall = ws.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND)
            || ws.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)

    WindowDraggableArea {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)) {

            Box(modifier = Modifier.weight(1f)) {
                IconButton(onClick = onToggleAlwaysOnTop, modifier = Modifier.size(24.dp)) {
                    Icon(
                        if (alwaysOnTop) Icons.Default.Lock else Icons.Default.PushPin,
                        null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = if (alwaysOnTop) 1f else 0.3f)
                    )
                }
            }

            Row(modifier = Modifier.weight(5f).height(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {

                if (!isTall) {
                    IconButton(
                        onClick = { onA(ClepsydraScreenAction.OnPreviousDay) },
                        modifier = Modifier.size(12.dp).weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "go in the past",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Text(
                    modifier = Modifier.clickable(onClick = { nv.navigate("Calendar") }).weight(3f),
                    text = if (isTall) "Clepsydrae" else st.value.dateText,
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer),
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )

                if (!isTall) {
                    IconButton(
                        onClick = { onA(ClepsydraScreenAction.OnNextDay) },
                        modifier = Modifier.size(12.dp).weight(1f)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward, "go in the future",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Row(modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically ) {

                IconButton(onClick = onMinimize, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Minimize, null, modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    ClepsydraeNavigation(nv)

}
