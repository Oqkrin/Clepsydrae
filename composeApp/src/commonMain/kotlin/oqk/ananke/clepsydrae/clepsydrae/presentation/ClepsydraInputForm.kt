package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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
import oqk.ananke.clepsydrae.core.iPhi
import oqk.ananke.clepsydrae.journal.presentation.TimePickerDialog
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.TimeMark
import kotlin.time.TimeSource

// ============================================================================
// COMPACT TOKENS (structural, not “just a scale knob”)
// ============================================================================

private val ClepsydraScope.TAP: Dp     // Material minimum touch target
    get() = 48.adp()
private val FAB: Dp = 56.dp
private val GAP: Dp = 4.dp
private val PAD_OUTER: Dp = 6.dp
private val PAD_INNER_H: Dp = 8.dp
private val PAD_INNER_V: Dp = 6.dp
private val ICON: Dp = 20.dp
private val LABEL_SP = 10.sp

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
// MAIN FORM — stacking triggers via isNarrow (from ClepsydraScope)
// ============================================================================

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ClepsydraScope.ClepsydraInputForm(modifier: Modifier = Modifier) {
    val formState_ = retain { MutableStateFlow(InputClepsydraFormState()) }
    val formState by formState_.asStateFlow().collectAsState()

    LaunchedEffect(Unit) {
        snapshotFlow { formState }.distinctUntilChanged().collect { }
    }

    // Card width clamps that do NOT force min=280 when the window is smaller.
    val maxCard = (sizes.width - 8.dp).coerceAtLeast(0.dp)
    val minCard = minOf(280.dp, maxCard)

    Box(
        modifier = modifier.adaptivePadding(minPadding = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .widthIn(min = minCard, max = maxCard)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp)
        ) {
            val contentMax = (maxCard - PAD_OUTER * 2).coerceAtLeast(0.dp)

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(PAD_OUTER)
                ) {
                    val usable = (contentMax).coerceAtLeast(280.dp)
                    val sideMax = (usable * 0.44f).coerceAtLeast(150.dp)
                    val centerMax = (usable * 0.18f).coerceAtLeast(TAP)

                    LeftTimeSection(formState, formState_::update, maxWidth = sideMax)
                    CenterActionSection(formState, formState_::update, maxWidth = centerMax)
                    RightTimeSection(formState, formState_::update, maxWidth = sideMax)
                }
            }
        }
    }
// ============================================================================
// SECTIONS
// ============================================================================

@Composable
private fun ClepsydraScope.LeftTimeSection(
    formState: InputClepsydraFormState,
    onFormUpdate: (InputClepsydraFormState.() -> InputClepsydraFormState) -> Unit,
    maxWidth: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.widthIn(max = maxWidth),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(GAP)
    ) {
        GoalCard(
            label = "Pomodoro Active",
            duration = formState.activeGoal,
            onDurationChange = { d -> onFormUpdate { copy(activeGoal = d) } },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.widthIn(max = maxWidth)
        )

        ClepsydraTimePicker(
            label = "Start",
            hours = formState.initHours,
            minutes = formState.initMinutes,
            seconds = formState.initSeconds,
            timeMark = formState.initTimeMark,
            onTimeChanged = { h, m, s, tm ->
                onFormUpdate { copy(initHours = h, initMinutes = m, initSeconds = s, initTimeMark = tm) }
            },
            isTowardsLeft = true,
            maxWidth = maxWidth
        )
    }
}

@Composable
private fun ClepsydraScope.RightTimeSection(
    formState: InputClepsydraFormState,
    onFormUpdate: (InputClepsydraFormState.() -> InputClepsydraFormState) -> Unit,
    maxWidth: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.widthIn(max = maxWidth),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(GAP)
    ) {
        GoalCard(
            label = "Pomodoro Passive",
            duration = formState.passiveGoal,
            onDurationChange = { d -> onFormUpdate { copy(passiveGoal = d) } },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.widthIn(max = maxWidth)
        )

        ClepsydraTimePicker(
            label = "End",
            hours = formState.finHours,
            minutes = formState.finMinutes,
            seconds = formState.finSeconds,
            timeMark = formState.finTimeMark,
            onTimeChanged = { h, m, s, tm ->
                onFormUpdate { copy(finHours = h, finMinutes = m, finSeconds = s, finTimeMark = tm) }
            },
            isTowardsLeft = false,
            maxWidth = maxWidth
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ClepsydraScope.CenterActionSection(
    formState: InputClepsydraFormState,
    onFormUpdate: (InputClepsydraFormState.() -> InputClepsydraFormState) -> Unit,
    maxWidth: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.widthIn(max = maxWidth),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = "Start as",
            style = MaterialTheme.typography.labelSmall,
            fontSize = LABEL_SP,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        StartModeToggle(
            startActive = formState.startActive,
            onToggle = { a -> onFormUpdate { copy(startActive = a) } },
            modifier = Modifier
                .width(48.dp)
                .aspectRatio(iPhi)
        )
        CreateButtonSection(formState = formState)
    }
}

// ============================================================================
// GOAL CARD — label header (same as your current)
// ============================================================================

@Composable
fun ClepsydraScope.GoalCard(
    label: String,
    duration: Duration,
    onDurationChange: (Duration) -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    val totalMinutes = duration.inWholeMinutes.toInt()
    val hoursState = rememberTextFieldState((totalMinutes / 60).toString())
    val minutesState = rememberTextFieldState((totalMinutes % 60).toString())

    LaunchedEffect(hoursState.text, minutesState.text) {
        val h = hoursState.text.toString().toIntOrNull() ?: 0
        val m = minutesState.text.toString().toIntOrNull() ?: 0
        onDurationChange(h.hours + m.minutes)
    }

    Surface(
        modifier = modifier.heightIn(min = TAP),
        shape = RoundedCornerShape(20.dp),
        color = containerColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, containerColor.copy(alpha = 0.25f)),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = PAD_INNER_H, vertical = PAD_INNER_V),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedCard(colors = CardDefaults.elevatedCardColors(CardDefaults.elevatedCardColors().containerColor.copy(alpha = 0.7f))) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 7.sp,
                fontWeight = FontWeight.Black,
                color = containerColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(2.dp)
            )
                }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                DurationTextField(hoursState, containerColor, label = "h", maxValue = 24)
                Text(":", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = containerColor.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 2.dp))
                DurationTextField(minutesState, containerColor, label = "m", maxValue = 59)
            }
        }
    }
}

// ============================================================================
// CREATE BUTTONS
// ============================================================================

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ClepsydraScope.CreateButtonSection(
    formState: InputClepsydraFormState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.wrapContentWidth(Alignment.CenterHorizontally),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(-GAP-3.dp)
    ) {
        Surface(
            onClick = { },
            modifier = Modifier.size(48.dp),
            shape = MaterialShapes.SemiCircle.toShape(),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 6.dp,
            shadowElevation = 6.dp
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.MoreHoriz, contentDescription = "Presets", modifier = Modifier.size(ICON),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        val isFirstClepsydra = LocalSettings.current.isFirstClepsydra
        FloatingActionButton(
            onClick = {
                if (isFirstClepsydra) onAction(ClepsydraScreenAction.OnFirstClepsydraCreation)
                onAction(createClepsydraAction(formState))
            },
            modifier = Modifier.size(FAB),
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create", modifier = Modifier.size(24.dp))
        }
    }
}

// ============================================================================
// START MODE TOGGLE — slim + animated indicator (restored vibe)
// ============================================================================

@Composable
private fun ClepsydraScope.StartModeToggle(
    startActive: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primaryContainer
    val passiveColor = MaterialTheme.colorScheme.secondaryContainer

    val backgroundColor by animateColorAsState(
        targetValue = if (startActive) activeColor else passiveColor,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "toggleBg"
    )

    // “Cool” motion: bouncy indicator slide between halves
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, backgroundColor.copy(alpha = 0.25f)),
        tonalElevation = 0.dp
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val half = maxHeight / 2

            val indicatorY by animateDpAsState(
                targetValue = if (startActive) 0.dp else half,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "indicatorY"
            )

            // moving indicator
            Box(
                modifier = Modifier
                    .offset(y = indicatorY)
                    .height(half)
                    .fillMaxSize()
                    .background(backgroundColor, RoundedCornerShape(16.dp))
            )

            Column(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .height(half)
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggle(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Active",
                        tint = if (startActive) contentColorFor(activeColor)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        modifier = Modifier.size(ICON)
                    )
                }
                Box(
                    modifier = Modifier
                        .height(half)
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggle(false) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Passive",
                        tint = if (!startActive) contentColorFor(passiveColor)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        modifier = Modifier.size(ICON)
                    )
                }
            }
        }
    }
}

// ============================================================================
// TIME PICKER + DISPLAY (unchanged from your current version except: make 2-row more eager when isNarrow)
// ============================================================================

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
    maxWidth: Dp,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(TimeInputMode.NONE) }

    val hoursState = rememberTextFieldState(hours?.toInt(DurationUnit.HOURS)?.toString() ?: "00")
    val minutesState = rememberTextFieldState(minutes?.toInt(DurationUnit.MINUTES)?.toString() ?: "00")
    val secondsState = rememberTextFieldState(seconds?.toInt(DurationUnit.SECONDS)?.toString() ?: "00")

    val timePickerState = rememberTimePickerState()
    var showTimePickerDialog by remember { mutableStateOf(false) }

    TimePickerModeSync(mode, hoursState, minutesState, secondsState, timePickerState, onTimeChanged)

    Column(
        modifier = modifier.widthIn(max = maxWidth).padding(bottom = GAP),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label + when (mode) {
                TimeInputMode.NONE -> " not set"
                TimeInputMode.DURATION -> " in"
                TimeInputMode.TIMESTAMP -> " at"
            },
            style = MaterialTheme.typography.labelSmall,
            fontSize = LABEL_SP,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(2.dp))

        val rowWidth = maxWidth.coerceAtLeast(TAP * 2 + GAP)
        val displayW = (rowWidth - TAP - GAP).coerceAtLeast(TAP)

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
                    isTowardsLeft = true,
                    modifier = Modifier
                )
                ThreeModalButton(
                    modifier = Modifier.size(48.dp),
                    isTowardsLeft = true,
                    onClick = { mode = mode.next() },
                    modes = mode
                )
            } else {
                ThreeModalButton(
                    modifier = Modifier.size(48.dp),
                    isTowardsLeft = false,
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
                    isTowardsLeft = false,
                    modifier = Modifier
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
            TimeInputMode.NONE -> onTimeChanged(null, null, null, null)
            TimeInputMode.DURATION -> {
                snapshotFlow { Triple(hoursState.text.toString(), minutesState.text.toString(), secondsState.text.toString()) }
                    .collect { (h, m, s) ->
                        onTimeChanged(h.toIntOrNull()?.hours, m.toIntOrNull()?.minutes, s.toIntOrNull()?.seconds, null)
                    }
            }
            TimeInputMode.TIMESTAMP -> {
                snapshotFlow { timePickerState.hour to timePickerState.minute }
                    .collect { (h, m) ->
                        onTimeChanged(null, null, null, (h * 60 + m).minutes.asTimeMarkFromStartOfDay())
                    }
            }
        }
    }
}

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
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AnimatedContent(targetState = mode, label = "TimeDisplay") { currentMode ->
            when (currentMode) {
                TimeInputMode.NONE -> Box {}
                TimeInputMode.DURATION -> {
                    DurationTimeDisplayResponsive(
                        hoursState = hoursState,
                        minutesState = minutesState,
                        secondsState = secondsState,
                        isTowardsLeft = isTowardsLeft
                    )
                }
                TimeInputMode.TIMESTAMP -> {
                }
            }
        }
    }
}

@Composable
private fun ClepsydraScope.DurationTimeDisplayResponsive(
    hoursState: TextFieldState,
    minutesState: TextFieldState,
    secondsState: TextFieldState,
    isTowardsLeft: Boolean
) {
    var showHours by remember { mutableStateOf(false) }
    var showSeconds by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
        tonalElevation = 0.dp,
        modifier = Modifier
    ) {
        BoxWithConstraints(Modifier) {
            val tooNarrowForSingleRow = isNarrow || maxWidth < (TAP * 3 + 20.dp)

            if (tooNarrowForSingleRow) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        DurationTextField(
                            state = minutesState,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            label = "m",
                            maxValue = 59,
                            modifier = Modifier.size(TAP)
                        )
                    }

                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DurationTextField(
                            state = hoursState,
                            label = "h",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            maxValue = 23,
                            modifier = Modifier.size(TAP)
                        )

                        DurationTextField(
                            state = secondsState,
                            label = "s",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            maxValue = 59,
                            modifier = Modifier.size(TAP)
                        )
                    }
                }
            } else {
                Row(
                    horizontalArrangement = if (isTowardsLeft) Arrangement.End else Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    DurationTextField(
                        state = hoursState,
                        label = "h",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        maxValue = 23,
                        modifier = Modifier.size(TAP)
                    )

                    Text(":", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 2.dp))

                    DurationTextField(
                        state = minutesState,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        label = "m",
                        maxValue = 59,
                        modifier = Modifier.size(TAP)
                    )

                    Text(":", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 2.dp))

                    DurationTextField(
                        state = secondsState,
                        label = "s",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        maxValue = 59,
                        modifier = Modifier.size(TAP)
                    )
                }
            }
        }
    }
}

// ============================================================================
// THREE MODAL BUTTON
// ============================================================================

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
        delay(200)
        isTransitioning = false
    }

    val angle by animateIntAsState(
        targetValue = circle,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "angle"
    )

    val scale by animateFloatAsState(
        targetValue = if (isTransitioning) 1.25f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
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
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .sizeIn(minWidth = TAP, minHeight = TAP),
        onClick = onClick,
        color = containerColor,
        shape = MaterialShapes.Triangle.toShape(angle),
        shadowElevation = 6.dp,
        tonalElevation = 6.dp
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(ICON), tint = contentColorFor(containerColor))
        }
    }
}

// ============================================================================
// DURATION TEXT FIELD
// ============================================================================

@Composable
fun ClepsydraScope.DurationTextField(
    state: TextFieldState,
    containerColor: Color,
    modifier: Modifier = Modifier,
    label: String = "",
    maxValue: Int = 99
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val focusRequester = remember { FocusRequester() }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor by animateColorAsState(
        targetValue = if (isPressed) containerColor.copy(alpha = 0.3f) else containerColor.copy(alpha = 0.1f),
        animationSpec = tween(120),
        label = "bgColor"
    )

    Box(
        modifier = modifier
            .size(TAP)
            .background(bgColor, RoundedCornerShape(8.dp))
            .focusRequester(focusRequester)
            .focusable()
            .clickable(interactionSource = interactionSource, indication = null) { focusRequester.requestFocus() }
            .drawWithContent {
                drawContent()
                with(density) {
                    drawLine(
                        color = containerColor,
                        start = Offset(size.width * 0.20f, size.height),
                        end = Offset(size.width * 0.80f, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
            .addScrollGesture { dir -> updateDurationValue(state, dir, maxValue, haptic) }
            .addDragGesture { dir -> updateDurationValue(state, dir, maxValue, haptic) },
        contentAlignment = Alignment.Center
    ) {
        val currentInt = state.text.toString().toIntOrNull() ?: 0

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GhostNumber(currentInt + 1, maxValue, fadeFromTop = true, modifier = Modifier.weight(iPhi))

            BasicTextField(
                state = state,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                lineLimits = TextFieldLineLimits.SingleLine,
                inputTransformation = InputTransformation.maxLength(2).then {
                    val txt = asCharSequence()
                    if (!txt.all { it.isDigit() }) revertAllChanges()
                    val num = txt.toString().toIntOrNull()
                    if (num != null && num > maxValue) replace(0, txt.length, maxValue.toString().padStart(2, '0'))
                },
                outputTransformation = OutputTransformation { },
                textStyle = TextStyle(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp*fluidScale,
                    color = contentColorFor(containerColor)
                ),
                modifier = Modifier.weight(1f).fillMaxSize(),
                decorator = { inner -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { inner() } }
            )

            GhostNumber(currentInt - 1, maxValue, fadeFromTop = false, modifier = Modifier.weight(iPhi))
        }

        Text(
            text = label,
            fontSize = 9.sp*fluidScale,
            fontWeight = FontWeight.Black,
            color = containerColor.copy(alpha = 0.95f),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 6.dp)
        )
    }
}

@Composable
private fun ClepsydraScope.GhostNumber(
    value: Int,
    maxValue: Int,
    fadeFromTop: Boolean,
    modifier: Modifier = Modifier
) {
    val displayValue = value.coerceIn(0, maxValue)
    val shouldShow = if (fadeFromTop) value <= maxValue else value >= 0


    Box(modifier = modifier, contentAlignment = if(fadeFromTop) Alignment.BottomCenter else Alignment.TopCenter) {
        if (!shouldShow) return
        Text(
            text = displayValue.toString().padStart(2, '0'),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 8.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            modifier = Modifier.applyFadeGradient(fadeFromTop)
        )
    }
}

// ============================================================================
// HELPERS
// ============================================================================

private fun Modifier.addScrollGesture(onScroll: (direction: Int) -> Unit): Modifier =
    this.pointerInput(Unit) {
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

private fun Modifier.addDragGesture(onDrag: (direction: Int) -> Unit): Modifier =
    this.pointerInput(Unit) {
        var drag = 0f
        detectVerticalDragGestures(onDragEnd = { drag = 0f }) { change, amount ->
            change.consume()
            drag += amount
            if (abs(drag) > 30f) {
                val direction = if (drag < 0) 1 else -1
                onDrag(direction)
                drag = 0f
            }
        }
    }

private fun Modifier.applyFadeGradient(fadeFromTop: Boolean): Modifier =
    this
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
        state.edit { replace(0, length, newVal.toString().padStart(2, '0')) }
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