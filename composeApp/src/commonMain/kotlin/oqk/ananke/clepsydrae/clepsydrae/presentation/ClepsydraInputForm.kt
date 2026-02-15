package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
// RESPONSIVE SIZING CONFIGURATION
// ============================================================================

/**
 * Responsive sizing configuration based on window width
 */
data class ResponsiveSizing(
    val goalCardHeight: Dp,
    val centerButtonSize: Dp,
    val timePickerMaxHeight: Dp,
    val iconSize: Dp,
    val smallIconSize: Dp,
    val spacing: Dp,
    val textScale: Float
)

@Composable
private fun ClepsydraScope.rememberResponsiveSizing(): ResponsiveSizing {
    val width = sizes.width

    return remember(width) {
        val scale = when {
            width < 400.dp -> 0.75f  // Compact: 300-400dp
            width < 600.dp -> 0.9f   // Small: 400-600dp
            width < 800.dp -> 1.0f   // Medium: 600-800dp
            width < 1200.dp -> 1.15f // Large: 800-1200dp
            else -> 1.3f          // Extra Large: 1200dp+
        }

        ResponsiveSizing(
            goalCardHeight = (64.dp * scale).coerceAtLeast(48.dp),
            centerButtonSize = (56.dp * scale).coerceAtLeast(44.dp),
            timePickerMaxHeight = (56.dp * scale).coerceAtLeast(44.dp),
            iconSize = (24.dp * scale).coerceAtLeast(18.dp),
            smallIconSize = (20.dp * scale).coerceAtLeast(16.dp),
            spacing = (8.dp * scale).coerceAtLeast(4.dp),
            textScale = scale.coerceIn(0.85f, 1.2f)
        )
    }
}

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
    val sizing = rememberResponsiveSizing()

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
                sizing = sizing,
                modifier = Modifier.weight(phi)
            )

            // Center: Start mode toggle and create button
            CenterActionSection(
                formState = formState,
                onFormUpdate = formState_::update,
                sizing = sizing,
                modifier = Modifier.width(sizing.centerButtonSize)
            )

            // Right side: Passive goal and end time
            RightTimeSection(
                formState = formState,
                onFormUpdate = formState_::update,
                sizing = sizing,
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
    sizing: ResponsiveSizing,
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
                sizing = sizing,
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
                sizing = sizing,
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
    sizing: ResponsiveSizing,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = "Start as",
            style = MaterialTheme.typography.labelSmall,
            fontSize = (MaterialTheme.typography.labelSmall.fontSize.value * sizing.textScale).sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(sizing.spacing))

        StartModeToggle(
            startActive = formState.startActive,
            onToggle = { startActive ->
                onFormUpdate { copy(startActive = startActive) }
            },
            sizing = sizing,
            modifier = Modifier.width(sizing.centerButtonSize).height(sizing.goalCardHeight)
        )

        Spacer(modifier = Modifier.height(sizing.spacing))

        CreateButtonSection(
            formState = formState,
            sizing = sizing,
            modifier = Modifier.width(sizing.centerButtonSize).wrapContentHeight()
        )
    }
}

@Composable
private fun ClepsydraScope.RightTimeSection(
    formState: InputClepsydraFormState,
    onFormUpdate: (InputClepsydraFormState.() -> InputClepsydraFormState) -> Unit,
    sizing: ResponsiveSizing,
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
                sizing = sizing,
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
                sizing = sizing,
                isTowardsLeft = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ClepsydraScope.CreateButtonSection(
    formState: InputClepsydraFormState,
    sizing: ResponsiveSizing,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy((-4.dp * sizing.textScale))
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
            modifier = Modifier.size(sizing.centerButtonSize),
            shape = RoundedCornerShape((16.dp * sizing.textScale).coerceAtLeast(12.dp)),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Create",
                modifier = Modifier.size(sizing.iconSize * 1.33f)
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
    sizing: ResponsiveSizing,
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
        modifier = modifier.height(sizing.goalCardHeight),
        shape = RoundedCornerShape((20.dp * sizing.textScale).coerceAtLeast(16.dp)),
        color = containerColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, containerColor.copy(alpha = 0.25f)),
        tonalElevation = 0.dp
    ) {
        Column (
            modifier = Modifier.padding(horizontal = (12.dp * sizing.textScale).coerceAtLeast(8.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            GoalLabel(label, containerColor, TextAlign.Center)

            GoalTimeFields(
                hoursState = hoursState,
                minutesState = minutesState,
                containerColor = containerColor,
                sizing = sizing,
                modifier = Modifier.weight(1f, false)
            )

        }
    }
}

@Composable
private fun GoalLabel(
    label: String,
    containerColor: Color,
    textAlign: TextAlign,
) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(textAlign = textAlign),
        autoSize = TextAutoSize.StepBased(minFontSize = 7.sp, stepSize = 0.001.sp),
        fontWeight = FontWeight.Black,
        maxLines = 1,
        color = containerColor,
        modifier = Modifier.fillMaxWidth(iPhi*iPhi)
    )
}

@Composable
private fun ClepsydraScope.GoalTimeFields(
    hoursState: TextFieldState,
    minutesState: TextFieldState,
    containerColor: Color,
    sizing: ResponsiveSizing,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        val separator = @Composable {
            Text(
                text = ":",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = (MaterialTheme.typography.bodyLarge.fontSize.value * sizing.textScale).sp,
                fontWeight = FontWeight.Bold,
                color = containerColor.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = (4.dp * sizing.textScale).coerceAtLeast(2.dp))
            )
        }

        val fieldWidth = (42.dp * sizing.textScale).coerceAtLeast(32.dp)


        DurationTextField(
            state = hoursState,
            containerColor = containerColor,
            label = "h",
            maxValue = 24,
            sizing = sizing,
            modifier = Modifier.width(fieldWidth)
        )
        separator()


        DurationTextField(
            state = minutesState,
            containerColor = containerColor,
            label = "m",
            maxValue = 59,
            sizing = sizing,
            modifier = Modifier.width(fieldWidth)
        )
    }
}

// ============================================================================
// START MODE TOGGLE
// ============================================================================

/**
 * Compact toggle between Active and Passive start modes - matches goal card height
 */
@Composable
private fun ClepsydraScope.StartModeToggle(
    startActive: Boolean,
    onToggle: (Boolean) -> Unit,
    sizing: ResponsiveSizing,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primaryContainer
    val passiveColor = MaterialTheme.colorScheme.secondaryContainer

    val backgroundColor by animateColorAsState(
        targetValue = if (startActive) activeColor else passiveColor,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "backgroundColor"
    )

    val indicatorOffset by animateFloatAsState(
        targetValue = if (startActive) -1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicatorOffset"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape((20.dp * sizing.textScale).coerceAtLeast(16.dp)),
        color = backgroundColor.copy(alpha = 0.12f),
        border = BorderStroke(
            width = 1.dp,
            color = backgroundColor.copy(alpha = 0.25f)
        ),
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Sliding indicator background
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .fillMaxHeight(0.46f)
                    .align(Alignment.Center)
                    .graphicsLayer {
                        val offsetAmount = size.height / 2
                        translationY = offsetAmount*indicatorOffset
                    }
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape((16.dp * sizing.textScale).coerceAtLeast(12.dp))
                    )
            )

            // Toggle options
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Active option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggle(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = sizing.spacing / 2)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Active",
                            tint = if (startActive)
                                contentColorFor(activeColor)
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(sizing.smallIconSize)
                        )
                    }
                }

                // Passive option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggle(false) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = sizing.spacing / 2)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Passive",
                            tint = if (!startActive)
                                contentColorFor(passiveColor)
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(sizing.smallIconSize)
                        )
                    }
                }
            }
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
    sizing: ResponsiveSizing,
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
        TimePickerLabel(label, mode, sizing)

        Spacer(modifier = Modifier.height(sizing.spacing / 2))

        Row(
            modifier = Modifier.height(sizing.timePickerMaxHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isTowardsLeft) Arrangement.End else Arrangement.Start
        ) {
            val buttonSize = sizing.centerButtonSize * 0.85f

            if (isTowardsLeft) {
                TimeDisplay(
                    mode = mode,
                    hoursState = hoursState,
                    minutesState = minutesState,
                    secondsState = secondsState,
                    timePickerState = timePickerState,
                    onShowTimePicker = { showTimePickerDialog = true },
                    isTowardsLeft = true,
                    sizing = sizing,
                    modifier = Modifier.weight(1f, false).fillMaxHeight()
                )
                if (mode != TimeInputMode.NONE) {
                    Spacer(modifier = Modifier.width(sizing.spacing))
                }
                ThreeModalButton(
                    modifier = Modifier.size(buttonSize),
                    isTowardsLeft = true,
                    onClick = { mode = mode.next() },
                    modes = mode,
                    sizing = sizing
                )
            } else {
                ThreeModalButton(
                    modifier = Modifier.size(buttonSize),
                    isTowardsLeft = false,
                    onClick = { mode = mode.next() },
                    modes = mode,
                    sizing = sizing
                )
                if (mode != TimeInputMode.NONE) {
                    Spacer(modifier = Modifier.width(sizing.spacing))
                }
                TimeDisplay(
                    mode = mode,
                    hoursState = hoursState,
                    minutesState = minutesState,
                    secondsState = secondsState,
                    timePickerState = timePickerState,
                    onShowTimePicker = { showTimePickerDialog = true },
                    isTowardsLeft = false,
                    sizing = sizing,
                    modifier = Modifier.weight(1f, false).fillMaxHeight()
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
private fun TimePickerLabel(label: String, mode: TimeInputMode, sizing: ResponsiveSizing) {
    val suffix = when (mode) {
        TimeInputMode.NONE -> " not set"
        TimeInputMode.DURATION -> " in"
        TimeInputMode.TIMESTAMP -> " at"
    }
    Text(
        text = label + suffix,
        style = MaterialTheme.typography.labelSmall,
        fontSize = (MaterialTheme.typography.labelSmall.fontSize.value * sizing.textScale).sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium
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
    sizing: ResponsiveSizing,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AnimatedContent(
            targetState = mode,
            label = "TimeDisplay"
        ) { currentMode ->
            when (currentMode) {
                TimeInputMode.NONE -> Box(modifier = Modifier.fillMaxSize())
                TimeInputMode.DURATION -> {
                    DurationTimeDisplay(
                        hoursState = hoursState,
                        minutesState = minutesState,
                        secondsState = secondsState,
                        isTowardsLeft = isTowardsLeft,
                        sizing = sizing
                    )
                }
                TimeInputMode.TIMESTAMP -> {
                    TimestampDisplay(
                        timePickerState = timePickerState,
                        onShowTimePicker = onShowTimePicker,
                        sizing = sizing
                    )
                }
            }
        }
    }
}

@Composable
private fun ClepsydraScope.DurationTimeDisplay(
    hoursState: TextFieldState,
    minutesState: TextFieldState,
    secondsState: TextFieldState,
    isTowardsLeft: Boolean,
    sizing: ResponsiveSizing
) {
    var showHours by remember { mutableStateOf(false) }
    var showSeconds by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
        shape = RoundedCornerShape((16.dp * sizing.textScale).coerceAtLeast(12.dp)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
        tonalElevation = 0.dp
    ) {
        Row(
            horizontalArrangement = if (isTowardsLeft) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = (4.dp * sizing.textScale).coerceAtLeast(2.dp))
        ) {
            DurationFieldOrToggle(
                isVisible = showHours,
                onToggle = { showHours = !showHours },
                state = hoursState,
                label = "h",
                sizing = sizing,
                modifier = Modifier.weight(1f, false)
            )

            Box(
                modifier = Modifier.weight(1f, false).padding(horizontal = (4.dp * sizing.textScale).coerceAtLeast(2.dp)),
                contentAlignment = Alignment.Center
            ) {
                DurationTextField(
                    label = "m",
                    state = minutesState,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    maxValue = 59,
                    sizing = sizing
                )
            }

            DurationFieldOrToggle(
                isVisible = showSeconds,
                onToggle = { showSeconds = !showSeconds },
                state = secondsState,
                label = "s",
                sizing = sizing,
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
    sizing: ResponsiveSizing,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isVisible) {
            if (label != "h") {
                Text(
                    text = " : ",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = (MaterialTheme.typography.bodyLarge.fontSize.value * sizing.textScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DurationTextField(
                    label = label,
                    state = state,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    maxValue = if (label == "h") 23 else 59,
                    sizing = sizing
                )
            }
            if (label == "h") {
                Text(
                    text = " : ",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = (MaterialTheme.typography.bodyLarge.fontSize.value * sizing.textScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

        } else {
            val toggleSize = (20.dp * sizing.textScale).coerceAtLeast(16.dp)
            Surface(
                onClick = onToggle,
                modifier = Modifier.size(toggleSize),
                shape = RoundedCornerShape((6.dp * sizing.textScale).coerceAtLeast(4.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = (10 * sizing.textScale).coerceAtLeast(8f).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimestampDisplay(
    timePickerState: TimePickerState,
    onShowTimePicker: () -> Unit,
    sizing: ResponsiveSizing
) {
    Surface(
        onClick = onShowTimePicker,
        modifier = Modifier.wrapContentSize(),
        shape = RoundedCornerShape((16.dp * sizing.textScale).coerceAtLeast(12.dp)),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = (16.dp * sizing.textScale).coerceAtLeast(12.dp),
                vertical = (12.dp * sizing.textScale).coerceAtLeast(8.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "At : ${timePickerState.hour.toString().padStart(2, '0')}:${
                    timePickerState.minute.toString().padStart(2, '0')
                }",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = (MaterialTheme.typography.bodyLarge.fontSize.value * sizing.textScale).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
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
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier.adaptivePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Time",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TimePicker(state = state)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(onClick = onConfirm) {
                        Text(
                            text = "OK",
                            fontWeight = FontWeight.Bold
                        )
                    }
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
    modes: TimeInputMode,
    sizing: ResponsiveSizing
) {
    var circle by remember { mutableIntStateOf(if (isTowardsLeft) 180 else 0) }
    var isTransitioning by remember { mutableStateOf(false) }

    LaunchedEffect(modes) {
        circle += 120 * (if (isTowardsLeft) -1 else 1)
        isTransitioning = true
        delay(250)
        isTransitioning = false
    }

    val angle by animateIntAsState(
        targetValue = circle,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "angle"
    )

    val scale by animateFloatAsState(
        targetValue = if (isTransitioning) 1.5f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val containerColor = when (modes) {
        TimeInputMode.NONE -> MaterialTheme.colorScheme.primaryContainer
        TimeInputMode.DURATION -> MaterialTheme.colorScheme.secondaryContainer
        TimeInputMode.TIMESTAMP -> MaterialTheme.colorScheme.tertiaryContainer
    }

    val icon = when (modes) {
        TimeInputMode.NONE -> Icons.Default.HourglassEmpty
        TimeInputMode.DURATION -> Icons.Default.Timer
        TimeInputMode.TIMESTAMP -> Icons.Default.AccessTime
    }

    Surface(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        onClick = onClick,
        color = containerColor,
        shape = MaterialShapes.Triangle.toShape(angle),
        shadowElevation = 6.dp,
        tonalElevation = 6.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(iPhi),
            contentAlignment = if (isTowardsLeft) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconPadding = (7.dp * sizing.textScale).coerceAtLeast(5.dp)
                Spacer(Modifier.width(iconPadding + if (!isTowardsLeft) (4.dp * sizing.textScale).coerceAtLeast(2.dp) else 0.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(sizing.smallIconSize),
                    tint = contentColorFor(containerColor)
                )
                Spacer(Modifier.width(iconPadding + if (isTowardsLeft) (4.dp * sizing.textScale).coerceAtLeast(2.dp) else 0.dp))
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
    maxValue: Int = 99,
    sizing: ResponsiveSizing = rememberResponsiveSizing()
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
                sizing = sizing,
                modifier = Modifier.weight(iPhi)
            )


            Box(modifier = Modifier, contentAlignment = Alignment.Center) {
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
                        fontSize = (14 * sizing.textScale).coerceAtLeast(11f).sp,
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
                sizing = sizing,
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
    sizing: ResponsiveSizing,
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
                fontSize = (10 * sizing.textScale).coerceAtLeast(8f).sp,
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