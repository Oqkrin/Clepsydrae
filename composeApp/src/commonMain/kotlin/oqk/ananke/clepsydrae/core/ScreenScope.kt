package oqk.ananke.clepsydrae.core

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

interface ScreenScope<S, A> {
    val st: S
    val onAction: (A) -> Unit
    val ws: WindowSizeClass
    val isPortrait: Boolean
        get() = ws.minWidthDp < ws.minHeightDp
    fun Modifier.fillMaxSmallest(fraction: Float = 1f): Modifier = 
        if (isPortrait) fillMaxWidth(fraction) else fillMaxHeight(fraction)
    fun Modifier.responsivePadding(fraction: Float = 0.03f): Modifier =
        padding(((if(isPortrait) ws.minWidthDp else ws.minHeightDp) * fraction).dp)
}

fun Modifier.debugBorder(dp: Dp = 1.dp ,color: Color = Color.Green): Modifier = border(dp, color)

const val phi = 1.618f
const val iPhi = 0.618f
