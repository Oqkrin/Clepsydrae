package oqk.ananke.clepsydrae.habits.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun HabitsScreen(navController: NavController, contentPadding: PaddingValues = PaddingValues()) {
    Surface(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Habits — coming soon", style = MaterialTheme.typography.titleMedium)
        }
    }
}
