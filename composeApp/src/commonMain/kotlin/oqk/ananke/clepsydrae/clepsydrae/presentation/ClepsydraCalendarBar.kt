package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.launch
import oqk.ananke.clepsydrae.core.iPhi
import oqk.ananke.clepsydrae.navigation.Screen

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun ClepsydraScope.ClepsydraCalendarBar(modifier: Modifier = Modifier.Companion) {
    // Animation State
    val textOffset = remember { Animatable(0f) }
    val textScale = remember { Animatable(1f) }
    val textRotation = remember { Animatable(0f) }
    val textOpacity = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    fun animateDateChange(direction: Int) {
        coroutineScope.launch {
            // Exit
            launch { textScale.animateTo(1.3f, tween(100, easing = EaseInCubic)) }
            launch { textRotation.animateTo(5f * -direction, tween(100, easing = EaseInCubic)) }
            launch { textOpacity.animateTo(0.6f, tween(100, easing = EaseInCubic)) }
            textOffset.animateTo(200f * -direction, tween(100, easing = EaseInCubic))

            // Snap
            textOffset.snapTo(200f * direction)
            textRotation.snapTo(-5f * -direction)

            // Enter
            launch { textScale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)) }
            launch { textRotation.animateTo(0f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)) }
            launch { textOpacity.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)) }
            textOffset.animateTo(0f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow))
        }
    }

    Row(
        modifier = modifier
            .widthIn(max = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp * iPhi)
            .fillMaxWidth()
            .height(64.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Previous Button (Satellite) ---
        Box(
            modifier = Modifier.fillMaxHeight().sq(),
            contentAlignment = Alignment.Center
        ) {
            ArrowButton(
                modifier = Modifier.size(48.dp),
                rotation = 360,
                onClick = {
                    animateDateChange(-1)
                    onAction(ClepsydraScreenAction.OnPreviousDay)
                }
            )
        }

        Box(
            modifier = Modifier.height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                onClick = { navController.navigate(Screen.MONTH.name) },
                modifier = Modifier.Companion,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 6.dp
            ) {

                Column(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val dateComp = st.dateText.split(" ")
                    Text(
                        text = dateComp[0],
                        modifier = Modifier.weight(7f).wrapContentHeight()
                            .offset(x = textOffset.value.dp)
                            .scale(textScale.value)
                            .rotate(textRotation.value)
                            .alpha(textOpacity.value),
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        autoSize = TextAutoSize.StepBased(1.sp, stepSize = 0.001.sp)
                    )
                    Text(
                        text = "${dateComp[1]} ${dateComp[2]} ${dateComp[3]}",
                        modifier = Modifier.weight(5f).wrapContentHeight()
                            .offset {
                                IntOffset(textOffset.value.toInt(), 0)
                            }
                            .scale(textScale.value)
                            .rotate(textRotation.value)
                            .alpha(textOpacity.value),
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        color = LocalContentColor.current.copy(alpha = iPhi),
                        autoSize = TextAutoSize.StepBased(1.sp, stepSize = 0.001.sp)
                    )
                }
            }
        }

        // --- Next Button (Satellite) ---
        Box(
            modifier = Modifier.fillMaxHeight().sq(),
            contentAlignment = Alignment.Center
        ) {
            ArrowButton(
                modifier = Modifier.size(48.dp),
                rotation = 180,
                onClick = {
                    animateDateChange(1)
                    onAction(ClepsydraScreenAction.OnNextDay)
                }
            )
        }
    }
}