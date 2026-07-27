package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import oqk.ananke.clepsydrae.clepsydrae.domain.asText
import oqk.ananke.clepsydrae.core.iPhi
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ClepsydraScope.ClepsydraTimeBar(modifier: Modifier = Modifier.Companion) {
    val current = st.coreClepsydra

    // ====== 1) LOCAL CLOCK LOGIC ======
    var nowText by remember { mutableStateOf("--:--:--") }
    LaunchedEffect(Unit) {
        while (true) {
            nowText = st.startOfDay?.elapsedNow()?.asText(true) ?: "--:--:--"
            delay(1.seconds)
        }
    }

    // ====== 2) LOCAL PROGRESS LOGIC ======
    val progress by produceState<Float?>(initialValue = null, current) {
        if (current?.fin != null) {
            while (true) {
                val elapsed = current.init.elapsedNow().inWholeMilliseconds.toFloat()
                val total = (current.fin.elapsedNow() - current.init.elapsedNow()).inWholeMilliseconds.toFloat()
                value = if (total > 0) (elapsed / total).coerceIn(0f, 1f) else 0f
                delay(500.milliseconds)
            }
        } else {
            value = null
        }
    }

    val haptic = LocalHapticFeedback.current

    SmallExtendedFloatingActionButton(
        modifier = modifier,
        icon = @Composable {
            Icon(
                imageVector = Icons.Default.EditNote,
                "new note at $nowText",
                modifier = Modifier.size(28.dp)
            )
        },
        text = @Composable {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = nowText.take(5),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    maxLines = 1,
                )
                Text(
                    text = nowText.takeLast(3),
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = iPhi),
                    maxLines = 1,
                )
            }
        },
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            onAction(ClepsydraScreenAction.OnCreateNoteAtTime(nowText))
        }
    )
}