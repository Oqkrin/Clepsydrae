package oqk.ananke.clepsydrae

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.window.core.layout.WindowSizeClass
import oqk.ananke.clepsydrae.core.ClepsydraeTheme
import oqk.ananke.clepsydrae.core.LocalSettings
import oqk.ananke.clepsydrae.di.clepsydraeModule
import oqk.ananke.clepsydrae.navigation.ClepsydraeNavigation
import oqk.ananke.clepsydrae.settings.domain.Settings
import oqk.ananke.clepsydrae.settings.domain.SettingsRepository
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.KoinConfiguration

@Composable
@Preview
fun ClepsydraeApp(
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
    content: @Composable () -> Unit = { ClepsydraeNavigation() }
) {
    val windowSizeState = rememberUpdatedState(windowSizeClass)
    KoinApplication(KoinConfiguration { modules(clepsydraeModule(windowSizeState)) }) {

        val repository: SettingsRepository = koinInject()
        val settings by repository.getSettings().collectAsState(Settings())
        
        CompositionLocalProvider(LocalSettings provides settings) {
            ClepsydraeTheme(isDarkTheme = settings.isDarkTheme, themeName = settings.theme, fontSize = settings.fontSize) {
                content()
            }
        }
    }
}
