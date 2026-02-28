package oqk.ananke.clepsydrae.journal.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScope
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScreenAction
import oqk.ananke.clepsydrae.journal.domain.TimeStamp
import oqk.ananke.clepsydrae.journal.domain.TimelineItem
import kotlin.time.ExperimentalTime

@Composable
fun ClepsydraScope.ClepsydraJournal(modifier: Modifier = Modifier.Companion) {
    // Recalculate timeline only when the raw data changes
    val timelineItems = remember(st.journalOfDay) {
        st.journalOfDay.buildTimeline()
    }

    // Outer container is just a Surface now, no giant ElevatedCard wrapping the entire list.
    // This allows each item to truly be its own Card.
    Surface(
        modifier = modifier.fillMaxSize().adaptivePadding(),
        color = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier.Companion.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp) // Space for FABs or bottom bars
        ) {
            items(
                items = timelineItems,
                key = { "${it.time}-${it.depth}" }
            ) { item ->
                TimelineItemRow(
                    item = item,
                    isLast = item == timelineItems.last(),
                    onContentChanged = { initTime, newText, endTime ->
                        onAction(ClepsydraScreenAction.OnSetEntryAtTime(initTime, newText to endTime))
                    },
                    onDelete = { onAction(ClepsydraScreenAction.OnDeleteEntryAtTime(item.time)) },
                    onSetEndTime = { content, newEndTime ->
                        onAction(ClepsydraScreenAction.OnSetEntryAtTime(item.time, content to newEndTime))
                    }
                )
            }
        }
    }
}

@Composable
fun ClepsydraScope.TimelineItemRow(
    item: TimelineItem,
    isLast: Boolean,
    onContentChanged: (TimeStamp, String, String?) -> Unit,
    onDelete: () -> Unit,
    onSetEndTime: (String, TimeStamp?) -> Unit
) {
    val timeStr = item.time
    val depth = item.depth
    val isGap = item is TimelineItem.Gap

    val content = if (item is TimelineItem.ExistingEntry) item.content else ""
    val endTime = if (item is TimelineItem.ExistingEntry) item.endTime else null
    val isActive = !isGap && endTime == null

    // Theme colors for the canvas drawing
    val primaryColor = MaterialTheme.colorScheme.primary
    val variantColor = MaterialTheme.colorScheme.surfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

    // Replaces IntrinsicSize.Min with a naturally sizing Box.
    // The Box expands based on the Card's text height. This fixes layout jumps and focus weirdness.
    Box(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .drawBehind {
                val lineSpacing = 20.dp.toPx()
                val lineXBase = 24.dp.toPx()
                val dotRadius = 6.dp.toPx()
                val strokeWidth = 2.dp.toPx()

                // Align dot visually with the Card's time header
                val dotY = 32.dp.toPx()

                // Draw connecting lines and dots on the left
                for (i in 0..depth) {
                    val xPos = lineXBase + (i * lineSpacing)

                    // If it's the last item, the line stops at the dot. Otherwise, it goes to the bottom.
                    val endY = if (isLast && i == depth) dotY else size.height

                    drawLine(
                        color = variantColor,
                        start = Offset(xPos, 0f),
                        end = Offset(xPos, endY),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Companion.Round
                    )

                    if (i == depth) {
                        // Clear line behind dot
                        drawCircle(
                            color = surfaceColor,
                            radius = dotRadius + 3.dp.toPx(),
                            center = Offset(xPos, dotY)
                        )
                        // The Dot
                        drawCircle(
                            color = if (isGap) variantColor else primaryColor,
                            radius = dotRadius,
                            center = Offset(xPos, dotY)
                        )
                        if (isGap) {
                            // Donut hole for Gaps
                            drawCircle(
                                color = surfaceColor,
                                radius = dotRadius * 0.5f,
                                center = Offset(xPos, dotY)
                            )
                        }
                    }
                }
            }
    ) {
        // Pushes the card to the right leaving space for the timeline lines
        val paddingStart = 48.dp + (depth * 20).dp

        TimestampCard(
            modifier = Modifier.Companion.padding(start = paddingStart, end = 16.dp, bottom = 16.dp),
            time = timeStr,
            content = content,
            endTime = endTime,
            isGap = isGap,
            isActive = isActive,
            onContentChanged = { txt -> onContentChanged(timeStr, txt, endTime) },
            onDelete = onDelete,
            onFinishEntry = { newEndTime -> onSetEndTime(content, newEndTime) }
        )
    }
}

@Composable
fun ClepsydraScope.TimestampCard(
    modifier: Modifier = Modifier.Companion,
    time: String,
    content: String,
    endTime: String?,
    isGap: Boolean,
    isActive: Boolean,
    onContentChanged: (String) -> Unit,
    onDelete: () -> Unit,
    onFinishEntry: (newEndTime : TimeStamp?) -> Unit
) {
    val textState = rememberTextFieldState(initialText = content)

    // Sync external changes safely without destroying focus
    LaunchedEffect(content) {
        if (textState.text.toString() != content) {
            textState.setTextAndPlaceCursorAtEnd(content)
        }
    }

    // Auto-save debounce
    LaunchedEffect(textState.text) {
        val currentText = textState.text.toString()
        if (currentText != content) {
            delay(600)
            onContentChanged(currentText)
        }
    }

    // A true standalone card wrapper
    Card(
        modifier = modifier.animateContentSize(),
        shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isGap -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) // Subtle for empty gaps
                isActive -> MaterialTheme.colorScheme.surfaceContainerHigh
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive && !isGap) 3.dp else 0.dp),
        border = if (isActive && !isGap) BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        ) else null
    ) {
        Column(modifier = Modifier.Companion.padding(16.dp)) {

            // --- TIME HEADER INSIDE THE CARD ---
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                verticalAlignment = Alignment.Companion.CenterVertically
            ) {
                Text(
                    text = time.take(5),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Companion.Bold,
                    color = if (isGap) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = time.takeLast(3),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.Companion.padding(start = 2.dp)
                )

                Spacer(modifier = Modifier.Companion.weight(1f))

                if (isGap) {
                    Text(
                        text = "Tap to add entry",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                } else if (!isActive) {
                    Icon(
                        Icons.Default.Done,
                        null,
                        modifier = Modifier.Companion.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.Companion.width(4.dp))
                    Text(
                        text = "Ended $endTime",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.Companion.height(8.dp))

            // --- TEXT CONTENT ---
            BasicTextField(
                state = textState,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 24.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorator = { innerTextField ->
                    if (textState.text.isEmpty()) {
                        Text(
                            text = if (isGap) "What's happening?" else "Write something...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            )

            // --- BOTTOM ACTIONS ---
            // Hide bottom action row completely on empty gaps until the user begins typing
            if (!isGap || textState.text.isNotEmpty()) {
                Spacer(modifier = Modifier.Companion.height(12.dp))
                Row(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.Companion.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                            modifier = Modifier.Companion.size(18.dp)
                        )
                    }

                    if (isActive) {
                        FinishButton(timeInit = time, onFinish = onFinishEntry)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun ClepsydraScope.FinishButton(timeInit: String, onFinish: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    val initH = timeInit.take(2).toIntOrNull() ?: 0
    val initM = timeInit.drop(3).take(2).toIntOrNull() ?: 0

    val currentHours = st.startOfDay?.elapsedNow()?.inWholeHours?.toInt()?.rem(24) ?: 0
    val currentMinutes = st.startOfDay?.elapsedNow()?.inWholeMinutes?.toInt()?.rem(60) ?: 0

    val timeState = rememberTimePickerState(
        initialHour = currentHours,
        initialMinute = currentMinutes
    )

    LaunchedEffect(timeState.hour, timeState.minute) {
        if (timeState.hour < initH || (timeState.hour == initH && timeState.minute < initM + 1)) {
            timeState.hour = initH % 24
            timeState.minute = (initM + 1) % 60
        }
    }

    Button(
        onClick = { showDialog = true },
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        modifier = Modifier.Companion.height(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Text("Finish", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.Companion.width(4.dp))
        Icon(Icons.Default.Close, null, modifier = Modifier.Companion.size(12.dp))
    }

    if (showDialog) {
        TimePickerDialog(
            state = timeState,
            onDismiss = { showDialog = false },
            onConfirm = {
                // Formatting cleanly bypassing locale issues on some Android variants
                val hh = timeState.hour.toString().padStart(2, '0')
                val mm = timeState.minute.toString().padStart(2, '0')
                onFinish("$hh:$mm:00")
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    state: TimePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            modifier = Modifier.Companion.wrapContentWidth()
        ) {
            Column(
                modifier = Modifier.Companion.padding(24.dp),
                horizontalAlignment = Alignment.Companion.CenterHorizontally
            ) {
                Text(
                    "Set End Time",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.Companion.height(16.dp))

                TimePicker(state = state)

                Spacer(Modifier.Companion.height(24.dp))

                Row(modifier = Modifier.Companion.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.Companion.width(8.dp))
                    Button(onClick = onConfirm) { Text("Confirm") }
                }
            }
        }
    }
}