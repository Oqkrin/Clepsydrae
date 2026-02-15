package oqk.ananke.clepsydrae.core

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.navigation.NavController
import androidx.window.core.layout.WindowSizeClass
import kotlinx.datetime.LocalDate

/**
 * Scope interface for screen composables providing state, actions, window info, and responsive utilities.
 */
interface ScreenScope<S, A> {
    /** Screen state */
    val st: S
    /** Action handler */
    val onAction: (A) -> Unit
    /** Window size class for responsive layout */
    val wsc: WindowSizeClass
    val sizes: DpSize //avelible screen size
    /** User-configured UI scale (0.75-1.5x) */
    val uiScale: Float
    /** Navigation controller */
    val navController: NavController

    val isShort: Boolean get() = wsc.isShort()
    val isNarrow: Boolean get() = wsc.isNarrow()
    /** Width ≥ 600dp */
    val isWide: Boolean get() = wsc.isWide()
    /** Width ≥ 840dp */
    val isExtraWide: Boolean get() = wsc.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    /** Height ≥ 600dp */
    val isTall: Boolean get() = wsc.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
    /** Height ≥ 840dp */
    val isExtraTall: Boolean get() = wsc.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND)
    /** Portrait orientation based on breakpoints */
    val isPortrait: Boolean get() = sizes.isPortrait()
    /** Automatic scale based on window size (1x/2x/3x) */
    val adaptiveScale: Float get() = if(isExtraWide) 3f else if(isWide) 2f else 1f
    /** Combined user scale × adaptive scale */
    val effectiveScale: Float get() = uiScale * adaptiveScale

    /** Fill max dimension based on orientation */
    fun Modifier.fillMaxSmallest(fraction: Float = 1f): Modifier =
        if (isPortrait) fillMaxWidth(fraction) else fillMaxHeight(fraction)

    fun Modifier.fillMaxBiggest(fraction: Float = 1f): Modifier =
        if (isPortrait) fillMaxHeight(fraction) else fillMaxWidth(fraction)

    /** * Fluidly lerps padding based on the window's actual dimensions.
     * Maps the size range (600dp - 1200dp) to a scale range (1x - 3x).
     */
    fun Modifier.adaptivePadding(fixedScale: Float = 1f, minPadding: Dp = 8.dp): Modifier {
        // We define the "Floor" and "Ceiling" for our fluid scaling
        val minLimit = 480.dp
        val maxLimit = 1200.dp

        // Calculate a 0.0 -> 1.0 progress based on width and height separately
        val hProgress = ((sizes.width - minLimit) / (maxLimit - minLimit)).coerceIn(0f, 1f)
        val vProgress = ((sizes.height - minLimit) / (maxLimit - minLimit)).coerceIn(0f, 1f)

        // Lerp the multiplier.
        // 1f + (hProgress * 2f) smoothly transitions from 1.0 to 3.0
        val hMultiplier = 1f + (hProgress * 2f)
        val vMultiplier = 1f + (vProgress * 2f)

        return padding(
            horizontal = minPadding * fixedScale * hMultiplier,
            vertical = minPadding * fixedScale * vMultiplier
        )
    }

    /** * A "Better" Adaptive Scale:
     * Instead of 1f, 2f, or 3f, it returns a precise float like 1.42f
     * based on the window's smallest dimension.
     */
    val fluidScale: Float get() {
        val minDim = sizes.min()
        return when {
            minDim < 600.dp -> 1f
            minDim < 840.dp -> lerp(1f.dp, 2f.dp, (minDim - 600.dp) / (840.dp - 600.dp)).value
            else -> lerp(2f.dp, 3f.dp, (minDim - 840.dp) / (1200.dp - 840.dp)).value
        }.coerceIn(1f, 3f)
    }

    /** Replaces effectiveScale with a smoother version */
    val smoothEffectiveScale: Float get() = uiScale * fluidScale

    /** make square */
    fun Modifier.sq(matchHeightConstraintsFirst: Boolean = false): Modifier = aspectRatio(1f, matchHeightConstraintsFirst)

    /** Convert Int to adaptive Dp */
    fun Int.adp(fixedScale: Float = 1f): Dp = this.dp.adaptive(fixedScale)
    /** Apply effective scale to Dp */
    fun Dp.adaptive(fixedScale: Float = 1f): Dp = this * smoothEffectiveScale * fixedScale

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

fun formatDate(date: LocalDate): String {
    val dayName = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
    return "$dayName ${date.day} $monthName ${date.year}"
}

fun WindowSizeClass.isTall(): Boolean = this.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
fun WindowSizeClass.isWide(): Boolean = this.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
fun WindowSizeClass.isExtraWide(): Boolean = this.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
fun WindowSizeClass.isExtraTall(): Boolean = this.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND)
fun WindowSizeClass.isShort(): Boolean = !this.isTall() && !this.isExtraTall()
fun WindowSizeClass.isNarrow(): Boolean = !this.isWide() && !this.isExtraWide()

fun DpSize.isPortrait(): Boolean = this.height > this.width
fun DpSize.isLandscape(): Boolean = this.width > this.height
fun DpSize.isSquare(): Boolean = this.width == this.height
fun DpSize.min(): Dp = if(this.isPortrait()) this.width else this.height
fun DpSize.max(): Dp = if(this.isPortrait()) this.height else this.width
fun DpSize.minSquared(): DpSize = DpSize(min(), min())
fun DpSize.maxSquared(): DpSize = DpSize(max(), max())