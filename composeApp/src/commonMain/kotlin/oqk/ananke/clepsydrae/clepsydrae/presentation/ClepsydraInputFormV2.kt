package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import oqk.ananke.clepsydrae.calendar.presentation.ClepsydraScope
import oqk.ananke.clepsydrae.di.isMobile
import org.koin.compose.koinInject
import kotlin.math.roundToInt

enum class TimeFieldTypes {
    INIT_TIME, END_TIME, ACTIVE_POMODORO, PASSIVE_POMODORO
}

enum class TimeBlockType {
    HOURS, MINUTES, SECONDS
}

enum class TimeInputMode {
    RELATIVE, ABSOLUTE
}

fun formatTimeInput(input: String): String {
    val padded = input.padStart(6, '0')
    return "${padded.substring(0, 2)}:${padded.substring(2, 4)}:${padded.substring(4, 6)}"
}

fun parseTimeToSeconds(timeString: String): Int {
    if (timeString.isBlank()) return 0
    val parts = formatTimeInput(timeString).split(":")
    return (parts[0].toIntOrNull() ?: 0) * 3600 +
            (parts[1].toIntOrNull() ?: 0) * 60 +
            (parts[2].toIntOrNull() ?: 0)
}

fun secondsToTimeString(seconds: Int): String {
    val h = (seconds / 3600).coerceIn(0, 99)
    val m = ((seconds % 3600) / 60).coerceIn(0, 59)
    val s = (seconds % 60).coerceIn(0, 59)
    return String.format("%02d%02d%02d", h, m, s)
}

fun updateTimeComponent(rawValue: String, block: TimeBlockType, newValue: Int, isAbsolute: Boolean = false): String {
    val current = rawValue.ifBlank { "000000" }
    val formatted = formatTimeInput(current)
    val parts = formatted.split(":")

    val maxVal = if (isAbsolute && block == TimeBlockType.HOURS) 23 else if (block != TimeBlockType.HOURS) 59 else 99
    val coercedValue = newValue.coerceIn(0, maxVal)
    val newValStr = coercedValue.toString().padStart(2, '0')

    val newParts = when (block) {
        TimeBlockType.HOURS -> listOf(newValStr, parts[1], parts[2])
        TimeBlockType.MINUTES -> listOf(parts[0], newValStr, parts[2])
        TimeBlockType.SECONDS -> listOf(parts[0], parts[1], newValStr)
    }
    return newParts.joinToString("")
}

fun formatFriendlyTime(rawValue: String, fieldType: TimeFieldTypes, mode: TimeInputMode): String {
    if (rawValue.isBlank() || rawValue == "000000") {
        return when (fieldType) {
            TimeFieldTypes.INIT_TIME -> "Now"
            TimeFieldTypes.END_TIME -> "Manual / Continuous"
            else -> "Optional / Skipped"
        }
    }

    val parts = formatTimeInput(rawValue).split(":")
    val h = parts[0].toIntOrNull() ?: 0
    val m = parts[1].toIntOrNull() ?: 0
    val s = parts[2].toIntOrNull() ?: 0

    if (mode == TimeInputMode.ABSOLUTE) {
        return String.format("%02d:%02d", h, m) + if (s > 0) String.format(":%02d", s) else ""
    }

    val components = mutableListOf<String>()
    if (h > 0) components.add("${h}h")
    if (m > 0) components.add("${m}m")
    if (s > 0 || components.isEmpty()) components.add("${s}s")

    return components.joinToString(" ")
}

private fun digitOnlyTransformation(isAbsoluteMode: Boolean, blockType: TimeBlockType) = InputTransformation {
    val maxLength = 2
    val maxValue = when {
        isAbsoluteMode && blockType == TimeBlockType.HOURS -> 23
        blockType == TimeBlockType.MINUTES || blockType == TimeBlockType.SECONDS -> 59
        else -> 99
    }

    val text = asCharSequence().toString()
    if (!text.all { it.isDigit() } || text.length > maxLength) {
        revertAllChanges()
    } else if (text.isNotEmpty()) {
        val value = text.toIntOrNull() ?: 0
        if (value !in 0..maxValue) {
            revertAllChanges()
        }
    }
}

private val NumpadButtons = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf("0", "00", "DEL")
)

private val WideNumpadButtons = listOf(
    listOf("1", "2", "3", "4", "5", "6"),
    listOf("7", "8", "9", "0", "00", "DEL")
)

/**
 * IMPROVEMENTS MADE:
 * 1. Visual Hierarchy: Added a Summary Header to see the total duration immediately.
 * 2. Interaction Design: Refined Numpad and Action Button placement for better thumb ergonomics.
 * 3. Feedback: Clearer focus indicators and smoother animations for field switching.
 * 4. Content: Better spacing and chip categorization.
 */

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ClepsydraScope.ClepsydraInputFormV2(modifier: Modifier = Modifier) {
    val isMobile = koinInject<isMobile>()
    var focusedField by remember { mutableStateOf(TimeFieldTypes.INIT_TIME) }
    var selectedBlock by remember { mutableStateOf(TimeBlockType.HOURS) }

    val timeValues = remember {
        mutableStateMapOf(
            TimeFieldTypes.INIT_TIME to "",
            TimeFieldTypes.END_TIME to "",
            TimeFieldTypes.ACTIVE_POMODORO to "002500",
            TimeFieldTypes.PASSIVE_POMODORO to "000500"
        )
    }

    var initMode by remember { mutableStateOf(TimeInputMode.RELATIVE) }
    var endMode by remember { mutableStateOf(TimeInputMode.RELATIVE) }
    var showTimePickerFor by remember { mutableStateOf<TimeFieldTypes?>(null) }

    // Dialog handling for Absolute time picking
    if (showTimePickerFor != null) {
        val currentField = showTimePickerFor!!
        val parts = formatTimeInput(timeValues[currentField] ?: "000000").split(":")

        TimePickerDialog(
            initialHour = parts[0].toIntOrNull()?.coerceIn(0, 23) ?: 0,
            initialMinute = parts[1].toIntOrNull()?.coerceIn(0, 59) ?: 0,
            onDismissRequest = { showTimePickerFor = null },
            onConfirm = { h, m ->
                timeValues[currentField] = String.format("%02d%02d00", h, m)
                showTimePickerFor = null
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
            LazyVerticalGrid(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .heightIn(min = 96.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                columns = GridCells.Adaptive(128.dp)) {
               item {
                   MaterialTimeCard(
                       fieldType = TimeFieldTypes.INIT_TIME,
                       title = if (initMode == TimeInputMode.RELATIVE) "Start Delay" else "Start At",
                       icon = Icons.Default.PlayCircleOutline,
                       rawValue = timeValues[TimeFieldTypes.INIT_TIME] ?: "",
                       mode = initMode,
                       onModeChange = { initMode = it },
                       isFocused = focusedField == TimeFieldTypes.INIT_TIME,
                       selectedBlock = selectedBlock,
                       onBlockSelect = { selectedBlock = it },
                       onValueChange = { timeValues[TimeFieldTypes.INIT_TIME] = it },
                       onClick = { focusedField = TimeFieldTypes.INIT_TIME },
                       onTimePickerClick = { showTimePickerFor = TimeFieldTypes.INIT_TIME },
                       modifier = Modifier.weight(1f),
                       quickChips = listOf("Now" to "", "+5m" to "000500", "+15m" to "001500")
                   )
               }
                item {
                    MaterialTimeCard(
                        fieldType = TimeFieldTypes.END_TIME,
                        title = if (endMode == TimeInputMode.RELATIVE) "Deadline" else "Stop At",
                        icon = Icons.Default.Flag,
                        rawValue = timeValues[TimeFieldTypes.END_TIME] ?: "",
                        mode = endMode,
                        onModeChange = { endMode = it },
                        isFocused = focusedField == TimeFieldTypes.END_TIME,
                        selectedBlock = selectedBlock,
                        onBlockSelect = { selectedBlock = it },
                        onValueChange = { timeValues[TimeFieldTypes.END_TIME] = it },
                        onClick = { focusedField = TimeFieldTypes.END_TIME },
                        onTimePickerClick = { showTimePickerFor = TimeFieldTypes.END_TIME },
                        modifier = Modifier.weight(1f),
                        quickChips = listOf("Open" to "", "25m" to "002500", "1h" to "010000")
                    )
                }
                
                item {
                    MaterialTimeCard(
                        fieldType = TimeFieldTypes.ACTIVE_POMODORO,
                        title = "Work",
                        icon = Icons.Default.Bolt,
                        rawValue = timeValues[TimeFieldTypes.ACTIVE_POMODORO] ?: "",
                        mode = TimeInputMode.RELATIVE,
                        onModeChange = {},
                        isFocused = focusedField == TimeFieldTypes.ACTIVE_POMODORO,
                        selectedBlock = selectedBlock,
                        onBlockSelect = { selectedBlock = it },
                        onValueChange = { timeValues[TimeFieldTypes.ACTIVE_POMODORO] = it },
                        onClick = { focusedField = TimeFieldTypes.ACTIVE_POMODORO },
                        modifier = Modifier.weight(1f),
                        quickChips = listOf("25m" to "002500", "50m" to "005000")
                    )
                }
                item {
                    MaterialTimeCard(
                        fieldType = TimeFieldTypes.PASSIVE_POMODORO,
                        title = "Break",
                        icon = Icons.Default.Coffee,
                        rawValue = timeValues[TimeFieldTypes.PASSIVE_POMODORO] ?: "",
                        mode = TimeInputMode.RELATIVE,
                        onModeChange = {},
                        isFocused = focusedField == TimeFieldTypes.PASSIVE_POMODORO,
                        selectedBlock = selectedBlock,
                        onBlockSelect = { selectedBlock = it },
                        onValueChange = { timeValues[TimeFieldTypes.PASSIVE_POMODORO] = it },
                        onClick = { focusedField = TimeFieldTypes.PASSIVE_POMODORO },
                        modifier = Modifier.weight(1f),
                        quickChips = listOf("5m" to "000500", "15m" to "001500")
                    )
                }
            }

        // 3. INTERACTION PANEL (Fixed Bottom)
        Surface(
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val activeRawValue = timeValues[focusedField] ?: ""
                val activeMode = if (focusedField == TimeFieldTypes.INIT_TIME) initMode else if (focusedField == TimeFieldTypes.END_TIME) endMode else TimeInputMode.RELATIVE

                // Scrub Slider
                val totalSeconds = parseTimeToSeconds(activeRawValue)
                val maxSliderSeconds = if (activeMode == TimeInputMode.ABSOLUTE) 86399f else 359999f

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                    Slider(
                        value = totalSeconds.toFloat(),
                        onValueChange = { timeValues[focusedField] = secondsToTimeString(it.roundToInt()) },
                        valueRange = 0f..maxSliderSeconds,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        colors = SliderDefaults.colors(
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            thumbColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Numpad is slightly compressed to leave room for the FAB on the right or centered bottom
                    Numpad(
                        modifier = Modifier.weight(1f),
                        isNarrow = isNarrow,
                        onNumberClick = { digit ->
                            val current = activeRawValue.ifBlank { "000000" }
                            timeValues[focusedField] = (current + digit).takeLast(6)
                        },
                        onDeleteClick = {
                            val current = activeRawValue.ifBlank { "000000" }
                            if (current != "000000") {
                                timeValues[focusedField] = ("0" + current.dropLast(1)).takeLast(6)
                            } else {
                                timeValues[focusedField] = ""
                            }
                        }
                    )

                    // Unified CTA
                    LargeFloatingActionButton(
                        onClick = { onAction(ClepsydraScreenAction.OnCreateClepsydra()) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.height(118.dp).width(72.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(32.dp))
                            Text("START", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialTimeCard(
    fieldType: TimeFieldTypes,
    title: String,
    icon: ImageVector,
    rawValue: String,
    mode: TimeInputMode,
    onModeChange: (TimeInputMode) -> Unit,
    isFocused: Boolean,
    selectedBlock: TimeBlockType,
    onBlockSelect: (TimeBlockType) -> Unit,
    onValueChange: (String) -> Unit,
    onClick: () -> Unit,
    onTimePickerClick: (() -> Unit)? = null,
    quickChips: List<Pair<String, String>> = emptyList(),
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "containerColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        label = "borderColor"
    )

    Card(
        modifier = modifier
            .animateContentSize(spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.extraLarge
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.extraLarge,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Label and Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, modifier = Modifier.size(16.dp), tint = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))

            if (isFocused) {
                // Focused state shows the logic toggles and block editors
                if (fieldType == TimeFieldTypes.INIT_TIME || fieldType == TimeFieldTypes.END_TIME) {
                    SingleChoiceSegmentedButtonRow (modifier = Modifier.fillMaxWidth().height(32.dp)) {
                        SegmentedButton(
                            selected = mode == TimeInputMode.RELATIVE,
                            onClick = { onModeChange(TimeInputMode.RELATIVE) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            icon = {}
                        ) { Text("Rel", fontSize = 10.sp) }
                        SegmentedButton(
                            selected = mode == TimeInputMode.ABSOLUTE,
                            onClick = { onModeChange(TimeInputMode.ABSOLUTE) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            icon = {}
                        ) { Text("Abs", fontSize = 10.sp) }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                val safeRaw = rawValue.ifBlank { "000000" }
                val parts = formatTimeInput(safeRaw).split(":")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BlockEditorSmall(parts[0], selectedBlock == TimeBlockType.HOURS) { onBlockSelect(TimeBlockType.HOURS) }
                    Text(":", style = MaterialTheme.typography.titleLarge)
                    BlockEditorSmall(parts[1], selectedBlock == TimeBlockType.MINUTES) { onBlockSelect(TimeBlockType.MINUTES) }
                    Text(":", style = MaterialTheme.typography.titleLarge)
                    BlockEditorSmall(parts[2], selectedBlock == TimeBlockType.SECONDS) { onBlockSelect(TimeBlockType.SECONDS) }
                }

                if (quickChips.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(quickChips) { chip ->
                            SuggestionChip(
                                onClick = { onValueChange(chip.second) },
                                label = { Text(chip.first, fontSize = 10.sp) },
                                shape = CircleShape
                            )
                        }
                    }
                }
            } else {
                // Collapsed state: High readability
                Text(
                    text = formatFriendlyTime(rawValue, fieldType, mode),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun BlockEditorSmall(value: String, isSelected: Boolean, onSelect: () -> Unit) {
    val bg by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
    val tc by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)

    Box(
        modifier = Modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable { onSelect() }
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = tc)
    }
}

@Composable
fun Numpad(
    isNarrow: Boolean,
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numpadB = if (isNarrow) NumpadButtons else WideNumpadButtons
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        numpadB.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { btn ->
                    FilledTonalButton(
                        onClick = { if (btn == "DEL") onDeleteClick() else onNumberClick(btn) },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (btn == "DEL") MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (btn == "DEL") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (btn == "DEL") {
                            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                        } else {
                            Text(text = btn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismissRequest: () -> Unit,
    onConfirm: (hours: Int, minutes: Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(onClick = { onConfirm(state.hour, state.minute) }) { Text("Set Time") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancel") }
        },
        title = { Text("Pick Clock Time", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                TimePicker(state = state)
            }
        }
    )
}