package oqk.ananke.clepsydrae

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import oqk.ananke.clepsydrae.core.DarkRainScheme
import oqk.ananke.clepsydrae.core.LightRainScheme
import oqk.ananke.clepsydrae.core.ThemePreference
import oqk.ananke.clepsydrae.di.appModule
import oqk.ananke.clepsydrae.navigation.AppNavigation
import org.koin.compose.KoinApplication
import org.koin.dsl.KoinConfiguration

@Composable
@Preview
fun App() {
    val isDarkTheme by ThemePreference.isDarkTheme
    
    MaterialTheme(colorScheme = if (isDarkTheme) DarkRainScheme else LightRainScheme) {
        KoinApplication(KoinConfiguration { modules(appModule()) }) {
            AppNavigation()
        }
    }
}
