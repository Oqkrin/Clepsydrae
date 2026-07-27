package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.TonalToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import oqk.ananke.clepsydrae.clepsydrae.domain.shouldNotifyPomodoro
import oqk.ananke.clepsydrae.clepsydrae.domain.strlapsed
import oqk.ananke.clepsydrae.core.iPhi
import oqk.ananke.clepsydrae.core.phi
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ClepsydraScope.MorphingTimer(modifier: Modifier = Modifier.Companion) {
    val clepsydra = st.coreClepsydra ?: return

    val color by animateColorAsState(
        targetValue = if (clepsydra.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )
    var elapsed by remember { mutableStateOf("00:00") }

    LaunchedEffect(clepsydra.lastStateChange) {
        while (true) {
            elapsed = clepsydra.strlapsed()
            if (clepsydra.shouldNotifyPomodoro() && !st.pomodoroNotifying) {
                onAction(ClepsydraScreenAction.OnPomodoroThresholdCrossed)
            }
            delay(1.seconds)
        }
    }

    val pulse by animateFloatAsState(
        targetValue = if (clepsydra.isActive) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow
        )
    )

    Column(modifier) {


        Box(Modifier.weight(iPhi)) { }

        Box(Modifier.weight(phi)) {
            Surface(
                modifier = Modifier.sq()
                    .clip(CircleShape),
                shape = CircleShape,
                color = color,
                shadowElevation = 6.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                        .graphicsLayer {
                            scaleX = pulse
                            scaleY = pulse
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!clepsydra.isActive) {
                        IconButton(
                            modifier = Modifier.size(24.dp),
                            onClick = { onAction(ClepsydraScreenAction.OnCloseCoreClepsydra) }) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    } else {
                        Spacer(Modifier.size(24.dp))
                    }


                    TonalToggleButton(
                        modifier = Modifier.fillMaxHeight(.3f),
                        checked = clepsydra.isActive,
                        onCheckedChange = { onAction(ClepsydraScreenAction.ToggleDiatesi) },
                        colors = ToggleButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.tertiaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContentColor = MaterialTheme.colorScheme.onSurface,
                            checkedContainerColor = MaterialTheme.colorScheme.primary,
                            checkedContentColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(iPhi), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = elapsed.take(2),
                                style = MaterialTheme.typography.labelSmallEmphasized,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(phi),
                                autoSize = TextAutoSize.StepBased(1.sp, stepSize = 0.001.sp)
                            )
                            ClepsydraTimerSeparator(Modifier.weight(iPhi).fillMaxHeight(.75f), clepsydra.isActive)

                            if (elapsed.length == 8) {
                                Text(
                                    text = elapsed.substring(3, 5),
                                    style = MaterialTheme.typography.displayLargeEmphasized,
                                    maxLines = 1,
                                    modifier = Modifier.weight(phi),
                                    autoSize = TextAutoSize.StepBased(1.sp, stepSize = 0.001.sp)

                                )
                                ClepsydraTimerSeparator(Modifier.weight(iPhi).fillMaxHeight(.75f), clepsydra.isActive)
                            }
                            Text(
                                text = elapsed.takeLast(2),
                                style = MaterialTheme.typography.labelSmallEmphasized,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(phi),
                                autoSize = TextAutoSize.StepBased(1.sp, stepSize = 0.001.sp)
                            )
                        }
                    }

                    if (clepsydra.isActive) {
                        IconButton(
                            modifier = Modifier.size(24.dp),
                            onClick = { onAction(ClepsydraScreenAction.OnCloseCoreClepsydra) }) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    } else {
                        Spacer(Modifier.size(24.dp))
                    }
                }
            }
        }

        Box(Modifier.weight(iPhi)) { }
    }
}

@Composable
private fun ClepsydraTimerSeparator(modifier: Modifier = Modifier.Companion, isActive: Boolean) {
    Column(modifier, verticalArrangement = Arrangement.SpaceAround) {
        Icon(
            if (isActive) Icons.Default.PlayArrow else Icons.Default.Stop,
            "player",
            Modifier.weight(1f)
        )
        Icon(
            if (isActive) Icons.Default.PlayArrow else Icons.Default.Stop,
            "player",
            Modifier.weight(1f)
        )
    }
}