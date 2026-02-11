package oqk.ananke.clepsydrae.core

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.DpSize
import androidx.window.core.layout.WindowSizeClass

data class SizeInfo (
    val sizeClass: WindowSizeClass,
    val sizes: DpSize
)

val LocalSizeInfo = compositionLocalOf<SizeInfo> {
    error("SizeInfo not present")
}
