package oqk.ananke.clepsydrae.calendar.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import oqk.ananke.clepsydrae.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearScreen(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Year") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screen.DAY.name) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Day")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.SETTINGS.name) }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Year view — coming soon", style = MaterialTheme.typography.titleMedium)
        }
    }
}
