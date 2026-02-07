package oqk.ananke.clepsydrae.core

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.window.core.layout.WindowSizeClass

/**
 * Scope interface for screen composables providing state, actions, window info, and responsive utilities.
 */
interface ScreenScope<S, A> {
    /** Screen state */
    val st: S
    /** Action handler */
    val onAction: (A) -> Unit
    /** Window size class for responsive layout */
    val ws: WindowSizeClass
    /** User-configured UI scale (0.75-1.5x) */
    val uiScale: Float
    /** Navigation controller */
    val navController: NavController
    
    /** Width ≥ 600dp */
    val isWide: Boolean get() = ws.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    /** Width ≥ 840dp */
    val isExtraWide: Boolean get() = ws.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    /** Height ≥ 600dp */
    val isTall: Boolean get() = ws.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
    /** Height ≥ 840dp */
    val isExtraTall: Boolean get() = ws.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND)
    /** Portrait orientation based on breakpoints */
    val isPortrait: Boolean get() = (isTall && !(isWide || isExtraWide)) || (isExtraTall && !isExtraWide)
    /** Automatic scale based on window size (1x/2x/3x) */
    val adaptiveScale: Float get() = if(isExtraWide) 3f else if(isWide) 2f else 1f
    /** Combined user scale × adaptive scale */
    val effectiveScale: Float get() = uiScale * adaptiveScale
    
    /** Fill max dimension based on orientation */
    fun Modifier.fillMaxSmallest(fraction: Float = 1f): Modifier =
        if (isPortrait) fillMaxWidth(fraction) else fillMaxHeight(fraction)

    fun Modifier.fillMaxBiggest(fraction: Float = 1f): Modifier =
        if (isPortrait) fillMaxHeight(fraction) else fillMaxWidth(fraction)

    /** Responsive padding based on window size */
    fun Modifier.adaptivePadding(fixedScale: Float = 1f, minPadding: Int = 8): Modifier {
        return padding(maxOf(minPadding, minOf(ws.minWidthDp, ws.minHeightDp)/50).adp(fixedScale))
    }

    /** make square */
    fun Modifier.sq(matchHeightConstraintsFirst: Boolean = false): Modifier = aspectRatio(1f, matchHeightConstraintsFirst)
    
    /** Convert Int to adaptive Dp */
    fun Int.adp(fixedScale: Float = 1f): Dp = this.dp.adaptive(fixedScale)
    /** Apply effective scale to Dp */
    fun Dp.adaptive(fixedScale: Float = 1f): Dp = this * effectiveScale * fixedScale

    @Composable
    fun ConditionalLayout(condition: Boolean,
                          modifier: Modifier = Modifier,
                          horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
                          verticalAlignment: Alignment.Vertical = Alignment.Top,
                          verticalArrangement: Arrangement.Vertical = Arrangement.Top,
                          horizontalAlignment: Alignment.Horizontal = Alignment.Start,
                          content: @Composable (() -> Unit)
                          ) {
        if(condition) Row(Modifier, horizontalArrangement, verticalAlignment) { content() }
        else Column(Modifier,verticalArrangement ,horizontalAlignment ) { content() }
    }
}

fun Modifier.debugBorder(dp: Dp = 1.dp ,color: Color = Color.Green): Modifier = border(dp, color)

const val phi = 1.618f
const val iPhi = 0.618f
