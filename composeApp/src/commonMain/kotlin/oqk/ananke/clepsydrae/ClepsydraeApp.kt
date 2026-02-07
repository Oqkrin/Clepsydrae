package oqk.ananke.clepsydrae

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
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
fun ClepsydraeApp(content: @Composable () -> Unit = { ClepsydraeNavigation() }) {
    KoinApplication(KoinConfiguration { modules(clepsydraeModule()) }) {

        val repository: SettingsRepository = koinInject()
        val settings by repository.getSettings().collectAsState(Settings())
        
        CompositionLocalProvider(LocalSettings provides settings) {
            ClepsydraeTheme(isDarkTheme = settings.isDarkTheme, themeName = settings.theme, fontSize = settings.fontSize) {
                content()
            }
        }
    }
}
