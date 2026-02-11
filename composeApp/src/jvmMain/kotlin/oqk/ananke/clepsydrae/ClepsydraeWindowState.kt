package oqk.ananke.clepsydrae

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState

class ClepsydraeWindowState(private val windowState: WindowState) {
    var isAlwaysOnTop by mutableStateOf(false)
        private set

    private var restoreSize: DpSize? = null
    private var restorePosition: WindowPosition? = null

    var compactSize = DpSize(300.dp, 300.dp)

    fun toggleCompactMode() {
        if(!isAlwaysOnTop) {
            restoreSize = windowState.size
            restorePosition = windowState.position
            windowState.size = compactSize
        } else {
            restoreSize?.let { windowState.size = it }
            restorePosition?.let { windowState.position = it }
        }
        isAlwaysOnTop = !isAlwaysOnTop
    }

    fun minimize() { windowState.isMinimized = true }

}

@Composable
fun rememberClepsydraeWindowState(windowState: WindowState): ClepsydraeWindowState {
    return remember(windowState) { ClepsydraeWindowState(windowState) }
}