package oqk.ananke.clepsydrae

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.tooling.preview.Preview
import androidx.window.core.layout.WindowSizeClass

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            ClepsydraeApp(getWindowSizeClass())
        }
    }
}

@Composable

fun getWindowSizeClass(): WindowSizeClass {
    val windowSizeClass by mutableStateOf(currentWindowAdaptiveInfo().windowSizeClass)

    return windowSizeClass
}