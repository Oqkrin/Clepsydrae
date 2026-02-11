package oqk.ananke.clepsydrae

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope

@Composable
fun FrameWindowScope.ClepsydraWindowTitleBar(
    title: String,
    isAlwaysOnTop: Boolean,
    isCompact: Boolean, // Derived from window size
    onToggleAlwaysOnTop: () -> Unit,
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    onNavigateCalendar: () -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
) {
    WindowDraggableArea {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp) // Slightly taller for better touch target
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Window Mode Controls
            IconButton(onClick = onToggleAlwaysOnTop, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = if (isAlwaysOnTop) Icons.Default.Lock else Icons.Default.PushPin,
                    contentDescription = "Toggle Always On Top",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = if (isAlwaysOnTop) 1f else 0.5f)
                )
            }

            // Center: Date Navigation
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isCompact) {
                    IconButton(onClick = onPreviousDay, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous Day", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clickable(onClick = onNavigateCalendar)
                )

                if (isCompact) {
                    IconButton(onClick = onNextDay, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next Day", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            // Right: Window Actions
            Row {
                IconButton(onClick = onMinimize, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Minimize, "Minimize", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, "Close", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}