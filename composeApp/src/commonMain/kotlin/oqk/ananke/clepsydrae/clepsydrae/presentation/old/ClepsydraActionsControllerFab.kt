package oqk.ananke.clepsydrae.clepsydrae.presentation.old

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra
import oqk.ananke.clepsydrae.clepsydrae.domain.asText
import oqk.ananke.clepsydrae.calendar.presentation.ClepsydraScope
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScreenAction
import oqk.ananke.clepsydrae.core.LocalSettings
import oqk.ananke.clepsydrae.core.phi
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClepsydraScope.ClepsydraActionsControllerFab(modifier: Modifier = Modifier) {
    val isFirst = LocalSettings.current.isFirstClepsydra
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
        contentAlignment = if (isShort) Alignment.BottomEnd else Alignment.BottomCenter
    ) {
        // First time hint
        AnimatedVisibility(
            visible = isFirst && !isExpanded,
            modifier = Modifier.align(Alignment.TopCenter).padding(bottom = 72.dp)
        ) {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(8.adp()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Swipe sections to configure",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // The floating strip
        ClepsydraCreationStrip(
            isExpanded = isExpanded,
            onExpandedChange = { isExpanded = it },
            onAction = onAction,
            isFirst = isFirst
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClepsydraScope.ClepsydraCreationStrip(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAction: (ClepsydraScreenAction) -> Unit,
    isFirst: Boolean
) {
    val name = rememberTextFieldState()
    val note = rememberTextFieldState()

    var startMode by remember { mutableIntStateOf(0) } // 0=immediate, 1=duration, 2=timestamp
    var initHours by remember { mutableStateOf<Duration?>(null) }
    var initMinutes by remember { mutableStateOf<Duration?>(null) }
    var initSeconds by remember { mutableStateOf<Duration?>(null) }
    var init by remember { mutableStateOf<TimeMark?>(null) }

    var endMode by remember { mutableIntStateOf(0) } // 0=no end, 1=duration, 2=timestamp
    var finHours by remember { mutableStateOf<Duration?>(null) }
    var finMinutes by remember { mutableStateOf<Duration?>(null) }
    var finSeconds by remember { mutableStateOf<Duration?>(null) }
    var fin by remember { mutableStateOf<TimeMark?>(null) }

    var activeGoal by remember { mutableStateOf(Duration.ZERO) }
    var passiveGoal by remember { mutableStateOf(Duration.ZERO) }
    var startActive by remember { mutableStateOf(false) }
    var presetClepsydra by remember { mutableStateOf<Clepsydra?>(null) }

    val animatedHeight by animateDpAsState(
        targetValue = if (isExpanded) 56.dp*phi else 56.dp,
        label = "strip height"
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(if (isShort) 0.95f else 0.9f)
            .height(animatedHeight)
            .padding(bottom = 16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "strip content"
        ) { expanded ->
            if (!expanded) {
                // Collapsed state - just the button
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "New Clepsydra",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                // Clickable overlay
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onExpandedChange(true) },
                    color = Color.Transparent
                ) {}
            } else {
                // Expanded state - scrollable sections
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Name/Note Section
                    NameNoteSection(
                        name = name,
                        note = note,
                        modifier = Modifier.width(140.dp)
                    )

                    // Start Mode Section
                    ScrollableModeSection(
                        title = "Start",
                        mode = startMode,
                        modeLabels = listOf("Now", "After", "At"),
                        onModeChange = { startMode = it },
                        hours = initHours,
                        minutes = initMinutes,
                        seconds = initSeconds,
                        onDurationChange = { h, m, s ->
                            initHours = h
                            initMinutes = m
                            initSeconds = s
                        },
                        modifier = Modifier.width(110.dp)
                    )

                    // End Mode Section
                    ScrollableModeSection(
                        title = "End",
                        mode = endMode,
                        modeLabels = listOf("None", "After", "At"),
                        onModeChange = { endMode = it },
                        hours = finHours,
                        minutes = finMinutes,
                        seconds = finSeconds,
                        onDurationChange = { h, m, s ->
                            finHours = h
                            finMinutes = m
                            finSeconds = s
                        },
                        modifier = Modifier.width(110.dp)
                    )

                    // Goals Section
                    GoalsCompactSection(
                        activeGoal = activeGoal,
                        passiveGoal = passiveGoal,
                        startActive = startActive,
                        onActiveGoalChange = { activeGoal = it },
                        onPassiveGoalChange = { passiveGoal = it },
                        onStartActiveChange = { startActive = it },
                        modifier = Modifier.width(100.dp)
                    )

                    // Create Button
                    FilledIconButton(
                        onClick = {
                            if (isFirst) onAction(ClepsydraScreenAction.OnFirstClepsydraCreation)
                            onAction(
                                ClepsydraScreenAction.OnCreateClepsydra(
                                    presetClepsydra = presetClepsydra,
                                    name = name.text.trim().toString(),
                                    note = note.text.toString(),
                                    initHours = if (startMode == 1) initHours else null,
                                    initMinutes = if (startMode == 1) initMinutes else null,
                                    initSeconds = if (startMode == 1) initSeconds else null,
                                    init = if (startMode == 2) init else null,
                                    finHours = if (endMode == 1) finHours else null,
                                    finMinutes = if (endMode == 1) finMinutes else null,
                                    finSeconds = if (endMode == 1) finSeconds else null,
                                    fin = if (endMode == 2) fin else null,
                                    passiveGoal = passiveGoal,
                                    activeGoal = activeGoal,
                                    startActive = startActive
                                )
                            )
                            onExpandedChange(false)
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Create",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Close Button
                    IconButton(
                        onClick = { onExpandedChange(false) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun NameNoteSection(
    name: TextFieldState,
    note: TextFieldState,
    modifier: Modifier = Modifier
) {
    var showNote by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Name",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BasicTextField(
                state = name,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth(),
                lineLimits = TextFieldLineLimits.SingleLine,
                decorator = { innerTextField ->
                    if (name.text.isEmpty()) {
                        Text(
                            "Enter name",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(Modifier.height(4.dp))

            TextButton(
                onClick = { showNote = !showNote },
                modifier = Modifier.height(24.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Icon(
                    if (showNote) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    "Note",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            AnimatedVisibility(visible = showNote) {
                BasicTextField(
                    state = note,
                    textStyle = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 30.dp),
                    lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 2)
                )
            }
        }
    }
}

@Composable
private fun ScrollableModeSection(
    title: String,
    mode: Int,
    modeLabels: List<String>,
    onModeChange: (Int) -> Unit,
    hours: Duration?,
    minutes: Duration?,
    seconds: Duration?,
    onDurationChange: (Duration?, Duration?, Duration?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // Calculate the section that should be centered based on scroll
    val sectionHeight = with(density) { 32.dp.toPx() }
    val visibleHeight = with(density) { 104.dp.toPx() } // Total visible height
    val centerOffset = (visibleHeight - sectionHeight) / 2

    val currentMode = remember(scrollState.value) {
        derivedStateOf {
            val adjustedScroll = scrollState.value + centerOffset
            ((adjustedScroll / sectionHeight).roundToInt().coerceIn(0, modeLabels.size - 1))
        }
    }

    LaunchedEffect(currentMode.value) {
        if (currentMode.value != mode) {
            onModeChange(currentMode.value)
        }
    }

    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Invisible gradient overlays for bounds indication
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                0f to MaterialTheme.colorScheme.surface,
                                1f to Color.Transparent
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                1f to MaterialTheme.colorScheme.surface
                            )
                        )
                )

                // Center indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .align(Alignment.Center)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.shapes.small
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(vertical = 36.dp), // Padding to center first/last items
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    modeLabels.forEachIndexed { index, label ->
                        val isSelected = currentMode.value == index

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        // Show duration picker for "After" mode
                        if (isSelected && index == 1 && label == "After") {
                            CompactDurationDisplay(
                                hours = hours ?: Duration.ZERO,
                                minutes = minutes ?: Duration.ZERO,
                                seconds = seconds ?: Duration.ZERO,
                                onDurationChange = onDurationChange
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactDurationDisplay(
    hours: Duration,
    minutes: Duration,
    seconds: Duration,
    onDurationChange: (Duration?, Duration?, Duration?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${hours.inWholeHours}:${minutes.inWholeMinutes.toString().padStart(2, '0')}:${seconds.inWholeSeconds.toString().padStart(2, '0')}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            IconButton(
                onClick = {
                    if (seconds > Duration.ZERO) {
                        onDurationChange(hours, minutes, seconds - 10.seconds)
                    }
                },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(Icons.Default.Remove, null, modifier = Modifier.size(12.dp))
            }
            IconButton(
                onClick = { onDurationChange(hours, minutes, seconds + 10.seconds) },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
private fun GoalsCompactSection(
    activeGoal: Duration,
    passiveGoal: Duration,
    startActive: Boolean,
    onActiveGoalChange: (Duration) -> Unit,
    onPassiveGoalChange: (Duration) -> Unit,
    onStartActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Goals",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                // Gradients
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                0f to MaterialTheme.colorScheme.surface,
                                1f to Color.Transparent
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                1f to MaterialTheme.colorScheme.surface
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Start mode toggle
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (startActive)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer,
                        onClick = { onStartActiveChange(!startActive) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (startActive) "Active" else "Passive",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(vertical = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    HorizontalDivider(thickness = 0.5.dp)

                    // Active goal
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            activeGoal.asText(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    if (activeGoal >= 10.seconds)
                                        onActiveGoalChange(activeGoal - 10.seconds)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Remove, null, Modifier.size(14.dp))
                            }
                            IconButton(
                                onClick = { onActiveGoalChange(activeGoal + 10.seconds) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Add, null, Modifier.size(14.dp))
                            }
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp)

                    // Passive goal
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Passive",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            passiveGoal.asText(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    if (passiveGoal >= 10.seconds)
                                        onPassiveGoalChange(passiveGoal - 10.seconds)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Remove, null, Modifier.size(14.dp))
                            }
                            IconButton(
                                onClick = { onPassiveGoalChange(passiveGoal + 10.seconds) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Add, null, Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}