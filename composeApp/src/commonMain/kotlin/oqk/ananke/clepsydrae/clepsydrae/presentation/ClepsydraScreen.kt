package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import kotlinx.coroutines.delay
import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra
import oqk.ananke.clepsydrae.clepsydrae.domain.dts
import oqk.ananke.clepsydrae.clepsydrae.domain.strlapsed
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.seconds

@Composable
fun ClepsydraScreen() {
    val vw: ClepsydraViewModel = koinViewModel()
    val st by vw.state.collectAsState()

    if (st.showNameDialog) {
        NameDialog(name = st.name, onAction = vw::handleAction)
    }

    Scaffold(
        floatingActionButton = { ClepsydraFABs(state = st, onAction = vw::handleAction) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            CurrentClepsydraView(clepsydra = st.currentClepsydra, onAction = vw::handleAction)
            AnimatedVisibility(
                visible = st.showHistory,
                enter = slideInHorizontally { it },
                exit = slideOutHorizontally { it }
            ) {
                HistoryList(clepsydrae = st.savedClepsydrae, vw::handleAction)
            }
        }
    }
}

@Composable
fun NameDialog(name: String, onAction: (ClepsydraAction) -> Unit) {
    AlertDialog(
        onDismissRequest = { onAction(ClepsydraAction.ConfirmName) },
        title = { Text("Timer Name") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { onAction(ClepsydraAction.SetName(it)) },
                label = { Text("Name (optional)") }
            )
        },
        confirmButton = {
            TextButton(onClick = { onAction(ClepsydraAction.ConfirmName) }) {
                Text("Create")
            }
        }
    )
}

@Composable
fun ClepsydraFABs(state: ClepsydraState, onAction: (ClepsydraAction) -> Unit) {
    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FloatingActionButton(onClick = { onAction(ClepsydraAction.ToggleHistory) }) {
            Icon(Icons.AutoMirrored.Filled.List, "History")
        }

        if (state.currentClepsydra != null) {
            FloatingActionButton(onClick = { onAction(ClepsydraAction.Close) }) {
                Icon(Icons.Default.Close, "Close")
            }
        } else {
            FloatingActionButton(onClick = { onAction(ClepsydraAction.Create) }) {
                Icon(Icons.Default.Add, "Create")
            }
        }
    }
}

@Composable
fun HistoryList(clepsydrae: List<Clepsydra>, onAction: (ClepsydraAction) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        Surface(
            modifier = Modifier.width(200.dp).fillMaxHeight(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shadowElevation = 8.dp
        ) {
            LazyColumn(modifier = Modifier.padding(12.dp)) {
                items(clepsydrae) { clepsydra ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                            .animateItem(fadeInSpec = tween(300), fadeOutSpec = tween(300)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        onClick = { onAction(ClepsydraAction.Restore(clepsydra)) }
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
                                    Text(dts(clepsydra.totalPassiveTime), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            clepsydra.id?.let { id ->
                                IconButton(
                                    onClick = { onAction(ClepsydraAction.Delete(id)) },
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

@Composable
fun CurrentClepsydraView(clepsydra: Clepsydra?, onAction: (ClepsydraAction) -> Unit) {
    val colors = MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.inversePrimary
    val color by remember(clepsydra?.isActive) {
        derivedStateOf { if (clepsydra?.isActive == true) colors.first else colors.second }
    }
    
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        clepsydra?.let { 
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MorphingTimer(clepsydra = it, onAction = onAction)
                if (it.isActive) {
                    WaterDroplets(color = color)
                }
            }
        }
    }
}

@Composable
fun WaterDroplets(color: Color) {
    var droplets by remember { mutableStateOf(listOf<Float>()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1200)
            droplets = droplets.filter { it < 1f } + 0f
        }
    }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            droplets = droplets.map { it + 0.015f }.filter { it < 1f }
        }
    }
    
    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.TopCenter) {
        droplets.forEach { progress ->
            val eased = progress * progress
            val alpha by animateFloatAsState(1f - eased, tween(100))
            val size = 6.dp + (eased * 4).dp
            Box(
                modifier = Modifier
                    .offset(y = (eased * 200).dp)
                    .size(size)
                    .graphicsLayer { 
                        this.alpha = alpha
                        scaleY = 1f + eased * 0.3f
                    }
                    .drawWithCache {
                        onDrawBehind {
                            drawCircle(color.copy(alpha = 0.7f))
                        }
                    }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MorphingTimer(clepsydra: Clepsydra, onAction: (ClepsydraAction) -> Unit) {
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

    Box(
        modifier = Modifier.size(200.dp).graphicsLayer {
            scaleX = scale
            scaleY = scale
        }.drawWithCache {
            val circle = RoundedPolygon.circle(
                radius = size.minDimension / 2f,
                centerX = size.width / 2f,
                centerY = size.height / 2f
            )
            val diamond = RoundedPolygon(
                numVertices = 4,
                radius = size.minDimension / 2f,
                centerX = size.width / 2f,
                centerY = size.height / 2f,
                rounding = CornerRounding(size.minDimension / 20f)
            )
            val morph = Morph(start = circle, end = diamond)
            val morphPath = morph.toPath(progress = morphProgress)

            onDrawBehind { drawPath(morphPath, color = color) }
        },
        contentAlignment = Alignment.Center
    ) {
        TimerContent(clepsydra = clepsydra, elapsed = elapsed, onAction = onAction)
    }
}

@Composable
fun TimerContent(clepsydra: Clepsydra, elapsed: String, onAction: (ClepsydraAction) -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (!clepsydra.name.isNullOrBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = clepsydra.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                IconButton(onClick = { onAction(ClepsydraAction.CreateWithName) }, modifier = Modifier.size(16.dp)) {
                    Icon(Icons.Default.Edit, "Edit name", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
                }
            }
        } else {
            IconButton(onClick = { onAction(ClepsydraAction.CreateWithName) }, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Edit, "Edit name", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
            }
        }
        Text(
            text = elapsed,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Light,
            color = Color.White
        )
        IconButton(onClick = { onAction(ClepsydraAction.Toggle) }) {
            Icon(
                imageVector = if (clepsydra.isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (clepsydra.isActive) "Pause" else "Play",
                tint = Color.White
            )
        }
    }
}
