package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.HourglassDisabled
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.navigation.NavController
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import oqk.ananke.clepsydrae.clepsydrae.domain.asText
import oqk.ananke.clepsydrae.clepsydrae.domain.shouldNotifyPomodoro
import oqk.ananke.clepsydrae.clepsydrae.domain.strlapsed
import oqk.ananke.clepsydrae.core.*
import oqk.ananke.clepsydrae.journal.presentation.ClepsydraJournal
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
interface ClepsydraScope : ScreenScope<ClepsydraScreenState, ClepsydraScreenAction>

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ClepsydraScreen(navController: NavController) {

    /** Scope Creation **/
    val vw: ClepsydraScreenViewModel = koinViewModel()
    val st by vw.state.collectAsState()
    val onAction = vw::onAction

    val ws = LocalSizeInfo.current.sizeClass
    val sz = LocalSizeInfo.current.sizes
    val uiScale = LocalSettings.current.uiScale
    val isFirst = LocalSettings.current.isFirstClepsydra

    val notificationManager: NotificationManager = org.koin.compose.koinInject()
    LaunchedEffect(vw) {
        vw.effect.collect { effect ->
            when (effect) {
                is ClepsydraSideEffect.ShowPomodoroNotification -> {
                    notificationManager.sendPomodoroNotification(effect.clepsydra)
                }
            }
        }
    }

    val scope = retain(ws, sz, st) {
        object : ClepsydraScope {
            override val st = st
            override val onAction = onAction
            override val wsc: WindowSizeClass = ws
            override val sizes: DpSize = sz
            override val uiScale: Float = uiScale
            override val navController: NavController = navController
        }
    }

    with(scope) {
        Surface(Modifier.fillMaxSize()) {

            if (isFirst) {
                NotificationPermissionPopUp(st.showNotificationPermissionPopUp) {
                    onAction(ClepsydraScreenAction.OnFirstClepsydraCreationOnResult)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top App Bar
                if (!isShort) {
                    CenterAlignedTopAppBar(
                        modifier = Modifier.fillMaxWidth(),
                        title = { ClepsydraCalendarBar(Modifier) },
                        navigationIcon = {
                            SmallFloatingActionButton(
                                modifier = Modifier,
                                onClick = { onAction(ClepsydraScreenAction.ToggleHistory) }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.List, "History")
                            }
                        },
                        actions = {
                            SmallFloatingActionButton(
                                modifier = Modifier,
                                onClick = { navController.navigate("settings") }
                            ) {
                                Icon(Icons.Default.Settings, "Settings")
                            }
                        }
                    )
                }

                // Main Content Area
                Box(Modifier.weight(1f)) {
                    WaterDroplets()

                    Box(
                        Modifier.fillMaxSize()
                            .padding(horizontal = if (isNarrow && !isShort) 4.dp else 0.dp, vertical = 16.dp)
                    ) {
                        st.coreClepsydra?.let {
                            MorphingTimer(Modifier.align(Alignment.TopCenter))
                        } ?: ClepsydraInputFormV2(modifier = Modifier.align(Alignment.BottomCenter)
                            .padding(
                                start = 4.dp,
                                end = if(!isNarrow) 96.dp+4.dp else 4.dp,
                                bottom = if(isNarrow) 72.dp else 0.dp ))
                        //androidx.compose.animation.AnimatedVisibility
                        androidx.compose.animation.AnimatedVisibility(
                            st.showJournal,
                            modifier = Modifier.align(Alignment.Center),
                            enter = expandIn() + expandVertically(expandFrom = Alignment.Top),
                            exit = shrinkOut() + shrinkVertically(shrinkTowards = Alignment.Top),
                            label = "Journal"
                        ) {
                            ClepsydraJournal(Modifier.align(Alignment.Center))
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = 16.dp, end = 16.dp).align(Alignment.BottomCenter),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isShort) {
                            SmallFloatingActionButton(
                                modifier = Modifier
                                    .align(Alignment.End),
                                onClick = { navController.navigate("settings") }
                            ) {
                                Icon(Icons.Default.Settings, "Settings")
                            }
                            Spacer(Modifier.weight(1f))
                        }
                        if (!isNarrow) {
                            ClepsydraNavigationBar(
                                Modifier
                                    .align(Alignment.End)
                                    .fillMaxHeight(iPhi)
                            )
                        }

                        Row(
                            modifier = Modifier.align(Alignment.End).heightIn(max = if(isShort) 48.dp else 56.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (isNarrow && isShort) {
                                ClepsydraNavigationBar(modifier = Modifier.weight(1f))
                            }
                            ClepsydraTimeBar(modifier = Modifier.height(56.dp))
                        }

                        if (isNarrow && !isShort) ClepsydraNavigationBar(modifier = Modifier.padding(start = 16.dp))
                    }
                }


            }
        }
    }
}


@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ClepsydraScope.ClepsydraNavigationBar(modifier: Modifier = Modifier) {
    val items = listOf("Journal", "Clepsydra", "Habits")
    val selectedIcons = listOf(Icons.Filled.Book, Icons.Filled.HourglassFull, Icons.Filled.Star)
    val unselectedIcons = listOf(Icons.Outlined.Book, Icons.Outlined.HourglassDisabled, Icons.Outlined.StarBorder)
    var selectedItem by remember { mutableIntStateOf(1) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(st.showJournal) {
        if (st.showJournal) selectedItem = 0
    }

    val itemColors = NavigationBarItemColors(
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(iPhi),
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(iPhi),
        selectedIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
        disabledIconColor = MaterialTheme.colorScheme.error,
        disabledTextColor = MaterialTheme.colorScheme.error
    )

    Card(
        shape = RoundedCornerShape(40),
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        if (isNarrow) {
            NavigationBar(
                containerColor = NavigationBarDefaults.containerColor.copy(iPhi),
                modifier = Modifier.height(64.dp).widthIn(max = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp*2/3)
            ) {
                items.fastForEachIndexed { i, label ->
                    NavigationBarItem(
                        selected = i == selectedItem,
                        label = { Text(label, style = MaterialTheme.typography.labelMediumEmphasized, maxLines = 1) },
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            selectedItem = i
                            onAction(ClepsydraScreenAction.OnToggleShowJournal(i == 0))
                        },
                        icon = { Icon(if (i == selectedItem) selectedIcons[i] else unselectedIcons[i], label, modifier = Modifier.size(if(isShort) 14.dp else 24.dp)) },
                        colors = itemColors,
                        alwaysShowLabel = !(isNarrow && isShort)
                    )
                }
            }
        } else {
            NavigationRail(
                containerColor = NavigationBarDefaults.containerColor.copy(iPhi),
                modifier = Modifier.width(64.dp)
            ) {
                Column(
                    Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    items.fastForEachIndexed { i, label ->
                        NavigationRailItem(
                            modifier = Modifier.weight(1f),
                            selected = i == selectedItem,
                            label = {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelSmallEmphasized,
                                    maxLines = 1
                                )
                            },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                selectedItem = i
                                onAction(ClepsydraScreenAction.OnToggleShowJournal(i == 0))
                            },
                            icon = {
                                Icon(
                                    if (i == selectedItem) selectedIcons[i] else unselectedIcons[i],
                                    label,
                                    Modifier.size(24.dp)
                                )
                            },
                            colors = NavigationRailItemColors(
                                selectedIconColor = itemColors.selectedIconColor,
                                unselectedIconColor = itemColors.unselectedIconColor,
                                selectedTextColor = itemColors.selectedTextColor,
                                unselectedTextColor = itemColors.unselectedTextColor,
                                selectedIndicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(iPhi),
                                disabledIconColor = itemColors.disabledIconColor,
                                disabledTextColor = itemColors.disabledTextColor
                            ),
                            alwaysShowLabel = !isShort
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// TIMER COMPONENTS
// ============================================================================

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ClepsydraScope.MorphingTimer(modifier: Modifier = Modifier) {
    val clepsydra = st.coreClepsydra ?: return

    val color by animateColorAsState(
        targetValue = if (clepsydra.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )
    var elapsed by remember { mutableStateOf("00:00") }

    LaunchedEffect(clepsydra.lastStateChange) {
        while (true) {
            elapsed = clepsydra.strlapsed()
            if (clepsydra.shouldNotifyPomodoro() && !st.pomodoroNotifying) {
                onAction(ClepsydraScreenAction.OnPomodoroThresholdCrossed)
            }
            delay(1.seconds)
        }
    }

    val pulse by animateFloatAsState(
        targetValue = if (clepsydra.isActive) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow
        )
    )

    Column(modifier) {


        Box(Modifier.weight(iPhi)) { }

        Box(Modifier.weight(phi)) {
            Surface(
                modifier = Modifier.sq()
                    .clip(CircleShape),
                shape = CircleShape,
                color = color,
                shadowElevation = 6.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                        .graphicsLayer {
                            scaleX = pulse
                            scaleY = pulse
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!clepsydra.isActive) {
                        IconButton(
                            modifier = Modifier.size(24.dp),
                            onClick = { onAction(ClepsydraScreenAction.OnCloseCoreClepsydra) }) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    } else {
                        Spacer(Modifier.size(24.dp))
                    }


                    TonalToggleButton(
                        modifier = Modifier.fillMaxHeight(.3f),
                        checked = clepsydra.isActive,
                        onCheckedChange = { onAction(ClepsydraScreenAction.ToggleDiatesi) },
                        colors = ToggleButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.tertiaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContentColor = MaterialTheme.colorScheme.onSurface,
                            checkedContainerColor = MaterialTheme.colorScheme.primary,
                            checkedContentColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(iPhi), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = elapsed.take(2),
                                style = MaterialTheme.typography.labelSmallEmphasized,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(phi),
                                autoSize = TextAutoSize.StepBased(1.sp, stepSize = 0.001.sp)
                            )
                            ClepsydraTimerSeparator(Modifier.weight(iPhi).fillMaxHeight(.75f), clepsydra.isActive)

                            if (elapsed.length == 8) {
                                Text(
                                    text = elapsed.substring(3, 5),
                                    style = MaterialTheme.typography.displayLargeEmphasized,
                                    maxLines = 1,
                                    modifier = Modifier.weight(phi),
                                    autoSize = TextAutoSize.StepBased(1.sp, stepSize = 0.001.sp)

                                )
                                ClepsydraTimerSeparator(Modifier.weight(iPhi).fillMaxHeight(.75f), clepsydra.isActive)
                            }
                            Text(
                                text = elapsed.takeLast(2),
                                style = MaterialTheme.typography.labelSmallEmphasized,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(phi),
                                autoSize = TextAutoSize.StepBased(1.sp, stepSize = 0.001.sp)
                            )
                        }
                    }

                    if (clepsydra.isActive) {
                        IconButton(
                            modifier = Modifier.size(24.dp),
                            onClick = { onAction(ClepsydraScreenAction.OnCloseCoreClepsydra) }) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    } else {
                        Spacer(Modifier.size(24.dp))
                    }
                }
            }
        }

        Box(Modifier.weight(iPhi)) { }
    }
}

@Composable
private fun ClepsydraTimerSeparator(modifier: Modifier = Modifier, isActive: Boolean) {
    Column(modifier, verticalArrangement = Arrangement.SpaceAround) {
        Icon(
            if (isActive) Icons.Default.PlayArrow else Icons.Default.Stop,
            "player",
            Modifier.weight(1f)
        )
        Icon(
            if (isActive) Icons.Default.PlayArrow else Icons.Default.Stop,
            "player",
            Modifier.weight(1f)
        )
    }
}

// ============================================================================
// INPUT & TIME BAR COMPONENTS
// ============================================================================

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ClepsydraScope.ClepsydraTimeBar(modifier: Modifier = Modifier) {
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
        icon = @Composable { Icon(imageVector = Icons.Default.EditNote, "new note at $nowText", modifier = Modifier.size(28.dp)) },
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
            ) } },
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            onAction(ClepsydraScreenAction.OnCreateNoteAtTime(nowText))
        }
    )
}

// ============================================================================
// TOP BAR & CALENDAR COMPONENTS
// ============================================================================

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ClepsydraScope.ClepsydraCalendarBar(modifier: Modifier = Modifier) {
    // Animation State
    val textOffset = remember { Animatable(0f) }
    val textScale = remember { Animatable(1f) }
    val textRotation = remember { Animatable(0f) }
    val textOpacity = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    fun animateDateChange(direction: Int) {
        coroutineScope.launch {
            // Exit
            launch { textScale.animateTo(1.3f, tween(100, easing = EaseInCubic)) }
            launch { textRotation.animateTo(5f * -direction, tween(100, easing = EaseInCubic)) }
            launch { textOpacity.animateTo(0.6f, tween(100, easing = EaseInCubic)) }
            textOffset.animateTo(200f * -direction, tween(100, easing = EaseInCubic))

            // Snap
            textOffset.snapTo(200f * direction)
            textRotation.snapTo(-5f * -direction)

            // Enter
            launch { textScale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)) }
            launch { textRotation.animateTo(0f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)) }
            launch { textOpacity.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)) }
            textOffset.animateTo(0f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow))
        }
    }

    Row(
        modifier = modifier
            .widthIn(max = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp * iPhi)
            .fillMaxWidth()
            .height(64.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Previous Button (Satellite) ---
        Box(
            modifier = Modifier.fillMaxHeight().sq(),
            contentAlignment = Alignment.Center
        ) {
            ArrowButton(
                modifier = Modifier.size(48.dp),
                rotation = 360,
                onClick = {
                    animateDateChange(-1)
                    onAction(ClepsydraScreenAction.OnPreviousDay)
                }
            )
        }

        Box(
            modifier = Modifier.height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                onClick = { navController.navigate("calendar") },
                modifier = Modifier,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 6.dp
            ) {

                Column(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val dateComp = st.dateText.split(" ")
                    Text(
                        text = dateComp[0],
                        modifier = Modifier.weight(7f).wrapContentHeight()
                            .offset(x = textOffset.value.dp)
                            .scale(textScale.value)
                            .rotate(textRotation.value)
                            .alpha(textOpacity.value),
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        autoSize = TextAutoSize.StepBased(1.sp, stepSize = 0.001.sp)
                    )
                    Text(
                        text = "${dateComp[1]} ${dateComp[2]} ${dateComp[3]}",
                        modifier = Modifier.weight(5f).wrapContentHeight()
                            .offset(x = textOffset.value.dp)
                            .scale(textScale.value)
                            .rotate(textRotation.value)
                            .alpha(textOpacity.value),
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        color = LocalContentColor.current.copy(alpha = iPhi),
                        autoSize = TextAutoSize.StepBased(1.sp, stepSize = 0.001.sp)
                    )
                }
            }
        }

        // --- Next Button (Satellite) ---
        Box(
            modifier = Modifier.fillMaxHeight().sq(),
            contentAlignment = Alignment.Center
        ) {
            ArrowButton(
                modifier = Modifier.size(48.dp),
                rotation = 180,
                onClick = {
                    animateDateChange(1)
                    onAction(ClepsydraScreenAction.OnNextDay)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ClepsydraScope.ArrowButton(
    modifier: Modifier = Modifier,
    rotation: Int,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    // Animate scale: shrink slightly when pressed (inverse phi effect)
    val targetScale = if (pressed) iPhi else 1f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(4.dp, MaterialShapes.Arrow.toShape(rotation))
            .clip(MaterialShapes.Arrow.toShape(rotation))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {}
}

// ============================================================================
// VISUAL EFFECTS & BACKGROUND
// ============================================================================

data class Droplet(
    val progress: Float,
    val x: Float,
    val speed: Float,
    val size: Float,
    val drift: Float,
    val isStreak: Boolean
)

@Composable
fun ClepsydraScope.WaterDroplets() {
    val timerActive = MaterialTheme.colorScheme.primary
    val timerInactive = MaterialTheme.colorScheme.secondary
    val color by remember(st.coreClepsydra?.isActive) {
        derivedStateOf { if (st.coreClepsydra?.isActive == true) timerActive else timerInactive }
    }
    var droplets by remember { mutableStateOf(listOf<Droplet>()) }
    val isActive = st.coreClepsydra?.isActive == true

    val rowPresets = remember {
        listOf(
            (0..60).map { it / 60f },
            (0..55).map { it / 55f },
            (0..70).map { it / 70f },
            (0..45).map { it / 45f },
            (0..50).map { (it * 1.3f) % 1f },
            (0..48).map { (it * 1.7f) % 1f },
            (0..65).map { (it * 0.8f) % 1f },
            (0..42).map { (it * 2.1f) % 1f },
            (0..58).map { (it * 1.1f) % 1f },
            (0..52).map { (it * 1.5f) % 1f }
        )
    }

    LaunchedEffect(isActive) {
        var offset = 0f
        while (true) {
            delay(if (isActive) Random.nextLong(40L, 100L) else Random.nextLong(200L, 400L))
            val pattern = rowPresets.random()
            val baseSpeed = if (isActive) 0.028f else 0.012f

            offset = (offset + iPhi) % 1f
            val newDroplets = pattern.mapIndexed { idx, x ->
                val isStreak = Random.nextFloat() < if (isActive) 0.55f else 0.15f
                val sizeRandom = 1f - sqrt(Random.nextFloat())
                val spreadX = (x + offset + idx * 0.01f) % 1f

                Droplet(
                    progress = 0f,
                    x = (spreadX + Random.nextFloat() * 0.02f - 0.01f).coerceIn(0f, 1f),
                    speed = baseSpeed + Random.nextFloat() * 0.018f,
                    size = if (isStreak) 0.6f + sizeRandom * 0.5f else 0.4f + sizeRandom * 0.8f,
                    drift = if (isActive) 0.018f + Random.nextFloat() * 0.012f else Random.nextFloat() * 0.012f - 0.006f,
                    isStreak = isStreak
                )
            }
            droplets = droplets.filter { it.progress < 1f } + newDroplets
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            droplets = droplets.map { it.copy(progress = it.progress + it.speed) }.filter { it.progress < 1f }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                onDrawBehind {
                    droplets.forEach { droplet ->
                        val eased = droplet.progress * droplet.progress
                        val alpha = (1f - eased) * if (isActive) 0.88f else 0.5f
                        val xDrift = droplet.drift * droplet.progress * size.width
                        val x = droplet.x * size.width + xDrift
                        val y = droplet.progress * size.height

                        if (droplet.isStreak) {
                            val streakLength = 18f * droplet.size
                            val path = Path().apply {
                                moveTo(x, y)
                                lineTo(x, y - streakLength)
                            }
                            drawPath(
                                path = path,
                                color = color.copy(alpha = alpha),
                                style = Stroke(width = 2.5f * droplet.size)
                            )
                        } else {
                            val w = 6f * droplet.size
                            val h = 9f * droplet.size
                            val path = Path().apply {
                                moveTo(x, y + h)
                                cubicTo(x + w * 0.35f, y + h * 0.7f, x + w * 0.45f, y + h * 0.4f, x, y)
                                cubicTo(x - w * 0.45f, y + h * 0.4f, x - w * 0.35f, y + h * 0.7f, x, y + h)
                                close()
                            }
                            drawPath(path = path, color = color.copy(alpha = alpha))
                        }
                    }
                }
            }
    )
}