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

class ClepsydraWindow(val state: WindowState) {
    var isAlwaysOnTop by mutableStateOf(false)
        private set

    private var restoreSize: DpSize? = null
    private var restorePosition: WindowPosition? = null

    var compactDp = 300.dp
    var compactSize = DpSize(compactDp, compactDp)

    fun toggleCompactMode() {
        if(!isAlwaysOnTop) {
            restoreSize = state.size
            restorePosition = state.position
            state.size = compactSize
        } else {
            restoreSize?.let { state.size = it }
            restorePosition?.let { state.position = it }
        }
        isAlwaysOnTop = !isAlwaysOnTop
    }

    fun minimize() { state.isMinimized = true }

}

@Composable
fun rememberClepsydraeWindow(windowState: WindowState): ClepsydraWindow {
    return remember(windowState) { ClepsydraWindow(windowState) }
}