package oqk.ananke.clepsydrae.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.runtime.State
import androidx.window.core.layout.WindowSizeClass
import oqk.ananke.clepsydrae.core.ScreenScope
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

interface SettingsScope : ScreenScope<SettingsScreenState, SettingsAction>

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val vw: SettingsScreenViewModel = koinViewModel()
    val st by vw.state.collectAsState()
    val onAction = vw::onAction
    val ws by koinInject<State<WindowSizeClass>>()
    
    val scope = retain(st, ws, st.settings.uiScale) { object : SettingsScope {
        override val st = st
        override val onAction = onAction
        override val ws: WindowSizeClass = ws
        override val uiScale = st.settings.uiScale
        override val navController = navController
    } }
    
    with(scope) {
        Scaffold(
            topBar = { SettingsTopBar(navController) },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { padding ->
            SettingsContent(Modifier.padding(padding), navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScope.SettingsTopBar(navController: NavController) {
    TopAppBar(
        title = { Text("Settings") },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        }
    )
}

@Composable
fun SettingsScope.SettingsContent(modifier: Modifier = Modifier, navController: NavController) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (st.settings.isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null
                    )
                    Column {
                        Text(
                            text = if (st.settings.isDarkTheme) "Dark Theme" else "Light Theme",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (st.settings.isDarkTheme) "Night Storm" else "Morning Fog",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = st.settings.isDarkTheme,
                    onCheckedChange = { onAction(SettingsAction.ToggleTheme) }
                )
            }
        }
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Theme", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = st.settings.theme == "rain",
                        onClick = { onAction(SettingsAction.SetTheme("rain")) },
                        label = { Text("Rain") }
                    )
                    FilterChip(
                        selected = st.settings.theme == "dynamic",
                        onClick = { onAction(SettingsAction.SetTheme("dynamic")) },
                        label = { Text("Dynamic") }
                    )
                }
            }
        }
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Font Size: ${st.settings.fontSize}", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = st.settings.fontSize.toFloat(),
                    onValueChange = { onAction(SettingsAction.SetFontSize(it.toInt())) },
                    valueRange = 10f..20f,
                    steps = 9
                )
            }
        }
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("UI Scale: ${"%.2f".format(st.settings.uiScale)}x", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = st.settings.uiScale,
                    onValueChange = { onAction(SettingsAction.SetUIScale(it)) },
                    valueRange = 0.75f..1.5f,
                    steps = 14
                )
            }
        }
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Window Size", style = MaterialTheme.typography.titleMedium)
                Text("Width: ${ws.minWidthDp}dp", style = MaterialTheme.typography.bodyMedium)
                Text("Height: ${ws.minHeightDp}dp", style = MaterialTheme.typography.bodyMedium)
                Text("Width Class: ${ws.windowWidthSizeClass}", style = MaterialTheme.typography.bodySmall)
                Text("Height Class: ${ws.windowHeightSizeClass}", style = MaterialTheme.typography.bodySmall)
                Text("isPortrait: $isPortrait", style = MaterialTheme.typography.bodySmall)

            }
        }
        
        Button(
            onClick = { navController.navigate("statistics") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Statistics")
        }
    }
}
