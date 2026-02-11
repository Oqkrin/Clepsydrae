package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.navigation.NavController
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import oqk.ananke.clepsydrae.clepsydrae.domain.dts
import oqk.ananke.clepsydrae.clepsydrae.domain.shouldNotifyPomodoro
import oqk.ananke.clepsydrae.clepsydrae.domain.strlapsed
import oqk.ananke.clepsydrae.core.*
import oqk.ananke.clepsydrae.settings.presentation.SettingsScreenViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

interface ClepsydraScope : ScreenScope<ClepsydraScreenState, ClepsydraScreenAction>

/**Initial Application Screen where the day-to-day timers live**/
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ClepsydraScreen(navController: NavController) {

    /**Scope Creation**/
    val vw: ClepsydraScreenViewModel = koinViewModel()

    val st by vw.state.collectAsState()
    val onAction = vw::onAction
    val ws by koinInject<State<WindowSizeClass>>()
    val uiScale = LocalSettings.current.uiScale
    val notificationManager: NotificationManager = koinInject()
    /*Permission Settings*/
    val ssvm: SettingsScreenViewModel = koinViewModel()
    val isFirstClepsydra = LocalSettings.current.isFirstClepsydra


    val scope = retain(st, ws) {
        object : ClepsydraScope {
            override val st = st
            override val onAction = onAction
            override val ws: WindowSizeClass = ws
            override val uiScale: Float = uiScale
            override val navController: NavController = navController
            override val notificationManager: NotificationManager = notificationManager
            override val isFirstClepsydra = isFirstClepsydra
        }
    }


    with(scope) {
        Surface(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                if (isTall || isExtraTall) {
                    CenterAlignedTopAppBar(
                        modifier = Modifier.fillMaxWidth(),
                        title = { ClepsydraCalendarBar(Modifier) },
                        navigationIcon = {

                            SmallFloatingActionButton(
                                modifier = Modifier,
                                onClick = { onAction(ClepsydraScreenAction.ToggleHistory) }) {
                                Icon(Icons.AutoMirrored.Filled.List, "History")
                            }

                        },
                        actions = {
                            SmallFloatingActionButton(
                                modifier = Modifier,
                                onClick = { navController.navigate("settings") }) {
                                Icon(Icons.Default.Settings, "Settings")
                            }
                        }
                    )
                }

                Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {

                    WaterDroplets()

                    if (!isTall && !isExtraTall) {

                        SmallFloatingActionButton(
                            modifier = Modifier.align(Alignment.TopEnd).adaptivePadding(),
                            onClick = { navController.navigate("settings") }) {
                            Icon(Icons.Default.Settings, "Settings")
                        }

                        Column(Modifier.align(Alignment.TopStart)) {
                            SmallFloatingActionButton(
                                modifier = Modifier.adaptivePadding(),
                                onClick = { onAction(ClepsydraScreenAction.ToggleHistory) }) {
                                Icon(Icons.AutoMirrored.Filled.List, "History")
                            }
                        }
                    }



                    st.currentClepsydra?.let {

                        MorphingTimer(Modifier.align(Alignment.Center))
                        ClepsydraActionsControllerFab(
                            text = "finish",
                            onClick = { onAction(ClepsydraScreenAction.OnClose) },
                            modifier = Modifier
                                .align(if (isTall || isExtraTall) Alignment.BottomCenter else Alignment.BottomEnd)
                                .adaptivePadding()
                        ) {

                            Icon(Icons.Default.Close, "Close")

                        }
                    } ?: ClepsydraActionsControllerFab(
                        text = "New Clepsydra",

                        onClick = {
                            if(st.shouldAskForNotificationPermission)
                                onAction(ClepsydraScreenAction.NotificationsPermissioner(notificationManager, isFirstClepsydra, ssvm ))
                            onAction(ClepsydraScreenAction.OnCreateClepsydra()) },
                        modifier = Modifier
                            .align(if (isTall || isExtraTall) Alignment.BottomCenter else Alignment.BottomEnd)
                            .adaptivePadding()
                    ) {

                        Icon(Icons.Default.Add, "Create")

                    }

                    if (st.showHistory) HistoryList()

                    if (st.showNameDialog) NameDialog()
                }

            }
        }
    }
}

@Composable
fun ClepsydraScope.ClepsydraActionsControllerFab(text: String,
                                                 onClick: () -> Unit,
                                                 modifier: Modifier = Modifier,
                                                 containerColor: Color = FloatingActionButtonDefaults.containerColor,
                                                 contentColor: Color = contentColorFor(containerColor),
                                                 content: @Composable (() -> Unit)) {

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {

        if (st.shouldAskForNotificationPermission && isFirstClepsydra) {
            ElevatedCard(
                modifier = Modifier.padding(bottom = 8.adp()),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Text(text = "You will be prompted to allow notification for timer", style = MaterialTheme.typography.labelSmall)
            }
        }

        if (isExtraWide || isWide) {
            ExtendedFloatingActionButton(
                onClick = onClick,
                containerColor = containerColor,
                contentColor = contentColor
            ) {
                Text(text)
            }
        } else {
            if (isTall || isExtraTall) {
                FloatingActionButton(
                    onClick = onClick,
                    containerColor = containerColor,
                    contentColor = contentColor,
                ) {
                    content()
                }
            } else {
                SmallFloatingActionButton(
                    onClick = onClick,
                    modifier = modifier,
                    containerColor = containerColor,
                    contentColor = contentColor,
                ) {
                    content()
                }
            }
        }
    }
}
@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ClepsydraScope.ClepsydraCalendarBar(modifier: Modifier = Modifier) {
    // 1. Animation State
    val textOffset = remember { Animatable(0f) }
    val textScale = remember { Animatable(1f) }
    val textRotation = remember { Animatable(0f) }
    val textOpacity = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    // 2. Reusable Animation Logic (Keeps your code clean)
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
            .widthIn(max = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp*iPhi)
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .fillMaxHeight(if(isExtraTall) .05f else .085f),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // --- Previous Button (Satellite) ---
        Box(
            modifier = Modifier.fillMaxHeight().sq(), // Square touch target
            contentAlignment = Alignment.Center
        ) {
            ArrowButton(
                modifier = Modifier.fillMaxSize(iPhi), // Visual size is smaller (~40dp)
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

            ExtendedFloatingActionButton(
                onClick = { navController.navigate("calendar") },
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = st.dateText,
                    modifier = Modifier
                        .offset(x = textOffset.value.dp)
                        .scale(textScale.value)
                        .rotate(textRotation.value)
                        .alpha(textOpacity.value),
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    autoSize = TextAutoSize.StepBased(2.sp)
                )
            }
        }

        // --- Next Button (Satellite) ---
        Box(
            modifier = Modifier.fillMaxHeight().sq(), // Square touch target
            contentAlignment = Alignment.Center
        ) {
            ArrowButton(
                modifier = Modifier.fillMaxSize(iPhi), // Visual size is smaller (~40dp)
                rotation = 180,
                onClick = {
                    animateDateChange(1)
                    onAction(ClepsydraScreenAction.OnNextDay)
                }
            )
        }
    }
}
// Updated ArrowButton to ensure it handles the modifier passed correctly
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
            // Apply shadow/clip before background
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


@Composable
fun ClepsydraScope.NameDialog() {
    AlertDialog(
        onDismissRequest = { onAction(ClepsydraScreenAction.OnCreateWithName) },
        title = { Text("Timer Name") },
        text = {
            st.currentClepsydra?.name?.let { name ->
                OutlinedTextField(
                    value = name,
                    onValueChange = { onAction(ClepsydraScreenAction.OnSetName(name)) },
                    label = { Text("Name (optional)") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onAction(ClepsydraScreenAction.OnConfirmName) }) {
                Text("Create")
            }
        }
    )
}

@Composable
fun ClepsydraScope.HistoryList() {

    AnimatedVisibility(
        visible = st.showHistory,
        enter = slideInHorizontally { it },
        exit = slideOutHorizontally { it }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
            Surface(
                modifier = Modifier.width(200.dp).fillMaxHeight().windowInsetsPadding(WindowInsets.systemBars),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shadowElevation = 8.dp
            ) {
                LazyColumn(modifier = Modifier.padding(12.dp)) {
                    items(st.pastClepsydrae) { clepsydra ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                .animateItem(fadeInSpec = tween(300), fadeOutSpec = tween(300)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            onClick = { onAction(ClepsydraScreenAction.OnRestore(clepsydra)) }
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    if (!clepsydra.name.isNullOrBlank()) {
                                        Text(clepsydra.name, style = MaterialTheme.typography.labelMedium)
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(12.dp))
                                        Text(dts(clepsydra.totalActiveTime), style = MaterialTheme.typography.bodySmall)
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Pause, null, modifier = Modifier.size(12.dp))
                                        Text(
                                            dts(clepsydra.totalPassiveTime),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                clepsydra.id?.let { id ->
                                    IconButton(
                                        onClick = { onAction(ClepsydraScreenAction.OnDelete(id)) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val GOLDEN_RATIO = iPhi

data class Droplet(val progress: Float, val x: Float, val speed: Float, val size: Float, val drift: Float, val isStreak: Boolean)

@Composable
fun ClepsydraScope.WaterDroplets() {
    val color by remember(st.currentClepsydra?.isActive) {
        derivedStateOf { if (st.currentClepsydra?.isActive == true) TimerActive else TimerInactive }
    }
    var droplets by remember { mutableStateOf(listOf<Droplet>()) }
    val isActive = st.currentClepsydra?.isActive == true

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
            val goldenRatio = GOLDEN_RATIO
            offset = (offset + goldenRatio) % 1f
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

    Box(Modifier.fillMaxSize().drawWithCache {
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
                            drawPath(path, color.copy(alpha = alpha), style = Stroke(width = 2.5f * droplet.size))
                        } else {
                            val w = 6f * droplet.size
                            val h = 9f * droplet.size
                            val path = Path().apply {
                                moveTo(x, y + h)
                                cubicTo(x + w * 0.35f, y + h * 0.7f, x + w * 0.45f, y + h * 0.4f, x, y)
                                cubicTo(x - w * 0.45f, y + h * 0.4f, x - w * 0.35f, y + h * 0.7f, x, y + h)
                                close()
                            }
                            drawPath(path, color.copy(alpha = alpha))
                        }
                    }
                }
            }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ClepsydraScope.MorphingTimer(modifier: Modifier = Modifier) {
    val clepsydra = st.currentClepsydra ?: return

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
            if (clepsydra.shouldNotifyPomodoro() && !st.pomodoroNotifying) onAction(ClepsydraScreenAction.OnPomodoroThresholdCrossed(notificationManager))
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
        modifier
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
        Alignment.Center
    ) {
        TimerContent(elapsed)
    }
}

@Composable
fun ClepsydraScope.TimerContent(elapsed: String) {
    val clepsydra = st.currentClepsydra ?: return

    // Subtle pulse animation for the timer
    val pulse by animateFloatAsState(
        targetValue = if (clepsydra.isActive) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow
        )
    )

    Column(
        modifier = Modifier.padding(16.dp).graphicsLayer {
            scaleX = pulse
            scaleY = pulse
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (!clepsydra.name.isNullOrBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = clepsydra.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                IconButton(onClick = { onAction(ClepsydraScreenAction.OnCreateWithName) }, modifier = Modifier.size(16.dp)) {
                    Icon(Icons.Default.Edit, "Edit name", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
                }
            }
        } else {
            IconButton(onClick = { onAction(ClepsydraScreenAction.OnCreateWithName) }, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Edit, "Edit name", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
            }
        }
        Text(
            text = elapsed,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Light,
            color = if(!st.pomodoroNotifying) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.error
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



