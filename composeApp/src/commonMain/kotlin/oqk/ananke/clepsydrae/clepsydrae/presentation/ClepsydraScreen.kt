package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.window.core.layout.WindowSizeClass
import kotlinx.datetime.LocalDateTime
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import oqk.ananke.clepsydrae.clepsydrae.domain.dts
import oqk.ananke.clepsydrae.core.LocalWindowSizeClass
import oqk.ananke.clepsydrae.core.ScreenScope
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

interface ClepsydraScope : ScreenScope<ClepsydraState, ClepsydraAction>

@Composable
fun ClepsydraScreen(navController: NavController) {
    val vw: ClepsydraViewModel = koinViewModel()
    val st by vw.state.collectAsState()
    val onAction = vw::handleAction
    val ws = LocalWindowSizeClass.current


    val scope = retain(st, ws) {  object : ClepsydraScope {
        override val st = st
        override val onAction = onAction
        override val ws: WindowSizeClass = ws
    } }

    with(scope) {

        if (st.showNameDialog) NameDialog()

        Scaffold(
            topBar = { ClepsydraTopBar(navController) },
            bottomBar = { ClepsydraBottomBar() },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                CurrentClepsydraView()

                AnimatedVisibility(
                    visible = st.showHistory,
                    enter = slideInHorizontally { it },
                    exit = slideOutHorizontally { it }
                ) {

                    HistoryList()

                }
            }
        }
    }
}

@Composable
fun ClepsydraScope.ClepsydraBottomBar() {
    Box(modifier = Modifier.fillMaxWidth().responsivePadding()) {
        FloatingActionButton(
            onClick = { onAction(ClepsydraAction.Create) },
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(Icons.Default.Add, "Create")
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun ClepsydraScope.ClepsydraTopBar(navController: NavController) {
    val today = remember {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val dayName = now.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val monthName = now.month.name.lowercase().replaceFirstChar { it.uppercase() }
        "$dayName ${now.day} $monthName ${now.year}"
    }
    Row(modifier = Modifier.fillMaxWidth().responsivePadding(), horizontalArrangement = Arrangement.SpaceAround) {

        FloatingActionButton(onClick = { navController.navigate("settings") }) {
            Icon(Icons.Default.Settings, "Settings")
        }


        ExtendedFloatingActionButton(onClick = { onAction(ClepsydraAction.Create) }) {
            Text(today)
        }


        FloatingActionButton(onClick = { onAction(ClepsydraAction.ToggleHistory) }) {
            Icon(Icons.AutoMirrored.Filled.List, "History")
        }

    }
}

@Composable
fun ClepsydraScope.NameDialog() {
    AlertDialog(
        onDismissRequest = { onAction(ClepsydraAction.ConfirmName) },
        title = { Text("Timer Name") },
        text = {
            OutlinedTextField(
                value = st.name,
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
fun ClepsydraScope.HistoryList() {

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        Surface(
            modifier = Modifier.width(200.dp).fillMaxHeight(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shadowElevation = 8.dp
        ) {

            LazyColumn(modifier = Modifier.padding(12.dp)) {
                items(st.savedClepsydrae) { clepsydra ->
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
