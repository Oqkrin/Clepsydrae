package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import kotlinx.coroutines.delay
import oqk.ananke.clepsydrae.clepsydrae.domain.strlapsed
import oqk.ananke.clepsydrae.core.TimerActive
import oqk.ananke.clepsydrae.core.TimerInactive
import oqk.ananke.clepsydrae.core.iPhi
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

private const val GOLDEN_RATIO = 0.618034f

data class Droplet(val progress: Float, val x: Float, val speed: Float, val size: Float, val drift: Float, val isStreak: Boolean)

@Composable
fun ClepsydraScope.CurrentClepsydraView() {
    val color by remember(st.currentClepsydra?.isActive) {
        derivedStateOf { if (st.currentClepsydra?.isActive == true) TimerActive else TimerInactive }
    }
    var showRain by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        st.currentClepsydra?.let {
            WaterDroplets(color, isPaused = !showRain)
            
            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { showRain = !showRain }
                ) {
                    Icon(
                        if (showRain) Icons.Default.Pause else Icons.Default.PlayArrow,
                        if (showRain) "Hide rain" else "Show rain"
                    )
                }
                SmallFloatingActionButton(
                    onClick = { onAction(ClepsydraAction.OnClose) }
                ) {
                    Icon(Icons.Default.Close, "Close")
                }
            }

            Box(
                modifier = Modifier.fillMaxSmallest(iPhi).aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                MorphingTimer()
            }
        }
    }
}

@Composable
fun ClepsydraScope.WaterDroplets(color: Color, isPaused: Boolean = false) {
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
    
    LaunchedEffect(isActive, isPaused) {
        if (isPaused) return@LaunchedEffect
        var offset = 0f
        while (true) {
            delay(if (isActive) Random.nextLong(40L, 100L) else Random.nextLong(200L, 400L))
            val pattern = rowPresets.random()
            val baseSpeed = if (isActive) 0.028f else 0.012f
            val goldenRatio = GOLDEN_RATIO
            offset = (offset + goldenRatio) % 1f
            val newDroplets = pattern.mapIndexed { idx, x ->
                val isStreak = Random.nextFloat() < if (isActive) 0.55f else 0.15f
                val sizeRandom = 1f - kotlin.math.sqrt(Random.nextFloat())
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
    
    LaunchedEffect(isPaused) {
        if (isPaused) return@LaunchedEffect
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
                            drawPath(path, color.copy(alpha = alpha), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f * droplet.size))
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
fun ClepsydraScope.MorphingTimer() {
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
        modifier = Modifier
            .fillMaxSize()
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
                IconButton(onClick = { onAction(ClepsydraAction.OnCreateWithName) }, modifier = Modifier.size(16.dp)) {
                    Icon(Icons.Default.Edit, "Edit name", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
                }
            }
        } else {
            IconButton(onClick = { onAction(ClepsydraAction.OnCreateWithName) }, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Edit, "Edit name", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
            }
        }
        Text(
            text = elapsed,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = { onAction(ClepsydraAction.ToggleDiatesi) }) {
            Icon(
                imageVector = if (clepsydra.isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (clepsydra.isActive) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
