package oqk.ananke.clepsydrae.journal.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.delay
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScope
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScreenAction
import oqk.ananke.clepsydrae.core.iPhi
import oqk.ananke.clepsydrae.journal.domain.TimeStamp
import oqk.ananke.clepsydrae.journal.domain.TimelineItem
import kotlin.time.ExperimentalTime

@Composable
fun ClepsydraScope.ClepsydraJournal(modifier: Modifier = Modifier) {
    val timelineItems = remember(st.journalOfDay) { st.journalOfDay.buildTimeline() }

    ElevatedCard(Modifier.widthIn(max = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp).adaptivePadding(), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = iPhi)),) {
        LazyColumn(
            modifier = modifier.fillMaxSize().adaptivePadding(),
            contentPadding = PaddingValues(16.dp, 24.dp, 16.dp, 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(timelineItems, key = { it.time }) { item ->
                TimelineEntry(
                    item = item,
                    onUpdate = { text, end ->
                        onAction(ClepsydraScreenAction.OnSetEntryAtTime(item.time, text to end))
                    },
                    onDelete = { onAction(ClepsydraScreenAction.OnDeleteEntryAtTime(item.time)) }
                )
            }
        }
    }
}

@Composable
fun ClepsydraScope.TimelineEntry(
    item: TimelineItem,
    onUpdate: (String, String?) -> Unit,
    onDelete: () -> Unit
) {
    val isGap = item is TimelineItem.Gap
    val content = (item as? TimelineItem.ExistingEntry)?.content ?: ""
    val endTime = (item as? TimelineItem.ExistingEntry)?.endTime
    var text by remember { mutableStateOf(content) }

    LaunchedEffect(content) { text = content }

    LaunchedEffect(text) {
        if (text != content) {
            delay(600)
            onUpdate(text, endTime)
        }
    }

    Row(Modifier.fillMaxWidth()) {
        if (item.depth > 0) {
            repeat(item.depth) {
                Box(
                    Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(0.5f),
                                    MaterialTheme.colorScheme.tertiary.copy(0.2f)
                                )
                            )
                        )
                )
            }
        }

        Column(
            Modifier.weight(1f).widthIn(max = 600.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = when {
                    isGap -> MaterialTheme.colorScheme.secondaryContainer
                    endTime == null -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.tertiaryContainer
                },
                tonalElevation = 2.dp
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        item.time.take(5),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        item.time.takeLast(3),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 120.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 1.dp
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text(
                                if (isGap) "What's happening?" else "Write something...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f)
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.size(36.dp))
                if (endTime == null) {
                    EndTimeButton(item.time, endTime) { onUpdate(text, it) }
                } else {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Check, null, Modifier.size(14.dp))
                            Text(
                                endTime.take(5),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                endTime.takeLast(3),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun ClepsydraScope.EndTimeButton(startTime: TimeStamp, endTime: String?, onSet: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    val initH = startTime.take(2).toIntOrNull() ?: 0
    val initM = startTime.drop(3).take(2).toIntOrNull() ?: 0
    val currentH = st.startOfDay?.elapsedNow()?.inWholeHours?.toInt()?.rem(24) ?: 0
    val currentM = st.startOfDay?.elapsedNow()?.inWholeMinutes?.toInt()?.rem(60) ?: 0

    val timeState = rememberTimePickerState(currentH, currentM)

    LaunchedEffect(timeState.hour, timeState.minute) {
        if (timeState.hour < initH || (timeState.hour == initH && timeState.minute < initM + 1)) {
            timeState.hour = initH % 24
            timeState.minute = (initM + 1) % 60
        }
    }

    Surface(
        onClick = { showDialog = true },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Done, null, Modifier.size(16.dp))
            Text("Finish", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }

    if (showDialog) {
        TimePickerDialog(
            state = timeState,
            onDismiss = { showDialog = false },
            onConfirm = {
                val hh = timeState.hour.toString().padStart(2, '0')
                val mm = timeState.minute.toString().padStart(2, '0')
                onSet("$hh:$mm:00")
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(state: TimePickerState, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Set End Time",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(24.dp))
                TimePicker(state)
                Spacer(Modifier.height(28.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
                    FilledTonalButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = onConfirm) { Text("Confirm") }
                }
            }
        }
    }
}
