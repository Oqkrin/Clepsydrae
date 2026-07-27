package oqk.ananke.clepsydrae.calendar.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import oqk.ananke.clepsydrae.navigation.Screen
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekScreen(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Week") },
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
        val primaryColor = MaterialTheme.colorScheme.primary

        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(300.dp)) {
                val center = this.center
                val radius = size.minDimension / 2 - 40.dp.toPx()
                val pentagonRadius = 30.dp.toPx()

                // 7 pentagons arranged in a circle
                for (i in 0 until 7) {
                    val angle = i * (2 * PI / 7) - PI / 2
                    val x = center.x + radius * cos(angle).toFloat()
                    val y = center.y + radius * sin(angle).toFloat()
                    val path = createPolygonPath(x, y, pentagonRadius, 5)
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }
}
