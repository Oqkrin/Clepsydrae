package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.input.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
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
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra
import oqk.ananke.clepsydrae.clepsydrae.domain.asTimeMarkFromStartOfDay
import oqk.ananke.clepsydrae.core.LocalSettings
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

/**
 * Form state for creating or editing a Clepsydra timer
 */
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

/**
 * Modes for time input: NONE (not set), DURATION (relative), TIMESTAMP (absolute)
 */
enum class TimeInputMode {
    NONE, DURATION, TIMESTAMP;

    fun next(): TimeInputMode = entries[(ordinal + 1) % entries.size]
}

// ============================================================================
// MAIN FORM COMPONENT
// ============================================================================

/**
 * Main input form for creating Clepsydra timers with start/end times and goals
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ClepsydraScope.ClepsydraInputForm(modifier: Modifier = Modifier) {
    val formState_ = retain { MutableStateFlow(InputClepsydraFormState()) }
    val formState by formState_.asStateFlow().collectAsState()

    LaunchedEffect(Unit) {
        snapshotFlow { formState }
            .distinctUntilChanged()
            .collect { /* Sync if needed */ }
    }

    Box(modifier = modifier.adaptivePadding().fillMaxWidth().adaptivePadding()) {
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.adaptivePadding()) {
            // Left side: Active goal and start time
            LeftTimeSection(
                formState = formState,
                onFormUpdate = formState_::update,
                modifier = Modifier.weight(phi)
            )

            // Center: Start mode toggle and create button
            CenterActionSection(
                formState = formState,
                onFormUpdate = formState_::update,
                modifier = Modifier.width(56.dp)
            )

            // Right side: Passive goal and end time
            RightTimeSection(
                formState = formState,
                onFormUpdate = formState_::update,
                modifier = Modifier.weight(phi)
            )
        }
    }
}

// ============================================================================
// FORM SECTIONS
// ============================================================================

@Composable
private fun ClepsydraScope.LeftTimeSection(
    formState: InputClepsydraFormState,
    onFormUpdate: (InputClepsydraFormState.() -> InputClepsydraFormState) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.End) {
        Column(horizontalAlignment = Alignment.End) {
            GoalCard(
                label = "Pomodoro Active",
                duration = formState.activeGoal,
                onDurationChange = { duration ->
                    onFormUpdate { copy(activeGoal = duration) }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                isTowardsLeft = true,
                modifier = Modifier.adaptivePadding(),
            )

            ClepsydraTimePicker(
                label = "Start",
                hours = formState.initHours,
                minutes = formState.initMinutes,
                seconds = formState.initSeconds,
                timeMark = formState.initTimeMark,
                onTimeChanged = { h, m, s, tm ->
                    onFormUpdate {
                        copy(
                            initHours = h,
                            initMinutes = m,
                            initSeconds = s,
                            initTimeMark = tm
                        )
                    }
                },
                isTowardsLeft = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ClepsydraScope.CenterActionSection(
    formState: InputClepsydraFormState,
    onFormUpdate: (InputClepsydraFormState.() -> InputClepsydraFormState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            "Start as",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )

        StartModeToggle(
            startActive = formState.startActive,
            onToggle = { startActive ->
                onFormUpdate { copy(startActive = startActive) }
            },
            modifier = Modifier.width(56.dp).aspectRatio(iPhi)
        )

        CreateButtonSection(
            formState = formState,
            modifier = Modifier.width(56.dp).wrapContentHeight()
        )
    }
}

@Composable
private fun ClepsydraScope.RightTimeSection(
    formState: InputClepsydraFormState,
    onFormUpdate: (InputClepsydraFormState.() -> InputClepsydraFormState) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Start) {
        Column {
            GoalCard(
                label = "Pomodoro Passive",
                duration = formState.passiveGoal,
                onDurationChange = { duration ->
                    onFormUpdate { copy(passiveGoal = duration) }
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                isTowardsLeft = false,
                modifier = Modifier.adaptivePadding()
            )

            ClepsydraTimePicker(
                label = "End",
                hours = formState.finHours,
                minutes = formState.finMinutes,
                seconds = formState.finSeconds,
                timeMark = formState.finTimeMark,
                onTimeChanged = { h, m, s, tm ->
                    onFormUpdate {
                        copy(
                            finHours = h,
                            finMinutes = m,
                            finSeconds = s,
                            finTimeMark = tm
                        )
                    }
                },
                isTowardsLeft = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ClepsydraScope.CreateButtonSection(
    formState: InputClepsydraFormState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy((-4).dp)
    ) {
        // Presets button (top cap)
        Surface(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth(.9f)
                .aspectRatio(4 / 3f),
            shape = MaterialShapes.SemiCircle.toShape(),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 6.dp,
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    Icons.Default.MoreHoriz,
                    contentDescription = "Presets",
                    modifier = Modifier.fillMaxSize(iPhi * iPhi),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Main create button
        val isFirstClepsydra = LocalSettings.current.isFirstClepsydra
        FloatingActionButton(
            onClick = {
                if (isFirstClepsydra) {
                    onAction(ClepsydraScreenAction.OnFirstClepsydraCreation)
                }
                onAction(createClepsydraAction(formState))
            },
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Create",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// ============================================================================
// GOAL CARD COMPONENT
// ============================================================================

/**
 * Card for displaying and editing Pomodoro goal durations
 */
@Composable
private fun ClepsydraScope.GoalCard(
    label: String,
    duration: Duration,
    onDurationChange: (Duration) -> Unit,
    containerColor: Color,
    isTowardsLeft: Boolean,
    modifier: Modifier = Modifier
) {
    val totalMinutes = duration.inWholeMinutes.toInt()
    val hoursState = rememberTextFieldState((totalMinutes / 60).toString())
    val minutesState = rememberTextFieldState((totalMinutes % 60).toString())

    // Sync state changes to duration
    LaunchedEffect(hoursState.text, minutesState.text) {
        val h = hoursState.text.toString().toIntOrNull() ?: 0
        val m = minutesState.text.toString().toIntOrNull() ?: 0
        onDurationChange(h.hours + m.minutes)
    }

    Surface(
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(20.dp),
        color = containerColor.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, containerColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isTowardsLeft) Arrangement.End else Arrangement.Start
        ) {
            if (!isTowardsLeft) {
                GoalLabel(label, containerColor, TextAlign.Start)
            }

            GoalTimeFields(
                hoursState = hoursState,
                minutesState = minutesState,
                containerColor = containerColor,
                isTowardsLeft = isTowardsLeft,
                modifier = Modifier.weight(1f, false)
            )

            if (isTowardsLeft) {
                GoalLabel(label, containerColor, TextAlign.End)
            }
        }
    }
}

@Composable
private fun GoalLabel(
    label: String,
    containerColor: Color,
    textAlign: TextAlign
) {
    Text(
        text = label.replace(" ", "\n").uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(textAlign = textAlign),
        fontSize = 7.sp,
        lineHeight = 8.sp,
        fontWeight = FontWeight.Black,
        color = containerColor,
        modifier = Modifier.width(50.dp)
    )
}

@Composable
private fun ClepsydraScope.GoalTimeFields(
    hoursState: TextFieldState,
    minutesState: TextFieldState,
    containerColor: Color,
    isTowardsLeft: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = modifier
    ) {
        val separator = @Composable {
            Text(
                ":",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = containerColor.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (isTowardsLeft) {
            DurationTextField(
                state = hoursState,
                containerColor = containerColor,
                label = "h",
                maxValue = 24,
                modifier = Modifier.width(42.dp)
            )
            separator()
        }

        DurationTextField(
            state = minutesState,
            containerColor = containerColor,
            label = "m",
            maxValue = 59,
            modifier = Modifier.width(42.dp)
        )

        if (!isTowardsLeft) {
            separator()
            DurationTextField(
                state = hoursState,
                containerColor = containerColor,
                label = "h",
                maxValue = 24,
                modifier = Modifier.width(42.dp)
            )
        }
    }
}

// ============================================================================
// START MODE TOGGLE
// ============================================================================

/**
 * Toggle between Active and Passive start modes
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ClepsydraScope.StartModeToggle(
    startActive: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ModeOption(
            selected = startActive,
            onClick = { onToggle(true) },
            icon = Icons.Default.PlayArrow,
            label = "Active",
            selectedColor = MaterialTheme.colorScheme.primaryContainer,
            onSelectedColor = MaterialTheme.colorScheme.onPrimaryContainer,
            expressiveShape = MaterialShapes.Arch.toShape(0),
            modifier = Modifier.weight(1f)
        )

        ModeOption(
            selected = !startActive,
            onClick = { onToggle(false) },
            icon = Icons.Default.Pause,
            label = "Passive",
            selectedColor = MaterialTheme.colorScheme.secondaryContainer,
            onSelectedColor = MaterialTheme.colorScheme.onSecondaryContainer,
            expressiveShape = MaterialShapes.Arch.toShape(45),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ModeOption(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    selectedColor: Color,
    onSelectedColor: Color,
    expressiveShape: Shape,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        if (selected) selectedColor else selectedColor.copy(alpha = 0.15f),
        animationSpec = tween(400)
    )

    val contentAlpha by animateFloatAsState(
        if (selected) 1f else 0.5f
    )

    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!selected) {
                    Modifier.border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                        RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        shape = if (selected) expressiveShape else RoundedCornerShape(16.dp),
        color = backgroundColor,
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        ModeOptionContent(
            icon = icon,
            label = label,
            selected = selected,
            selectedColor = selectedColor,
            onSelectedColor = onSelectedColor,
            contentAlpha = contentAlpha
        )
    }
}

@Composable
private fun ModeOptionContent(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onSelectedColor: Color,
    contentAlpha: Float
) {
    val textColor = if (selected) onSelectedColor else MaterialTheme.colorScheme.onSurfaceVariant
    val isPrimary = selectedColor == MaterialTheme.colorScheme.primaryContainer

    Column(
        modifier = Modifier.fillMaxSize().alpha(contentAlpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isPrimary) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                color = textColor
            )
        }

        Icon(
            icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )

        if (isPrimary) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

// ============================================================================
// TIME PICKER COMPONENT
// ============================================================================

/**
 * Time picker with three modes: NONE, DURATION, and TIMESTAMP
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ClepsydraScope.ClepsydraTimePicker(
    label: String,
    hours: Duration?,
    minutes: Duration?,
    seconds: Duration?,
    timeMark: TimeMark?,
    onTimeChanged: (Duration?, Duration?, Duration?, TimeMark?) -> Unit,
    isTowardsLeft: Boolean,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(TimeInputMode.NONE) }
    val hoursState = rememberTextFieldState(
        hours?.toInt(DurationUnit.HOURS).toString().replace("null", "00")
    )
    val minutesState = rememberTextFieldState(
        minutes?.toInt(DurationUnit.MINUTES).toString().replace("null", "00")
    )
    val secondsState = rememberTextFieldState(
        seconds?.toInt(DurationUnit.SECONDS).toString().replace("null", "00")
    )
    val timePickerState = rememberTimePickerState()
    var showTimePickerDialog by remember { mutableStateOf(false) }

    // Sync mode changes to parent
    TimePickerModeSync(
        mode = mode,
        hoursState = hoursState,
        minutesState = minutesState,
        secondsState = secondsState,
        timePickerState = timePickerState,
        onTimeChanged = onTimeChanged
    )

    Column(
        modifier = modifier,
        horizontalAlignment = if (isTowardsLeft) Alignment.End else Alignment.Start
    ) {
        TimePickerLabel(label, mode)

        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isTowardsLeft) Arrangement.End else Arrangement.Start
        ) {
            if (isTowardsLeft) {
                TimeDisplay(
                    mode = mode,
                    hoursState = hoursState,
                    minutesState = minutesState,
                    secondsState = secondsState,
                    timePickerState = timePickerState,
                    onShowTimePicker = { showTimePickerDialog = true },
                    isTowardsLeft = isTowardsLeft,
                    modifier = Modifier.heightIn(max = 56.dp).weight(1f, false)
                )
                ThreeModalButton(
                    modifier = Modifier.width(56.dp).aspectRatio(1f),
                    isTowardsLeft = isTowardsLeft,
                    onClick = { mode = mode.next() },
                    modes = mode
                )
            } else {
                ThreeModalButton(
                    modifier = Modifier.width(56.dp).aspectRatio(1f),
                    isTowardsLeft = isTowardsLeft,
                    onClick = { mode = mode.next() },
                    modes = mode
                )
                TimeDisplay(
                    mode = mode,
                    hoursState = hoursState,
                    minutesState = minutesState,
                    secondsState = secondsState,
                    timePickerState = timePickerState,
                    onShowTimePicker = { showTimePickerDialog = true },
                    isTowardsLeft = isTowardsLeft,
                    modifier = Modifier.heightIn(max = 56.dp).weight(1f, false)
                )
            }
        }
    }

    if (showTimePickerDialog) {
        TimePickerDialog(
            state = timePickerState,
            onDismiss = { showTimePickerDialog = false },
            onConfirm = { showTimePickerDialog = false }
        )
    }
}

@Composable
private fun TimePickerLabel(label: String, mode: TimeInputMode) {
    val suffix = when (mode) {
        TimeInputMode.NONE -> " not set"
        TimeInputMode.DURATION -> " in "
        TimeInputMode.TIMESTAMP -> " at "
    }
    Text(
        label + suffix,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.secondary
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerModeSync(
    mode: TimeInputMode,
    hoursState: TextFieldState,
    minutesState: TextFieldState,
    secondsState: TextFieldState,
    timePickerState: TimePickerState,
    onTimeChanged: (Duration?, Duration?, Duration?, TimeMark?) -> Unit
) {
    LaunchedEffect(mode) {
        when (mode) {
            TimeInputMode.NONE -> {
                onTimeChanged(null, null, null, null)
            }
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
                snapshotFlow { timePickerState.hour to timePickerState.minute }
                    .collect { (h, m) ->
                        onTimeChanged(
                            null,
                            null,
                            null,
                            (h * 60 + m).minutes.asTimeMarkFromStartOfDay()
                        )
                    }
            }
        }
    }
}

// ============================================================================
// TIME DISPLAY COMPONENT
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClepsydraScope.TimeDisplay(
    mode: TimeInputMode,
    hoursState: TextFieldState,
    minutesState: TextFieldState,
    secondsState: TextFieldState,
    timePickerState: TimePickerState,
    onShowTimePicker: () -> Unit,
    isTowardsLeft: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = mode,
        modifier = modifier,
        label = "TimeDisplay"
    ) { currentMode ->
        when (currentMode) {
            TimeInputMode.NONE -> Unit
            TimeInputMode.DURATION -> {
                DurationTimeDisplay(
                    hoursState = hoursState,
                    minutesState = minutesState,
                    secondsState = secondsState,
                    isTowardsLeft = isTowardsLeft
                )
            }
            TimeInputMode.TIMESTAMP -> {
                TimestampDisplay(
                    timePickerState = timePickerState,
                    onShowTimePicker = onShowTimePicker
                )
            }
        }
    }
}

@Composable
private fun ClepsydraScope.DurationTimeDisplay(
    hoursState: TextFieldState,
    minutesState: TextFieldState,
    secondsState: TextFieldState,
    isTowardsLeft: Boolean
) {
    var showHours by remember { mutableStateOf(false) }
    var showSeconds by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            horizontalArrangement = if (isTowardsLeft) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DurationFieldOrToggle(
                isVisible = showHours,
                onToggle = { showHours = !showHours },
                state = hoursState,
                label = "h",
                modifier = Modifier.weight(1f, false)
            )

            Box(
                modifier = Modifier.weight(1f, false).padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                DurationTextField(
                    label = "m",
                    state = minutesState,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    maxValue = 59,
                )
            }

            DurationFieldOrToggle(
                isVisible = showSeconds,
                onToggle = { showSeconds = !showSeconds },
                state = secondsState,
                label = "s",
                modifier = Modifier.weight(1f, false)
            )
        }
    }
}

@Composable
private fun ClepsydraScope.DurationFieldOrToggle(
    isVisible: Boolean,
    onToggle: () -> Unit,
    state: TextFieldState,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isVisible) {
            Text(" : ", fontWeight = FontWeight.Bold)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DurationTextField(
                    label = label,
                    state = state,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    maxValue = if (label == "h") 23 else 59,
                )
            }
        } else {
            Card {
                TextButton(
                    onClick = onToggle,
                    modifier = Modifier.size(20.dp)
                ) {
                    Box {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 2.sp,
                                stepSize = 0.001.sp
                            ),
                            maxLines = 1,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimestampDisplay(
    timePickerState: TimePickerState,
    onShowTimePicker: () -> Unit
) {
    Card {
        TextButton(
            onClick = onShowTimePicker,
            modifier = Modifier.wrapContentSize()
        ) {
            Text(
                "At : ${timePickerState.hour.toString().padStart(2, '0')}:${
                    timePickerState.minute.toString().padStart(2, '0')
                }",
                style = MaterialTheme.typography.bodyLarge,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 2.sp,
                    stepSize = 0.001.sp
                ),
                maxLines = 1,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================================================
// TIME PICKER DIALOG
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClepsydraScope.TimePickerDialog(
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
                .wrapContentWidth()
                .widthIn(max = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.adaptivePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Time", style = MaterialTheme.typography.labelLarge)
                TimePicker(state = state)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = onConfirm) { Text("OK") }
                }
            }
        }
    }
}

// ============================================================================
// THREE MODAL BUTTON
// ============================================================================

/**
 * Button that cycles through three modes with animated shape morphing
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ClepsydraScope.ThreeModalButton(
    modifier: Modifier,
    isTowardsLeft: Boolean,
    onClick: () -> Unit,
    modes: TimeInputMode
) {
    var circle by remember { mutableIntStateOf(if (isTowardsLeft) 180 else 0) }
    var isTransitioning by remember { mutableStateOf(false) }

    LaunchedEffect(modes) {
        circle += 120 * (if (isTowardsLeft) -1 else 1)
        isTransitioning = true
        delay(250)
        isTransitioning = false
    }

    val angle by animateIntAsState(circle, tween(500))
    val scale by animateFloatAsState(
        if (isTransitioning) 1.5f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
    )

    Surface(
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        onClick = onClick,
        color = when (modes) {
            TimeInputMode.NONE -> MaterialTheme.colorScheme.primaryContainer
            TimeInputMode.DURATION -> MaterialTheme.colorScheme.secondaryContainer
            TimeInputMode.TIMESTAMP -> MaterialTheme.colorScheme.tertiaryContainer
        },
        shape = MaterialShapes.Triangle.toShape(angle),
        shadowElevation = 6.dp,
        tonalElevation = 6.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(iPhi),
            contentAlignment = if (isTowardsLeft) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Row {
                Spacer(Modifier.width(7.dp + if (!isTowardsLeft) 4.dp else 0.dp))
                Icon(
                    when (modes) {
                        TimeInputMode.NONE -> Icons.Default.HourglassEmpty
                        TimeInputMode.DURATION -> Icons.Default.Timer
                        TimeInputMode.TIMESTAMP -> Icons.Default.AccessTime
                    },
                    contentDescription = null,
                    modifier = Modifier.size(23.dp)
                )
                Spacer(Modifier.width(7.dp + if (isTowardsLeft) 4.dp else 0.dp))
            }
        }
    }
}

// ============================================================================
// DURATION TEXT FIELD
// ============================================================================

/**
 * Interactive text field for duration input with scroll and drag gestures
 */
@Composable
fun ClepsydraScope.DurationTextField(
    state: TextFieldState,
    containerColor: Color,
    modifier: Modifier = Modifier,
    label: String = "",
    maxValue: Int = 99
) {
    val haptic = LocalHapticFeedback.current
    val currentInt = state.text.toString().toIntOrNull() ?: 0

    Box(
        modifier = modifier
            .aspectRatio(iPhi)
            .addScrollGesture { direction ->
                updateDurationValue(state, direction, maxValue, haptic)
            }
            .addDragGesture { direction ->
                updateDurationValue(state, direction, maxValue, haptic)
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GhostNumber(
                value = currentInt + 1,
                maxValue = maxValue,
                fadeFromTop = true,
                modifier = Modifier.weight(iPhi)
            )

            Box(modifier = Modifier.weight(phi), contentAlignment = Alignment.Center) {
                BasicTextField(
                    state = state,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    lineLimits = TextFieldLineLimits.SingleLine,
                    inputTransformation = InputTransformation.maxLength(2).then {
                        if (!asCharSequence().all { it.isDigit() }) revertAllChanges()
                    },
                    outputTransformation = OutputTransformation {
                        this.append(" $label")
                    },
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = contentColorFor(containerColor),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorator = { it() }
                )
            }

            GhostNumber(
                value = currentInt - 1,
                maxValue = maxValue,
                fadeFromTop = false,
                modifier = Modifier.weight(iPhi)
            )
        }
    }
}

@Composable
private fun GhostNumber(
    value: Int,
    maxValue: Int,
    fadeFromTop: Boolean,
    modifier: Modifier = Modifier
) {
    val displayValue = value.coerceIn(0, maxValue)
    val shouldShow = if (fadeFromTop) value <= maxValue else value >= 0

    Box(
        modifier = modifier,
        contentAlignment = if (fadeFromTop) Alignment.BottomCenter else Alignment.TopCenter
    ) {
        if (shouldShow) {
            Text(
                displayValue.toString().padStart(2, '0'),
                style = MaterialTheme.typography.labelSmall,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 2.sp,
                    stepSize = 0.01.sp
                ),
                modifier = Modifier.applyFadeGradient(fadeFromTop)
            )
        }
    }
}

// ============================================================================
// HELPER FUNCTIONS & EXTENSIONS
// ============================================================================

private fun Modifier.addScrollGesture(
    onScroll: (direction: Int) -> Unit
): Modifier = this.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            if (event.type == PointerEventType.Scroll) {
                val direction = if (event.changes.first().scrollDelta.y < 0) 1 else -1
                onScroll(direction)
            }
        }
    }
}

private fun Modifier.addDragGesture(
    onDrag: (direction: Int) -> Unit
): Modifier = this.pointerInput(Unit) {
    var drag = 0f
    detectVerticalDragGestures(
        onDragEnd = { drag = 0f }
    ) { change, amount ->
        change.consume()
        drag += amount
        if (abs(drag) > 30f) {
            val direction = if (drag < 0) 1 else -1
            onDrag(direction)
            drag = 0f
        }
    }
}

private fun Modifier.applyFadeGradient(fadeFromTop: Boolean): Modifier {
    return this
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val fadeOverlay = if (fadeFromTop) {
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    1f to Color.Black,
                    startY = 0f,
                    endY = size.height
                )
            } else {
                Brush.verticalGradient(
                    0f to Color.Black,
                    1f to Color.Transparent,
                    startY = 0f,
                    endY = size.height
                )
            }
            drawRect(brush = fadeOverlay, blendMode = BlendMode.DstIn)
        }
}

private fun updateDurationValue(
    state: TextFieldState,
    increment: Int,
    maxValue: Int,
    haptic: HapticFeedback?
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

private fun createClepsydraAction(formState: InputClepsydraFormState): ClepsydraScreenAction.OnCreateClepsydra {
    return ClepsydraScreenAction.OnCreateClepsydra(
        presetClepsydra = formState.presetClepsydra,
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
}