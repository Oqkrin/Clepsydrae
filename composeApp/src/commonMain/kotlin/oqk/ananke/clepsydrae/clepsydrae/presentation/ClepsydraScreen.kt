package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.compose.animation.core.*
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.navigation.NavController
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import oqk.ananke.clepsydrae.clepsydrae.domain.asText
import oqk.ananke.clepsydrae.clepsydrae.domain.shouldNotifyPomodoro
import oqk.ananke.clepsydrae.clepsydrae.domain.strlapsed
import oqk.ananke.clepsydrae.core.*
import oqk.ananke.clepsydrae.journal.domain.TimelineItem
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
                    Row {
                        Column(
                            modifier = Modifier.width(if (!isNarrow || isShort) 70.dp else 0.dp)
                                .padding(start = 16.adp())
                        ) { }
                        Column(
                            modifier = Modifier.weight(1f)
                                .padding(horizontal = if (isNarrow && !isShort) 16.dp else 0.dp)
                        ) {
                            Box {
                                st.coreClepsydra?.let {
                                    MorphingTimer(Modifier.align(Alignment.Center))
                                    SmallFloatingActionButton(
                                        modifier = Modifier
                                            .align(if (!isShort) Alignment.BottomCenter else Alignment.BottomEnd)
                                            .adaptivePadding(),
                                        onClick = { onAction(ClepsydraScreenAction.OnCloseCoreClepsydra) },
                                    ) {
                                        Icon(Icons.Default.Close, "Close")
                                    }
                                } ?: ClepsydraInputFormV2(modifier = Modifier.align(Alignment.BottomCenter))

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

                    }
                        Column(modifier = Modifier.width(if(!isNarrow || isShort) 70.dp else 0.dp).padding(end = 16.adp()), horizontalAlignment = Alignment.End) {
                            if (isShort) {
                                Row(modifier = Modifier.weight(iPhi),horizontalArrangement = Arrangement.End) {
                                    SmallFloatingActionButton(
                                        modifier = Modifier
                                            .padding(top = 16.adp()),
                                        onClick = { navController.navigate("settings") }
                                    ) {
                                        Icon(Icons.Default.Settings, "Settings")
                                    }
                                }
                            }
                            if (!isNarrow || isShort) {

                                Row( modifier = Modifier.weight(phi),horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {

                                    ClepsydraNavigationBar(Modifier)

                                }
                            }
                        }
                    }

                }

                Column (modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(horizontal = 16.adp()) , verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally ) {
                    ClepsydraTimeBar(modifier = Modifier.height(56.dp).align(Alignment.End))
                    Spacer(modifier = Modifier.height(8.adp()))
                    if(isNarrow && !isShort) ClepsydraNavigationBar()
                }


            }
        }
    }
}


@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ClepsydraScope.ClepsydraNavigationBar(
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(if(isShort) 50 else 75),
        modifier = modifier.padding(4.adp())
            .widthIn(max = WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND.dp)
            .heightIn(max = if (isNarrow && !isShort) (if(isShort) 48.dp else 64.dp) else (sizes.height*2/3) * iPhi),
        colors = CardDefaults.cardColors().copy(containerColor = Color.Transparent)
    ) {
        val items = listOf("Journal", "Clepsydra", "Habits")
        val selectedIcons = listOf(
            Icons.Filled.Book,
            Icons.Filled.HourglassFull,
            Icons.Filled.Star
        )
        val unselectedIcons = listOf(
            Icons.Outlined.Book,
            Icons.Outlined.HourglassDisabled,
            Icons.Outlined.StarBorder
        )
        var selectedItem by remember { mutableIntStateOf(1) }
        val haptic = LocalHapticFeedback.current

        if (isNarrow && !isShort) {
            NavigationBar(containerColor = NavigationBarDefaults.containerColor.copy(alpha = iPhi)) {

                items.fastForEachIndexed { i, label ->

                    NavigationBarItem(
                        selected = i == selectedItem,
                        label = if (!isShort) { {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelMediumEmphasized,
                                color = LocalContentColor.current.copy(if (i == selectedItem) 1f else iPhi),
                            )
                        } }else null,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            selectedItem = i
                            onAction(ClepsydraScreenAction.OnToggleShowJournal(i==0))
                        },
                        icon = {
                            Icon(
                                imageVector = if (i == selectedItem) selectedIcons[i] else unselectedIcons[i],
                                label
                            )
                        },
                        colors = NavigationBarItemColors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = iPhi),
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = iPhi),
                            selectedIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            disabledIconColor = MaterialTheme.colorScheme.error,
                            disabledTextColor = MaterialTheme.colorScheme.error
                        )
                    )

                }

            }

        } else {
            NavigationRail(containerColor = NavigationBarDefaults.containerColor.copy(alpha = iPhi)) {
                Column(modifier.padding(2.adp()).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceAround) {
                    items.fastForEachIndexed { i, label ->

                        NavigationRailItem(
                            modifier = Modifier.weight(1f),
                            selected = i == selectedItem,
                            label = {
                                Box(modifier = Modifier.width(40.dp)) {
                                        Text(
                                            label,
                                            style = MaterialTheme.typography.labelSmallEmphasized,
                                            color = LocalContentColor.current.copy(if (i == selectedItem) 1f else iPhi),
                                            autoSize = TextAutoSize.StepBased(1.sp, stepSize = 0.0001.sp),
                                            maxLines = 1
                                        )
                                    }
                                },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                selectedItem = i

                                onAction(ClepsydraScreenAction.OnToggleShowJournal(i==0))


                            },
                            icon = {
                                Icon(
                                    imageVector = if (i == selectedItem) selectedIcons[i] else unselectedIcons[i],
                                    label,
                                    modifier = Modifier.sizeIn(0.dp, 0.dp, 28.dp, 28.dp).size(lerp(0.dp, 28.dp,
                                        sizes.height.value/WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND))
                                )
                            },
                            colors = NavigationRailItemColors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = iPhi),
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = iPhi),
                                selectedIndicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = iPhi),
                                disabledIconColor = MaterialTheme.colorScheme.error,
                                disabledTextColor = MaterialTheme.colorScheme.error
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
            .height(56.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Previous Button (Satellite) ---
        Box(
            modifier = Modifier.fillMaxHeight().sq(),
            contentAlignment = Alignment.Center
        ) {
            ArrowButton(
                modifier = Modifier.fillMaxSize(iPhi),
                rotation = 360,
                onClick = {
                    animateDateChange(-1)
                    onAction(ClepsydraScreenAction.OnPreviousDay)
                }
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth(iPhi).fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                onClick = { navController.navigate("calendar") },
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 6.dp,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().adaptivePadding(minPadding = 6.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val dateComp = st.dateText.split(" ")
                    Text(
                        text = "${dateComp[1]} ${dateComp[2]} ${dateComp[3]}",
                        modifier = Modifier.weight(8f)
                            .offset(x = textOffset.value.dp)
                            .scale(textScale.value)
                            .rotate(textRotation.value)
                            .alpha(textOpacity.value),
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        autoSize = TextAutoSize.StepBased(2.sp)
                    )
                    Text(
                        text = dateComp[0],
                        modifier = Modifier.weight(5f)
                            .offset(x = textOffset.value.dp)
                            .scale(textScale.value)
                            .rotate(textRotation.value)
                            .alpha(textOpacity.value),
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        color = LocalContentColor.current.copy(alpha = iPhi),
                        autoSize = TextAutoSize.StepBased(2.sp)
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
                modifier = Modifier.fillMaxSize(iPhi),
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
// TIMER COMPONENTS
// ============================================================================

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ClepsydraScope.MorphingTimer(modifier: Modifier = Modifier) {
    val clepsydra = st.coreClepsydra ?: return

    val colors = MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.inversePrimary
    val color by remember(clepsydra.isActive) {
        derivedStateOf { if (clepsydra.isActive) colors.first else colors.second }
    }
    var elapsed by remember { mutableStateOf("00:00") }
    val morphProgress by animateFloatAsState(
        targetValue = if (clepsydra.isActive) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )
    val scale by animateFloatAsState(
        targetValue = if (clepsydra.isActive) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow)
    )

    LaunchedEffect(clepsydra.lastStateChange) {
        while (true) {
            elapsed = clepsydra.strlapsed()
            if (clepsydra.shouldNotifyPomodoro() && !st.pomodoroNotifying) {
                onAction(ClepsydraScreenAction.OnPomodoroThresholdCrossed)
            }
            delay(1.seconds)
        }
    }

    val downDrop = remember {
        RoundedPolygon(
            vertices = floatArrayOf(0.5f, 0.9f, 0.85f, 0.4f, 0.5f, 0.1f, 0.15f, 0.4f),
            perVertexRounding = listOf(
                CornerRounding(0.01f),
                CornerRounding(0.5f),
                CornerRounding(0.5f),
                CornerRounding(0.5f)
            ),
            centerX = 0.5f,
            centerY = 0.5f
        )
    }

    val upDrop = remember {
        RoundedPolygon(
            vertices = floatArrayOf(0.5f, 0.1f, 0.15f, 0.6f, 0.5f, 0.9f, 0.85f, 0.6f),
            perVertexRounding = listOf(
                CornerRounding(0.01f),
                CornerRounding(0.5f),
                CornerRounding(0.5f),
                CornerRounding(0.5f)
            ),
            centerX = 0.5f,
            centerY = 0.5f
        )
    }

    val matrix = remember { Matrix() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(1f, !isPortrait)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawWithCache {
                val morph = Morph(start = upDrop, end = downDrop)
                val morphPath = morph.toPath(progress = morphProgress)
                matrix.reset()
                matrix.scale(size.minDimension, size.minDimension)
                matrix.translate(
                    (size.width - size.minDimension) / 2f,
                    (size.height - size.minDimension) / 2f
                )
                morphPath.transform(matrix)

                onDrawBehind { drawPath(morphPath, color = color) }
            },
        contentAlignment = Alignment.Center
    ) {
        TimerContent(elapsed)
    }
}

@Composable
fun ClepsydraScope.TimerContent(elapsed: String) {
    val clepsydra = st.coreClepsydra ?: return

    // Subtle pulse animation for the timer
    val pulse by animateFloatAsState(
        targetValue = if (clepsydra.isActive) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow
        )
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TextField(
                value = clepsydra.name ?: "",
                onValueChange = { onAction(ClepsydraScreenAction.OnSetName(it)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Light),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.widthIn(max = 180.dp)
            )
        }

        Text(
            text = elapsed,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Light,
            color = if (!st.pomodoroNotifying) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.error
        )
        IconButton(onClick = { onAction(ClepsydraScreenAction.ToggleDiatesi) }) {
            Icon(
                imageVector = if (clepsydra.isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (clepsydra.isActive) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ============================================================================
// BOTTOM BAR & INPUT
// ============================================================================

@Composable
fun ClepsydraScope.ClepsydraInputFormV2(modifier: Modifier) {
    // TODO: Implement input form UI
}


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
            onAction(ClepsydraScreenAction.OnCreateNoteAtTime(nowText)) }
    )

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