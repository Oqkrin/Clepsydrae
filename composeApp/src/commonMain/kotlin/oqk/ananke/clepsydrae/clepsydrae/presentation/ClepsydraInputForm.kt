package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.distinctUntilChanged
import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra
import oqk.ananke.clepsydrae.clepsydrae.domain.asTimeMarkFromStartOfDay
import oqk.ananke.clepsydrae.core.debugBorder
import oqk.ananke.clepsydrae.core.iPhi
import oqk.ananke.clepsydrae.core.phi
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.TimeMark
import kotlin.time.TimeSource

// ============================================================================
// DOMAIN MODELS
// ============================================================================

data class InputClepsydraFormState(
    val presetClepsydra: Clepsydra? = null,
    val name: String = presetClepsydra?.name ?: "",
    val note: String = presetClepsydra?.note ?: "",
    val tags: String = presetClepsydra?.tags?.joinToString("#", "#") ?: "#",
    val initHours: Duration? = null,
    val initMinutes: Duration? = null,
    val initSeconds: Duration? = null,
    val initTimeMark: TimeMark? = null,
    val finHours: Duration? = null,
    val finMinutes: Duration? = null,
    val finSeconds: Duration? = null,
    val finTimeMark: TimeMark? = null,
    val activeGoal: Duration = Duration.ZERO,
    val passiveGoal: Duration = Duration.ZERO,
    val startActive: Boolean = false
)

enum class TimeInputMode {
    NONE, DURATION, TIMESTAMP;

    fun next(): TimeInputMode = entries[(ordinal + 1) % entries.size]
}

// ============================================================================
// MAIN FORM COMPONENT
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ClepsydraScope.ClepsydraInputForm(modifier: Modifier = Modifier) {
    var formState by remember { mutableStateOf(InputClepsydraFormState()) }
    val nameState = rememberTextFieldState(formState.name)
    val noteState = rememberTextFieldState(formState.note)
    val tagsState = rememberTextFieldState(formState.tags)

    // Sync text fields back to form state
    LaunchedEffect(Unit) {
        snapshotFlow { nameState.text.toString() to noteState.text.toString() }
            .distinctUntilChanged()
            .collect { (name, note) ->
                formState = formState.copy(name = name, note = note)
            }
    }

    Box(modifier.debugBorder()) {
        if (!isShort) {
            ClepsydraInputFormContent(
                formState = formState,
                onFormStateChange = { formState = it },
                nameState = nameState,
                noteState = noteState,
                tagsState = tagsState,
                onSubmit = {
                    onAction(
                        ClepsydraScreenAction.OnCreateClepsydra(
                            presetClepsydra = null,
                            name = formState.name,
                            note = formState.note,
                            tags = formState.tags.split("#").filter { it.isNotEmpty() }.toSet(),
                            now = TimeSource.Monotonic.markNow(),
                            initHours = formState.initHours,
                            initMinutes = formState.initMinutes,
                            initSeconds = formState.initSeconds,
                            init = formState.initTimeMark,
                            finHours = formState.finHours,
                            finMinutes = formState.finMinutes,
                            finSeconds = formState.finSeconds,
                            fin = formState.finTimeMark,
                            activeGoal = formState.activeGoal,
                            passiveGoal = formState.passiveGoal,
                            startActive = formState.startActive
                        )
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ClepsydraScope.ClepsydraInputFormContent(
    formState: InputClepsydraFormState,
    onFormStateChange: (InputClepsydraFormState) -> Unit,
    nameState: TextFieldState,
    noteState: TextFieldState,
    tagsState: TextFieldState,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header Section - Name, Note & Tag (Compact)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 16.adp(), vertical = 8.adp()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.adp())
        ) {
            ClepsydraNameField(
                state = nameState,
                modifier = Modifier.fillMaxWidth(iPhi)
            )

            ClepsydraNoteAndTagField(
                noteState = noteState,
                tagsState = tagsState,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Goals Section (Middle)
        ClepsydraGoalsSection(
            formState = formState,
            onFormStateChange = onFormStateChange,
            modifier = Modifier.weight(1f)
        )

        // Time Configuration & Submit (Bottom, larger portion)
        ClepsydraTimeConfigSection(
            formState = formState,
            onFormStateChange = onFormStateChange,
            onSubmit = onSubmit,
            modifier = Modifier.weight(phi)
        )
    }
}

// ============================================================================
// SECTION COMPONENTS
// ============================================================================
@Composable
private fun ClepsydraScope.ClepsydraNameField(
    state: TextFieldState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.adp(), vertical = 8.adp()),
            contentAlignment = Alignment.Center
        ) {
            if (state.text.isEmpty()) {
                Text(
                    text = "Clepsydra Name",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            BasicTextField(
                state = state,
                lineLimits = TextFieldLineLimits.SingleLine,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ClepsydraScope.ClepsydraNoteAndTagField(
    noteState: TextFieldState,
    tagsState: TextFieldState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.adp())
    ) {
        OutlinedTextField(
            state = noteState,
            modifier = Modifier.weight(1f),
            label = { Text("Note", style = MaterialTheme.typography.labelSmall) },
            lineLimits = TextFieldLineLimits.SingleLine,
            leadingIcon = {
                Icon(
                    Icons.AutoMirrored.Filled.Notes,
                    null,
                    Modifier.size(18.adp())
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            textStyle = MaterialTheme.typography.bodySmall
        )

        OutlinedTextField(
            state = tagsState,
            modifier = Modifier.weight(1f),
            label = { Text("Tag", style = MaterialTheme.typography.labelSmall) },
            lineLimits = TextFieldLineLimits.SingleLine,
            leadingIcon = {
                Icon(
                    Icons.Default.Tag,
                    null,
                    Modifier.size(18.adp())
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            textStyle = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ClepsydraScope.ClepsydraGoalsSection(
    formState: InputClepsydraFormState,
    onFormStateChange: (InputClepsydraFormState) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Active Goal
        ClepsydraGoalCard(
            label = "Active Goal",
            duration = formState.activeGoal,
            onDurationChange = { onFormStateChange(formState.copy(activeGoal = it)) },
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.weight(phi, false) // Golden ratio weight
        )

        // Start Mode Toggle (smallest, centered)
        ClepsydraStartModeToggle(
            startActive = formState.startActive,
            onToggle = { onFormStateChange(formState.copy(startActive = it)) },
            modifier = Modifier.weight(iPhi)
        )

        // Passive Goal
        ClepsydraGoalCard(
            label = "Passive Goal",
            duration = formState.passiveGoal,
            onDurationChange = { onFormStateChange(formState.copy(passiveGoal = it)) },
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.weight(phi, false) // Golden ratio weight
        )
    }
}

@Composable
private fun ClepsydraScope.ClepsydraGoalCard(
    label: String,
    duration: Duration,
    onDurationChange: (Duration) -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedIconButton(
                    onClick = {
                        if (duration >= 1.minutes) onDurationChange(duration - 1.minutes)
                    }
                ) {
                    Icon(
                        Icons.Default.Remove,
                        "Decrease"
                    )
                }

                Text(
                    text = duration.toComponents { h, m, s, _ ->
                        when {
                            h > 0 -> "${h}h ${m}m"
                            m > 0 -> "${m}m ${s}s"
                            else -> "${s}s"
                        }
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedIconButton(
                    onClick = { onDurationChange(duration + 1.minutes) },
                ) {
                    Icon(
                        Icons.Default.Add,
                        "Increase"
                    )
                }
            }
        }
    }
}

@Composable
private fun ClepsydraScope.ClepsydraStartModeToggle(
    startActive: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Start as",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Switch(
            checked = startActive,
            onCheckedChange = onToggle,
            thumbContent = {
                Icon(
                    imageVector = if (startActive) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            }
        )
        Text(
            text = if (startActive) "Active" else "Passive",
            style = MaterialTheme.typography.labelMedium,
            color = if (startActive)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ClepsydraScope.ClepsydraTimeConfigSection(
    formState: InputClepsydraFormState,
    onFormStateChange: (InputClepsydraFormState) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        // Start Time
        ClepsydraTimePicker(
            label = "Start",
            hours = formState.initHours,
            minutes = formState.initMinutes,
            seconds = formState.initSeconds,
            timeMark = formState.initTimeMark,
            onTimeChanged = { h, m, s, tm ->
                onFormStateChange(
                    formState.copy(
                        initHours = h,
                        initMinutes = m,
                        initSeconds = s,
                        initTimeMark = tm
                    )
                )
            },
            modifier = Modifier.weight(phi, false)
        )

        // Submit Button (center, smallest)
        Column(
            modifier = Modifier.weight(iPhi),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SplitButtonLayout(
                leadingButton = {
                    SplitButtonDefaults.LeadingButton(
                        onClick = onSubmit
                    ) {
                        Icon(Icons.Default.Add, "Create")
                    }
                },
                trailingButton = {
                    SplitButtonDefaults.TrailingButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, "More options")
                    }
                },
                spacing = 2.dp
            )
        }

        // End Time
        ClepsydraTimePicker(
            label = "End",
            hours = formState.finHours,
            minutes = formState.finMinutes,
            seconds = formState.finSeconds,
            timeMark = formState.finTimeMark,
            onTimeChanged = { h, m, s, tm ->
                onFormStateChange(
                    formState.copy(
                        finHours = h,
                        finMinutes = m,
                        finSeconds = s,
                        finTimeMark = tm
                    )
                )
            },
            modifier = Modifier.weight(phi, false)
        )
    }
}

// ============================================================================
// TIME PICKER COMPONENT
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClepsydraScope.ClepsydraTimePicker(
    label: String,
    hours: Duration?,
    minutes: Duration?,
    seconds: Duration?,
    timeMark: TimeMark?,
    onTimeChanged: (Duration?, Duration?, Duration?, TimeMark?) -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(TimeInputMode.NONE) }

    val hoursState = rememberTextFieldState(hours?.toInt(DurationUnit.HOURS).toString().replace("null", "00"))
    val minutesState = rememberTextFieldState(minutes?.toInt(DurationUnit.MINUTES).toString().replace("null", "00"))
    val secondsState = rememberTextFieldState(seconds?.toInt(DurationUnit.SECONDS).toString().replace("null", "00"))
    val timePickerState = rememberTimePickerState()

    var showTimePickerDialog by remember { mutableStateOf(false) }

    // Sync mode changes to parent
    LaunchedEffect(mode) {
        when (mode) {
            TimeInputMode.NONE -> onTimeChanged(null, null, null, null)

            TimeInputMode.DURATION -> {
                snapshotFlow {
                    Triple(
                        hoursState.text.toString(),
                        minutesState.text.toString(),
                        secondsState.text.toString()
                    )
                }.collect { (h, m, s) ->
                    onTimeChanged(
                        h.toIntOrNull()?.hours,
                        m.toIntOrNull()?.minutes,
                        s.toIntOrNull()?.seconds,
                        null
                    )
                }
            }

            TimeInputMode.TIMESTAMP -> {
                snapshotFlow {
                    timePickerState.hour to timePickerState.minute
                }.collect { (h, m) ->
                    val totalMinutes = (h * 60) + m
                    onTimeChanged(
                        null, null, null,
                        totalMinutes.minutes.asTimeMarkFromStartOfDay()
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.adp())
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            onClick = { mode = mode.next() },
            shape = RoundedCornerShape(16.adp()),
            color = when (mode) {
                TimeInputMode.NONE -> MaterialTheme.colorScheme.surfaceContainer
                TimeInputMode.DURATION -> MaterialTheme.colorScheme.primaryContainer
                TimeInputMode.TIMESTAMP -> MaterialTheme.colorScheme.tertiaryContainer
            },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(iPhi)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.adp()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = when (mode) {
                        TimeInputMode.NONE -> Icons.Default.HourglassEmpty
                        TimeInputMode.DURATION -> Icons.Default.Timer
                        TimeInputMode.TIMESTAMP -> Icons.Default.AccessTime
                    },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(iPhi * iPhi)
                )

                Spacer(Modifier.width(8.adp()))

                ClepsydraTimeDisplay(
                    mode = mode,
                    hoursState = hoursState,
                    minutesState = minutesState,
                    secondsState = secondsState,
                    timePickerState = timePickerState,
                    onShowTimePicker = { showTimePickerDialog = true }
                )
            }
        }
    }

    // Time Picker Dialog
    if (showTimePickerDialog) {
        ClepsydraTimePickerDialog(
            state = timePickerState,
            onDismiss = { showTimePickerDialog = false },
            onConfirm = { showTimePickerDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClepsydraScope.ClepsydraTimeDisplay(
    mode: TimeInputMode,
    hoursState: TextFieldState,
    minutesState: TextFieldState,
    secondsState: TextFieldState,
    timePickerState: TimePickerState,
    onShowTimePicker: () -> Unit
) {
    AnimatedContent(
        targetState = mode,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "TimeDisplay"
    ) { currentMode ->
        when (currentMode) {
            TimeInputMode.NONE -> {
                Text(
                    text = "Not set",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TimeInputMode.DURATION -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.adp())
                ) {
                    DurationTextField(hoursState, maxValue = 99)
                    Text(":", fontWeight = FontWeight.Bold)
                    DurationTextField(minutesState, maxValue = 59)
                    Text(":", fontWeight = FontWeight.Bold)
                    DurationTextField(secondsState, maxValue = 59)
                }
            }

            TimeInputMode.TIMESTAMP -> {
                TextButton(onClick = onShowTimePicker) {
                    Text(
                        text = "${timePickerState.hour.toString().padStart(2, '0')}:" +
                                "${timePickerState.minute.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClepsydraScope.ClepsydraTimePickerDialog(
    state: TimePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(iPhi)
                .wrapContentHeight()
                .padding(16.adp()),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.adp()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.adp())
            ) {
                Text(
                    text = "Select Time",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                TimePicker(state = state)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.adp())
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(onClick = onConfirm) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

// ============================================================================
// DURATION TEXT FIELD COMPONENT
// ============================================================================

@Composable
fun ClepsydraScope.DurationTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    maxValue: Int = 99
) {
    val haptic = LocalHapticFeedback.current
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }

    val currentInt = state.text.toString().toIntOrNull() ?: 0
    val prevInt = (currentInt - 1).coerceAtLeast(0)
    val nextInt = (currentInt + 1).coerceAtMost(maxValue)

    Box(
        modifier = modifier
            .wrapContentWidth()
            .fillMaxHeight()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Scroll) {
                            val delta = event.changes.first().scrollDelta.y
                            val increment = if (delta < 0) 1 else -1
                            updateDurationValue(state, increment, maxValue, haptic)
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = { accumulatedDrag = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        accumulatedDrag += dragAmount
                        if (abs(accumulatedDrag) > 30f) {
                            val increment = if (accumulatedDrag < 0) 1 else -1
                            updateDurationValue(state, increment, maxValue, haptic)
                            accumulatedDrag = 0f
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            // Preview number above
            Text(
                text = prevInt.toString().padStart(2, '0'),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.weight(iPhi)
            )

            // Current editable number
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHighest,
                        RoundedCornerShape(8.adp())
                    )
                    .padding(horizontal = 8.adp(), vertical = 4.adp())
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    state = state,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    lineLimits = TextFieldLineLimits.SingleLine,
                    inputTransformation = InputTransformation.maxLength(2).then {
                        if (!asCharSequence().all { it.isDigit() }) revertAllChanges()
                    },
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    decorator = { it() }
                )
            }

            // Preview number below
            Text(
                text = nextInt.toString().padStart(2, '0'),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.weight(iPhi)
            )
        }

        // Gradient fade overlay
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        0.25f to Color.Transparent,
                        0.75f to Color.Transparent,
                        1f to MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
        )
    }
}

private fun updateDurationValue(
    state: TextFieldState,
    increment: Int,
    maxValue: Int,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback?
) {
    val currentVal = state.text.toString().toIntOrNull() ?: 0
    val newVal = (currentVal + increment).coerceIn(0, maxValue)

    if (newVal != currentVal) {
        haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        state.edit {
            replace(0, length, newVal.toString().padStart(2, '0'))
        }
    }
}

// ============================================================================
// UTILITIES
// ============================================================================

fun Modifier.scale(scale: Float): Modifier = this.graphicsLayer(
    scaleX = scale,
    scaleY = scale
)