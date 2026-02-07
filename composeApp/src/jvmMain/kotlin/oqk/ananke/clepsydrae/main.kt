package oqk.ananke.clepsydrae

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.*
import java.awt.Toolkit
import oqk.ananke.clepsydrae.navigation.ClepsydraeNavigation

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
    ClepsydraeApp {
        Column(modifier = Modifier.fillMaxSize()) {
            WindowDraggableArea {
                WindowsClepsydraeTitleBar(onMinimize, onClose, alwaysOnTop, onToggleAlwaysOnTop)
            }
            ClepsydraeNavigation()
        }
    }
}

@Composable
fun WindowsClepsydraeTitleBar(
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    alwaysOnTop: Boolean,
    onToggleAlwaysOnTop: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().height(24.dp).background(MaterialTheme.colorScheme.surface)) {
        Text(
            "Clepsydrae",
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.labelSmall
        )
        Row(modifier = Modifier.align(Alignment.CenterEnd)) {
            IconButton(onClick = onToggleAlwaysOnTop, modifier = Modifier.size(24.dp)) {
                Icon(
                    if (alwaysOnTop) Icons.Default.Lock else Icons.Default.PushPin,
                    null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = if (alwaysOnTop) 1f else 0.3f)
                )
            }
            IconButton(onClick = onMinimize, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Minimize, null, modifier = Modifier.size(12.dp))
            }
            IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(12.dp))
            }
        }
    }
}
